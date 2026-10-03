// 처리량 baseline 테스트 (분산 부하)
// 실제 상황: 수강신청 오픈 직후 전교생이 여러 과목에 동시 접속하는 정상 피크
// 목적: 30과목에 부하를 분산시키며 VU를 올려 "시스템 한계 처리량(무릎 지점)"을 찾는다.
//
// 전제: 앱이 loadtest 프로파일로 떠 있어야 함 (userId 헤더 인증)
// 실행: k6 run -e BASE_URL=http://<EC2-IP> loadtest/throughput-baseline.js
//   (시계열까지 남기려면) k6 run --out csv=loadtest/results/ts.csv -e BASE_URL=... ...

import http from 'k6/http';
import { sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.4/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost';
const SUBJECT_COUNT = parseInt(__ENV.SUBJECT_COUNT || '30'); // 시드된 과목 수

const enrollSuccess = new Counter('enroll_success');   // 최종 신청 성공
const enrollRejected = new Counter('enroll_rejected'); // 비즈니스 거절(정원/중복/학점초과 등)
// 서버 에러(5xx/네트워크) 단계별 집계 → 실패 "원인/위치" 추적
const errApply = new Counter('err_apply_5xx');
const errTry = new Counter('err_try_5xx');
const errConfirm = new Counter('err_confirm_5xx');

// 비즈니스 거절(400)은 정상 응답으로 취급 → http_req_failed는 실제 실패(5xx/네트워크)만 집계
http.setResponseCallback(http.expectedStatuses(200, 400));

export const options = {
  scenarios: {
    baseline: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 100 },
        { duration: '2m', target: 500 },
        { duration: '2m', target: 1000 },
        { duration: '2m', target: 2000 }, // 목표 피크(4학년 코호트 가정)
        { duration: '2m', target: 2000 }, // 피크 유지
        { duration: '1m', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],    // 실제 실패(5xx/네트워크) 5% 미만
    http_req_duration: ['p(95)<2000'], // p95 2초 미만 (관찰 기준선)
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

export default function () {
  const userId = __VU; // VU별 고정 userId (시드 user1~)
  const code = 'SUBJ' + ('00' + randomInt(1, SUBJECT_COUNT)).slice(-3);

  // 1) 대기열 진입
  const aRes = http.post(`${BASE_URL}/courses/apply`, `code=${code}`, hdr(userId, 'apply'));
  count5xx(aRes, errApply);
  const apply = safeJson(aRes);
  if (!apply || apply.status !== 'WAITING') {
    enrollRejected.add(1);
    sleep(randomInt(1, 3));
    return;
  }

  // 2) 내 차례 폴링 (최대 10회)
  let allowed = false;
  for (let i = 0; i < 10; i++) {
    const tRes = http.get(`${BASE_URL}/courses/apply/try?code=${code}`, hdr(userId, 'try'));
    count5xx(tRes, errTry);
    const t = safeJson(tRes);
    if (t && t.status === 'ALLOWED') { allowed = true; break; }
    if (t && t.status === 'FAIL') break;
    sleep(0.5);
  }

  // 3) 최종 확정
  if (allowed) {
    const cRes = http.post(`${BASE_URL}/courses/apply/confirm?code=${code}`, null, hdr(userId, 'confirm'));
    count5xx(cRes, errConfirm);
    const c = safeJson(cRes);
    if (c && c.status === 'SUCCESS') enrollSuccess.add(1);
    else enrollRejected.add(1);
  } else {
    enrollRejected.add(1);
  }

  sleep(randomInt(1, 3)); // think time
}

function hdr(userId, step) {
  return {
    headers: { userId: String(userId), 'Content-Type': 'application/x-www-form-urlencoded' },
    tags: { step: step }, // 단계별 지연/실패 분석용
  };
}
function count5xx(res, counter) { if (res.status >= 500 || res.status === 0) counter.add(1); }
function randomInt(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
function safeJson(res) { try { return res.json(); } catch (e) { return null; } }

// 터미널엔 전체 요약 + 핵심 지표 강조, 파일엔 JSON 저장(비교용)
export function handleSummary(data) {
  const ts = new Date().toISOString().replace(/[:T]/g, '-').slice(0, 19);
  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }) + highlight(data),
    [`loadtest/results/throughput-baseline-${ts}.json`]: JSON.stringify(data, null, 2),
  };
}
function highlight(data) {
  const m = data.metrics;
  const cnt = (k) => (m[k] && m[k].values.count != null ? m[k].values.count : 0);
  const trend = (k, s) => (m[k] && m[k].values[s] != null ? m[k].values[s] : 0);
  const succ = cnt('enroll_success');
  const total = succ + cnt('enroll_rejected');
  const rate = total > 0 ? ((succ / total) * 100).toFixed(1) : '0.0';
  const p95 = trend('http_req_duration', 'p(95)');
  const p99 = trend('http_req_duration', 'p(99)');
  const rps = trend('http_reqs', 'rate');
  const failed = (trend('http_req_failed', 'rate') * 100).toFixed(2);
  return [
    '\n========== 핵심 지표 (처리량 baseline) ==========',
    `신청 성공 / 시도   : ${succ} / ${total}  (성공률 ${rate}%)`,
    `처리량(http_reqs)  : ${rps.toFixed(1)} req/s`,
    `응답시간 p95 / p99 : ${p95.toFixed(0)} / ${p99.toFixed(0)} ms`,
    `실제 실패율(5xx)   : ${failed} %`,
    `└ 5xx 발생 단계    : apply=${cnt('err_apply_5xx')} / try=${cnt('err_try_5xx')} / confirm=${cnt('err_confirm_5xx')}`,
    '================================================\n',
  ].join('\n');
}
