package com.practice.course_registration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((auth) -> auth.disable());

        http
                .sessionManagement((auth) -> auth
                        .maximumSessions(1) // 다중 로그인 허용 X
                        .maxSessionsPreventsLogin(true) // 다중 로그인 시 새로운 로그인 차단.
                );
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/", "/h2-console/**").permitAll() // 일단 개발을 위해서 모든 접근 허용. 필요 시 수정
                        .anyRequest().authenticated()
                );

        http
                .formLogin((form) -> form
                        .loginPage("/login") // 로그인 성공 시 redirect url 추후 작성
                        .permitAll()
                );

        http
                .headers((headers) -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        http
                .logout((logout) -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
