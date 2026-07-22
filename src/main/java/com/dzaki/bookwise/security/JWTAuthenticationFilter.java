package com.dzaki.bookwise.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dzaki.bookwise.entity.User;
import com.dzaki.bookwise.repo.UserRepo;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    
    private final JWTUtil jwtUtil;

    private final UserRepo userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // 🚀 Skip JWT Validation for Swagger & OpenAPI endpoints
        if (
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-resources") ||
            path.startsWith("/webjars")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            // Check if the request path requires authentication
            if (path.startsWith("/api/v1/auth")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // Return 401 with JSON error for missing token
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"timestamp\":\"" + java.time.LocalDateTime.now() + "\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Access token not found or invalid format\",\"path\":\"" + path + "\"}");
            return;
        }

        try {
            jwt = authHeader.substring(7);
            username = jwtUtil.extractEmail(jwt); // extractEmail karena JWT sekarang menggunakan email sebagai subject

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                User user = userRepo.findByEmail(username).orElse(null);

                if (user != null && jwtUtil.validateToken(jwt, user.getEmail())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user, 
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Token invalid/expired - return 401 with JSON error
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"timestamp\":\"" + java.time.LocalDateTime.now() + "\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid or expired token\",\"path\":\"" + request.getServletPath() + "\"}");
        }
    }
}