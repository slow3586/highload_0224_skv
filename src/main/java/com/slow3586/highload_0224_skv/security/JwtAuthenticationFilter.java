package com.slow3586.highload_0224_skv.security;

import com.slow3586.highload_0224_skv.entity.UserEntity;
import com.slow3586.highload_0224_skv.repository.read.UserReadRepository;
import com.slow3586.highload_0224_skv.service.PasswordService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final JwtService jwtService;
    private final UserReadRepository userReadRepository;
    private final PasswordService passwordService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        var authHeader = request.getHeader(HEADER_NAME);
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(BEARER_PREFIX.length());
        UUID uuid = jwtService.extractUserId(jwt);

        if (uuid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserEntity userEntity = userReadRepository.findById(uuid).orElseThrow();

            // Если токен валиден, то аутентифицируем пользователя
            if (jwtService.isTokenValid(jwt, userEntity)) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userEntity.getId(),
                    null,
                    null);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(request, response);
    }
}
