package com.connectsphere.auth.security;

import com.connectsphere.auth.service.JwtClaims;
import com.connectsphere.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	// OncePerRequestFilter guarantee karta hai ye filter ek request pe
	// sirf ONCE run hoga, chahe Spring internally redirect kare ya nahi
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        // "Bearer eyJhbGci..."
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            jwtService.validateToken(token).ifPresent(claims -> {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        claims.userId(),
                        null, // ← credentials (password nahi chahiye ab)
                        List.of(new SimpleGrantedAuthority("ROLE_" + claims.role()))
                );
                authentication.setDetails(claims);
                // ↑ puri JwtClaims object details mein store karo
                // controller mein zaroorat pade toh nikal sako
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // ↑ Spring ko bata do: "ye request authenticated hai"
                // is thread ke liye valid rahega
            });
        }

        filterChain.doFilter(request, response);
        // ↑ CRITICAL: ye line hamesha call karni padti hai
        // agar nahi kiya toh request yahan RUKH JAAYEGI
        // chahe token valid ho ya na ho
    }
}

