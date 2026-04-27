package youngju.seonbimind.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import youngju.seonbimind.auth.dto.AuthResponse;
import youngju.seonbimind.auth.dto.AuthResult;
import youngju.seonbimind.auth.dto.LoginRequest;
import youngju.seonbimind.auth.dto.SignupRequest;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.auth.jwt.JwtTokenProvider;
import youngju.seonbimind.auth.repository.AuthMemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthMemberRepository authMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResult signup(SignupRequest request) {
        String loginId = request.resolvedLoginId();
        requireNotBlank(request.name(), "이름");
        requireNotBlank(loginId, "로그인 ID");
        requireNotBlank(request.password(), "비밀번호");

        if (authMemberRepository.existsByLoginId(loginId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 로그인 ID입니다.");
        }

        AuthMember member = AuthMember.create(
                request.name(),
                loginId,
                passwordEncoder.encode(request.password())
        );

        return createAuthResult(authMemberRepository.save(member));
    }

    public AuthResult login(LoginRequest request) {
        String loginId = request.resolvedLoginId();
        requireNotBlank(loginId, "로그인 ID");
        requireNotBlank(request.password(), "비밀번호");

        AuthMember member = authMemberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 ID 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 ID 또는 비밀번호가 올바르지 않습니다.");
        }

        return createAuthResult(member);
    }

    public AuthResponse getMe(String accessToken) {
        String loginId = getCurrentLoginId();
        AuthMember member = authMemberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보를 찾을 수 없습니다."));

        return new AuthResponse(member.getName(), accessToken, member.getLoginId());
    }

    public long getRefreshTokenExpirationMs() {
        return jwtTokenProvider.getRefreshTokenExpirationMs();
    }

    private AuthResult createAuthResult(AuthMember member) {
        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);
        AuthResponse response = new AuthResponse(member.getName(), accessToken, member.getLoginId());

        return new AuthResult(response, refreshToken);
    }

    private String getCurrentLoginId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return authentication.getName();
    }

    private void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "을(를) 입력해주세요.");
        }
    }
}
