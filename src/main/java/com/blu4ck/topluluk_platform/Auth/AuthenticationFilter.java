//package com.blu4ck.topluluk_platform.Auth;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class AuthenticationFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private AuthService authService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        System.out.println("🔍 AuthenticationFilter çalışıyor...");
//
//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//
//            AuthService.UserInfo userInfo = authService.validateTokenAndGetUser(token);
//
//            if (userInfo != null) {
//                // Authentication başarılı
//                request.setAttribute("userId", userInfo.getUserId());
//                request.setAttribute("userEmail", userInfo.getEmail());
//                request.setAttribute("isAuthenticated", true);
//                System.out.println("✅ User authenticated: " + userInfo.getEmail());
//            } else {
//                // Authentication başarısız
//                request.setAttribute("isAuthenticated", false);
//                System.out.println("❌ Authentication failed");
//            }
//        } else {
//            // Token yok
//            request.setAttribute("isAuthenticated", false);
//            System.out.println("⚠️ No token provided");
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}

package com.blu4ck.topluluk_platform.Auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("🔍 AuthenticationFilter çalışıyor...");

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            AuthService.UserInfo userInfo = authService.validateTokenAndGetUser(token);

            if (userInfo != null) {
                // Authentication başarılı - Spring Security context'ine set et
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userInfo.getUserId(),
                                null,
                                new ArrayList<>() // Empty authorities
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContextHolder'a set et
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Request attributes'a da ekle (Controller'larda kullanmak için)
                request.setAttribute("userId", userInfo.getUserId());
                request.setAttribute("userEmail", userInfo.getEmail());
                request.setAttribute("isAuthenticated", true);

                System.out.println("✅ User authenticated and set to SecurityContext: " + userInfo.getEmail());
            } else {
                System.out.println("❌ Authentication failed");
            }
        } else {
            System.out.println("⚠️ No token provided");
        }

        filterChain.doFilter(request, response);
    }
}

