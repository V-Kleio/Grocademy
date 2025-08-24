package com.grocademy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.grocademy.config.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        CustomAuthenticationEntryPoint customAuthenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/auth", "/register", "/login", "/logout", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                .requestMatchers("/api/auth/login").permitAll()

                // Auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers("/api/auth/self").hasAnyRole("USER", "ADMIN")

                // Course
                .requestMatchers(HttpMethod.GET, "/api/courses", "/api/courses/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/courses/my-courses").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/courses/*/buy").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("ADMIN")

                // Module
                .requestMatchers(HttpMethod.GET, "/api/courses/*/modules", "/api/modules/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/courses/*/modules").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/modules/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/modules/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/courses/*/modules/reorder").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/modules/*/complete").hasAnyRole("USER", "ADMIN")

                // User
                .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/*/balance").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            // .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(customAuthenticationEntryPoint)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}