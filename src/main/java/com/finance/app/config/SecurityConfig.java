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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for REST APIs
                .cors(cors -> cors.configure(http)) // Enable CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No
                                                                                                              // Sessions
                .authorizeHttpRequests(auth -> auth
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/auth/**"),
                            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/health"),
                            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/"),
                            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/index.html"),
                            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/assets/**"),
                            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/*.js"),
                            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/*.css"),
                            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/favicon.ico")
                        ).permitAll() // Public endpoints and static resources
                        .anyRequest().authenticated() // All other requests require login
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
