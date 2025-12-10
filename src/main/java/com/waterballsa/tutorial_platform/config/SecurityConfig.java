package com.waterballsa.tutorial_platform.config;

import com.waterballsa.tutorial_platform.service.CustomAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 關閉 CSRF
                .cors(cors -> {}) // 啟用 CORS
                .authorizeHttpRequests(auth -> auth
                        // 1. 公開 GET API (排行榜、課程、道館、任務)
                        .requestMatchers(HttpMethod.GET, "/api/leaderboard").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/journeys/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/gyms/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/missions/**").permitAll()

                        // 2. 靜態資源與登入頁
                        .requestMatchers("/", "/sign-in", "/error", "/images/**").permitAll()

                        // ★★★ 3. 放行開發者登入 API (必須移到 anyRequest 之前！) ★★★
                        .requestMatchers("/api/auth/dev-login").permitAll()

                        // 4. 其他剩下的所有請求都要登入 (這行必須是最後一行)
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/sign-in?error")
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}