package youngju.seonbimind.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import youngju.seonbimind.auth.entity.AuthMember;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final TypeReference<Map<String, Object>> PAYLOAD_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    public String createAccessToken(AuthMember member) {
        return createToken(member, ACCESS_TOKEN_TYPE, accessTokenExpirationMs);
    }

    public String createRefreshToken(AuthMember member) {
        return createToken(member, REFRESH_TOKEN_TYPE, refreshTokenExpirationMs);
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, ACCESS_TOKEN_TYPE);
    }

    public String getLoginIdFromAccessToken(String token) {
        Map<String, Object> payload = parseAndVerify(token);
        return String.valueOf(payload.get("loginId"));
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    private String createToken(AuthMember member, String tokenType, long expirationMs) {
        long now = System.currentTimeMillis();

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(member.getId()));
        payload.put("loginId", member.getLoginId());
        payload.put("name", member.getName());
        payload.put("type", tokenType);
        payload.put("iat", now);
        payload.put("exp", now + expirationMs);

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signature = sign(encodedHeader + "." + encodedPayload);

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    private boolean validateToken(String token, String expectedTokenType) {
        try {
            Map<String, Object> payload = parseAndVerify(token);
            String tokenType = String.valueOf(payload.get("type"));
            long expiresAt = getLong(payload.get("exp"));
            return expectedTokenType.equals(tokenType) && expiresAt > System.currentTimeMillis();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private Map<String, Object> parseAndVerify(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }

        try {
            byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(decodedPayload, PAYLOAD_TYPE);
        } catch (IllegalArgumentException | JacksonException exception) {
            throw new IllegalArgumentException("Invalid JWT payload", exception);
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to create JWT", exception);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(key);
            byte[] signature = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new IllegalStateException("Failed to sign JWT", exception);
        }
    }

    private long getLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
