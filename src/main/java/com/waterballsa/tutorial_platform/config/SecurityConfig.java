package com.waterballsa.tutorial_platform.config;

import com.waterballsa.tutorial_platform.service.CustomAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // 將預設的 SCOPE_ 字首清空，讓我們可以直接使用 ROLE_XXX
        grantedAuthoritiesConverter.setAuthorityPrefix(""); 
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // ★ 修改重點 1: 掛載 CORS 設定
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // 允許 CORS Preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 既有的公開路徑
                        .requestMatchers("/api/leaderboard").permitAll()
                        .requestMatchers("/api/journeys/**").permitAll()
                        .requestMatchers("/api/gyms/**").permitAll()
                        .requestMatchers("/api/missions/**").permitAll()
                        .requestMatchers("/api/lessons/**").permitAll()

                        // ★ 修改重點 2: 新增這一行，允許公開讀取/變更使用者的資料
                        .requestMatchers("/api/users/**").permitAll()
                        .requestMatchers("/api/orders/**").permitAll()
                        .requestMatchers("/api/visitor/**").permitAll()
                        .requestMatchers("/api/gym-challenge-records/**").permitAll()
                        .requestMatchers("/api/learning-records/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/announcements/**").permitAll()
                        .requestMatchers("/", "/sign-in", "/error", "/images/**", "/logo.png", "/favicon.ico").permitAll()
                        .requestMatchers("/api/auth/dev-login").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/quick-register").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureUrl("/sign-in?error")
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .permitAll()
                );

        return http.build();
    }


    // ★ 修改重點 3: 定義 CORS 規則 (允許 localhost:3000)
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // ★ 支援 Vercel 與網域萬用字元
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "https://*.vercel.app",
                "https://your-production-domain.com" // 若有自定義網域請在此加入
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}