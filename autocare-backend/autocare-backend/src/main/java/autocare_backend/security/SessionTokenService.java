package autocare_backend.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionTokenService {
    private static final long SESSION_LIFETIME_SECONDS = 12 * 60 * 60;
    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public String create(Long userId, String role) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Session(userId, role, Instant.now().getEpochSecond() + SESSION_LIFETIME_SECONDS));
        return token;
    }

    public boolean isValid(String token) {
        return getSession(token) != null;
    }

    public String getRole(String token) {
        Session session = getSession(token);
        return session == null ? null : session.role();
    }

    public Long getUserId(String token) {
        Session session = getSession(token);
        return session == null ? null : session.userId();
    }

    private Session getSession(String token) {
        if (token == null || token.isBlank()) return null;
        Session session = sessions.get(token);
        if (session == null) return null;
        if (session.expiresAt() < Instant.now().getEpochSecond()) {
            sessions.remove(token);
            return null;
        }
        return session;
    }

    public void revoke(String token) {
        if (token != null) sessions.remove(token);
    }

    private record Session(Long userId, String role, long expiresAt) {}
}
