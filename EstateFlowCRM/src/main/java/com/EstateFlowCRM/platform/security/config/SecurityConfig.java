package com.EstateFlowCRM.platform.security.config;

import com.EstateFlowCRM.platform.security.security.JwtAuthenticationFilter;
import com.EstateFlowCRM.platform.security.security.ApiSecurityExceptionHandler;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final ApiSecurityExceptionHandler apiSecurityExceptionHandler;

    // Security Filter Chain
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                // Disable CSRF
                .csrf(csrf -> csrf.disable())

                // Configure Authorization
                .authorizeHttpRequests(auth -> auth

                        // Public Endpoints
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login"//todo the login is wrong when login as a sales agent and sales manager
                        ).permitAll()

                        // Admin Endpoints
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register"
                        ).hasRole("OVERVIEW_ADMIN")

                        // Sales Manager Endpoints
                        //todo

                        // Sales Agent Endpoints
                        //todo

                        // All Other Requests Require Authentication
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(apiSecurityExceptionHandler) // لما Spring Security يحتاج AuthenticationEntryPoint، استخدم كائن apiSecurityExceptionHandler.
                        .accessDeniedHandler(apiSecurityExceptionHandler) // لما Spring Security يحتاج AccessDeniedHandler، استخدم نفس الكائن.
                )

                // Stateless Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Authentication Provider
                .authenticationProvider(authenticationProvider())

                // JWT Filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }



    // Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication Provider
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    // Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }

}
