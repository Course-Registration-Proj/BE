// 동시성 경합 · 정원 정합성 테스트
// 실제 상황: 인기 과목(꿀강) 쟁탈전 — 정원 30짜리에 수백 명이 같은 과목·같은 순간에 광클
// 목적: 정원 초과 등록이 안 되는가 / 중복 신청이 차단되는가 (Redis Lua + DB 조건부 UPDATE 검증)
//
// 전제: 앱이 loadtest 프로파일로 떠 있어야 함
// 실행(핫과목 1개): k6 run -e BASE_URL=http://<EC2-IP> -e HOT_SUBJECTS=1 -e VUS=300 loadtest/capacity-contention.js
//     (핫과목 2개): k6 run -e BASE_URL=http://<EC2-IP> -e HOT_SUBJECTS=2 -e VUS=300 loadtest/capacity-contention.js
// ⚠️ 매 실행 전 DB를 깨끗한 상태로 (destroy→apply) 두어야 정원 검증이 정확함.

import http from 'k6/http';
import { sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.4/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost';
const HOT_SUBJECTS = parseInt(__ENV.HOT_SUBJECTS || '1'); // 경쟁 대상 과목 수 (1 또는 2)
const VUS = parseInt(__ENV.VUS || '300');                 // 동시 신청 인원

const enrollSuccess = new Counter('enroll_success'); // 정원 안에 들어 성공
const capacityFull = new Counter('capacity_full');   // 정원 초과로 거절
const otherReject = new Counter('other_reject');     // 그 외 거절

// 비즈니스 거절(400)은 정상 응답으로 취급 → http_req_failed는 실제 실패(5xx/네트워크)만 집계
http.setResponseCallback(http.expectedStatuses(200, 400));

export const options = {
  scenarios: {
    contention: {
      executor: 'per-vu-iterations',
      vus: VUS,
      iterations: 1,       // 각 유저 1회 신청 → 동시 경합
      maxDuration: '2m',
    },
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

export default function () {
  const userId = __VU;
  const hot = ((__VU - 1) % HOT_SUBJECTS) + 1;       // 1..HOT_SUBJECTS 로 고르게 배정
  const code = 'SUBJ' + ('00' + hot).slice(-3);

  // 1) 대기열 진입
  const apply = safeJson(http.post(`${BASE_URL}/courses/apply`, `code=${code}`, formHeaders(userId)));
  if (!apply || apply.status !== 'WAITING') { classify(apply); return; }

  // 2) 내 차례 폴링
  let allowed = false;
  for (let i = 0; i < 15; i++) {
    const t = safeJson(http.get(`${BASE_URL}/courses/apply/try?code=${code}`, formHeaders(userId)));
    if (t && t.status === 'ALLOWED') { allowed = true; break; }
    if (t && t.status === 'FAIL') { classify(t); return; }
    sleep(0.3);
  }
  if (!allowed) { otherReject.add(1); return; }

  // 3) 최종 확정
  const confRes = http.post(`${BASE_URL}/courses/apply/confirm?code=${code}`, null, formHeaders(userId));
  const c = safeJson(confRes);
  if (c && c.status === 'SUCCESS') enrollSuccess.add(1);
  else if (confRes.status === 400) capacityFull.add(1); // 정원 참(confirm 400) → 정원 초과 거절
  else otherReject.add(1);
}

// 검증 포인트(실행 후 확인):
//  - enroll_success == 30 * HOT_SUBJECTS  (정원만큼만 성공)
//  - 각 과목 registeredNum <= limitedNum(30)  → 정원 초과 0
//  - 같은 유저 중복 성공 0

function classify(body) {
  const msg = (body && body.message) ? String(body.message) : '';
  if (msg.indexOf('정원') >= 0 || msg.toUpperCase().indexOf('CAPACITY') >= 0) capacityFull.add(1);
  else otherReject.add(1);
}
function formHeaders(userId) {
  return { headers: { userId: String(userId), 'Content-Type': 'application/x-www-form-urlencoded' } };
}
function safeJson(res) { try { return res.json(); } catch (e) { return null; } }

// 터미널엔 전체 요약 + 정합성 지표 강조, 파일엔 JSON 저장(비교용)
export function handleSummary(data) {
  const ts = new Date().toISOString().replace(/[:T]/g, '-').slice(0, 19);
  return {
    stdout: textSummary(data, { indent: ' ', enableColors: true }) + highlight(data),
    [`loadtest/results/capacity-contention-${ts}.json`]: JSON.stringify(data, null, 2),
  };
}
function highlight(data) {
  const m = data.metrics;
  const cnt = (k) => (m[k] && m[k].values.count != null ? m[k].values.count : 0);
  const trend = (k, s) => (m[k] && m[k].values[s] != null ? m[k].values[s] : 0);
  const hot = parseInt(__ENV.HOT_SUBJECTS || '1');
  const p95 = trend('http_req_duration', 'p(95)');
  const failed = (trend('http_req_failed', 'rate') * 100).toFixed(2);
  return [
    '\n========== 정합성 검증 (경합) ==========',
    `신청 성공(정원 내) : ${cnt('enroll_success')}   (기대: 30 x ${hot} = ${30 * hot})`,
    `정원 초과 거절     : ${cnt('capacity_full')}`,
    `기타 거절          : ${cnt('other_reject')}`,
    `응답시간 p95       : ${p95.toFixed(0)} ms`,
    `HTTP 실패율        : ${failed} %`,
    '>>> 각 과목 registeredNum <= 30 인지 서버에서 추가 확인',
    '=======================================\n',
  ].join('\n');
}
