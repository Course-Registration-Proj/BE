package com.practice.course_registration.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((auth) -> auth.disable());

        http
                .sessionManagement((session) -> session
                        .sessionFixation().changeSessionId()
                        .maximumSessions(1) // 다중 로그인 허용 X
                        .maxSessionsPreventsLogin(false) // 다중 로그인 시 새로운 로그인 차단. true로 하면 로그인 실패 오류
                        .expiredUrl("/session/expired")
                );
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/h2-console/**", "/join", "/session/expired", "/login").permitAll() // 일단 개발을 위해서 모든 접근 허용. 필요 시 수정
                        .anyRequest().authenticated() // 로그인한 회원만 like 페이지에 접속 가능한 것 확인.
                );

        http
                .formLogin((form) -> form
                        .loginPage("/login") // 로그인 성공 시 redirect url 추후 작성
                        .defaultSuccessUrl("/courses", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                );

        http
                .headers((headers) -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        http
                .logout((logout) -> logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
