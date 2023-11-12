package rcm.rcmarket.config.token;

import lombok.RequiredArgsConstructor;
import rcm.rcmarket.handler.JwtHandler;

@RequiredArgsConstructor
public class TokenHelper {
    private final JwtHandler jwtHandler;
    private final String key;
    private final long maxAgeSeconds;

    public String createToken(String subject) {
        return jwtHandler.createToken(key, subject, maxAgeSeconds);
    }

    public boolean validate(String token) {
        return jwtHandler.validate(key, token);
    }

    public String extractSubject(String token) {
        return jwtHandler.extractSubject(key, token);
    }
}
