package autocare_backend.controller;

import autocare_backend.model.UserAccount;
import autocare_backend.repository.UserAccountRepository;
import autocare_backend.security.PasswordHasher;
import autocare_backend.security.SessionTokenService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserAccountRepository users;
    private final SessionTokenService sessions;

    public AuthController(UserAccountRepository users, SessionTokenService sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String fullName = clean(request.fullName());
        String email = clean(request.email()).toLowerCase();
        String password = request.password() == null ? "" : request.password();

        if (fullName.isBlank() || fullName.length() > 100) {
            return error(HttpStatus.BAD_REQUEST, "Enter a name of 1 to 100 characters.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches() || email.length() > 190) {
            return error(HttpStatus.BAD_REQUEST, "Enter a valid email address.");
        }
        if (password.length() < 8 || password.length() > 100) {
            return error(HttpStatus.BAD_REQUEST, "Password must be between 8 and 100 characters.");
        }
        if (users.existsByEmailIgnoreCase(email)) {
            return error(HttpStatus.CONFLICT, "An account with this email already exists. Please log in.");
        }

        try {
            UserAccount saved = users.save(
                    new UserAccount(fullName, email, PasswordHasher.hash(password)));
            return ResponseEntity.status(HttpStatus.CREATED).body(toLoginResponse(saved));
        } catch (DataIntegrityViolationException exception) {
            return error(HttpStatus.CONFLICT, "An account with this email already exists. Please log in.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String email = clean(request.email()).toLowerCase();
        String password = request.password() == null ? "" : request.password();

        return users.findByEmailIgnoreCase(email)
                .filter(user -> PasswordHasher.verify(password, user.getPasswordHash()))
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(toLoginResponse(user)))
                .orElseGet(() -> error(HttpStatus.UNAUTHORIZED, "Email or password is incorrect."));
    }

    private Map<String, Object> toLoginResponse(UserAccount user) {
        // Never return the stored password hash to the browser.
        return Map.of("id", user.getId(), "fullName", user.getFullName(),
                "email", user.getEmail(), "role", user.getRole(), "token", sessions.create(user.getId(), user.getRole()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            sessions.revoke(authorization.substring(7).trim());
        }
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public record RegisterRequest(String fullName, String email, String password) {}
    public record LoginRequest(String email, String password) {}
}
