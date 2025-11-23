package app.schoolbully.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication != null && authentication.isAuthenticated()) {
            var userDetails = (CustomUserDetails) authentication.getPrincipal();
            var role = userDetails.getUser().getRole();
            
            // Redirect based on role
            if (role == app.schoolbully.model.enums.Role.Teacher) {
                response.sendRedirect("/teacher");
            } else {
                response.sendRedirect("/signal");
            }
        } else {
            response.sendRedirect("/signal");
        }
    }
}

