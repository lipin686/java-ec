package com.example.demo.config;

import com.example.demo.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 配置類
 * 配置認證、授權規則以及 JWT 過濾器
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * 配置安全過濾鏈
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 啟用CORS
            .csrf(csrf -> csrf.disable())  // 停用 CSRF，因為我們用的是 JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 設定為無狀態會話管理
            .authorizeHttpRequests(authz -> authz
                // 公開訪問的路徑
                .requestMatchers("/api/v1/auth/**").permitAll()  // 前台認證相關路徑開放訪問
                .requestMatchers("/admin/v1/auth/**").permitAll()  // 後台認證相關路徑開放訪問
                .requestMatchers("/api/public/**").permitAll()  // 公開API

                // 前台API路徑 - 需要USER或ADMIN角色
                .requestMatchers("/api/v1/**").hasAnyRole("USER", "ADMIN")

                // 後台管理路徑 - 只允許ADMIN角色
                .requestMatchers("/admin/v1/**").hasRole("ADMIN")

                // 保留舊路徑的相容性
                .requestMatchers("/api/frontend/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/backend/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 其他路徑需要認證
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // 註冊 JWT 過濾器

        return http.build();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允許的來源 - 包括Docker和本地開發環境
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://frontend:3000"
        ));

        // 允許的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 允許的請求頭
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 允許攜帶認證信息
        configuration.setAllowCredentials(true);

        // 預檢請求的有效期
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 密碼加密器 Bean
     * 用於加密和驗證密碼
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 使用 BCrypt 加密演算法
    }

    /**
     * 認證管理器 Bean
     * 用於手動驗證 JWT
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
