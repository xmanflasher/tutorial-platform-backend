package com.waterballsa.tutorial_platform.config;

import com.waterballsa.tutorial_platform.service.CustomAuthenticationSuccessHandler;
import com.waterballsa.tutorial_platform.service.CustomAuthenticationFailureHandler;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final RateLimitingFilter rateLimitingFilter;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.cors.allowed-patterns}")
    private String corsAllowedPatterns;

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
                .addFilterBefore(rateLimitingFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                // ★ 修改重點 1: 掛載 CORS 設定
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // 允許 CORS Preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 1. 允許所有人的路徑 (僅限 GET)
                        .requestMatchers(HttpMethod.GET, "/api/leaderboard").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/journeys/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/gyms/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/missions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lessons/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/announcements/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/visitor/**").permitAll()
                        .requestMatchers("/api/health").permitAll()

                        // 2. 註冊與開發登入 (實務上 dev-login 應在生產環境停用)
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/quick-register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/dev-login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/visitor/**").permitAll()

                        // 3. 靜態資源與基礎頁面
                        .requestMatchers("/", "/sign-in", "/error", "/images/**", "/logo.png", "/favicon.ico").permitAll()

                        // 其餘所有請求 (如 POST /api/orders, PATCH /api/users) 接需驗證
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                .oauth2Login(oauth2 -> {
                        oauth2
                            .successHandler(customAuthenticationSuccessHandler)
                            .failureHandler(customAuthenticationFailureHandler);
                })
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


    // ★ 修改重點 3: 定義 CORS 規則 (動態支援環境變數)
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        String extraOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        
        List<String> allowedPatterns = new java.util.ArrayList<>();
        
        // 1. 加入配置檔案中的 Pattern
        if (corsAllowedPatterns != null && !corsAllowedPatterns.isEmpty()) {
            allowedPatterns.addAll(List.of(corsAllowedPatterns.split(",")));
        }
        
        // 2. 額外支持前端 URL (如果有單獨設定)
        if (frontendUrl != null && !frontendUrl.isEmpty() && !allowedPatterns.contains(frontendUrl)) {
            allowedPatterns.add(frontendUrl);
        }
        
        // 3. 支持 Env 注入的額外 Origin
        if (extraOrigins != null && !extraOrigins.isEmpty()) {
            allowedPatterns.addAll(List.of(extraOrigins.split(",")));
        }

        config.setAllowedOriginPatterns(allowedPatterns);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}