package com.EstateFlowCRM.platform.security.security;

import com.EstateFlowCRM.platform.security.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        final String jwt;
        final String username;

        // 1. Check Authorization Header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract JWT
        jwt = authHeader.substring(7);

        // 3. Extract Username from JWT
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception exception) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Check if user is not already authenticated
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // 5. Load User from Database
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            try {
                // 6. Validate JWT
                if (userDetails.isEnabled()
                        && userDetails.isAccountNonExpired()
                        && userDetails.isAccountNonLocked()
                        && userDetails.isCredentialsNonExpired()
                        && jwtService.isTokenValid(jwt, userDetails)) {

                    // 7. Create Authentication
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // 8. Set Authentication Details
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // 9. Set Authentication in Security Context
                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (Exception exception) {
                SecurityContextHolder.clearContext();
            }
        }

        // 10. Continue Filter Chain
        filterChain.doFilter(request, response);
    }
}