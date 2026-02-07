package com.finance.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.finance.app.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                // Keep only paths that strictly break with Security Headers (like H2 console)
                // Moving assets to permitAll so CORS headers are applied
                .requestMatchers(
                        new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/h2-console/**"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for REST APIs
                .cors(cors -> cors.configure(http)) // Enable CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No
                                                                                                              // Sessions
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/auth/**"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/health"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/error"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
                                        "/api/debug/**"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/index.html"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/assets/**"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/*.js"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/*.css"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/*.ico"))
                        .permitAll() // Public endpoints
                        .anyRequest().authenticated() // All other requests require login
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList("*"));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
        configuration.setAllowCredentials(false);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
