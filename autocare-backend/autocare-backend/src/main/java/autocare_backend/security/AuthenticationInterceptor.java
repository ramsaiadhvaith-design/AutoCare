package autocare_backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final SessionTokenService sessions;

    public AuthenticationInterceptor(SessionTokenService sessions) {
        this.sessions = sessions;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String authorization = request.getHeader("Authorization");
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring(7).trim() : null;
        if (!sessions.isValid(token)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Your session has expired. Please log in again.");
            return false;
        }

        String role = sessions.getRole(token);
        String path = request.getRequestURI();
        String method = request.getMethod();
        boolean allowed = isAllowed(role, path, method);
        if (!allowed) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Your account role does not have permission to perform this action.");
            return false;
        }
        request.setAttribute("authenticatedUserId", sessions.getUserId(token));
        request.setAttribute("authenticatedRole", role);
        return true;
    }

    private boolean isAllowed(String role, String path, String method) {
        if (role == null) return false;
        boolean customer = "CUSTOMER".equals(role);
        boolean mechanic = "MECHANIC".equals(role);

        if (path.startsWith("/api/vehicles")) {
            if ("GET".equals(method)) return customer || mechanic;
            return customer; // Only customers manage vehicle records.
        }
        if (path.equals("/api/bookings") || path.startsWith("/api/bookings/")) {
            if ("GET".equals(method)) return customer || mechanic;
            if ("POST".equals(method)) return customer;
            if ("PUT".equals(method) || "DELETE".equals(method)) return mechanic;
        }
        return false;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
