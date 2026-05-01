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
import youngju.seonbimind.classic.progress.dto.ProgressResponse;
import youngju.seonbimind.classic.progress.service.ProgressService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthMemberRepository authMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProgressService progressService;

    @Transactional
    public AuthResult signup(SignupRequest request) {
        String loginId = request.resolvedLoginId();
        requireNotBlank(request.name(), "name");
        requireNotBlank(loginId, "loginId");
        requireNotBlank(request.password(), "password");

        if (authMemberRepository.existsByLoginId(loginId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "loginId is already in use.");
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
        requireNotBlank(loginId, "loginId");
        requireNotBlank(request.password(), "password");

        AuthMember member = authMemberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "loginId or password is invalid."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "loginId or password is invalid.");
        }

        return createAuthResult(member);
    }

    public AuthResponse getMe(String accessToken) {
        String loginId = getCurrentLoginId();
        AuthMember member = authMemberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated member was not found."));

        return createAuthResponse(member, accessToken);
    }

    public long getRefreshTokenExpirationMs() {
        return jwtTokenProvider.getRefreshTokenExpirationMs();
    }

    private AuthResult createAuthResult(AuthMember member) {
        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member);
        AuthResponse response = createAuthResponse(member, accessToken);

        return new AuthResult(response, refreshToken);
    }

    private AuthResponse createAuthResponse(AuthMember member, String accessToken) {
        ProgressResponse progress = progressService.getProgress(member);
        var todaySentence = progressService.getTodaySentence(member);

        return new AuthResponse(
                member.getName(),
                accessToken,
                member.getLoginId(),
                progress.totalSolvedCount(),
                progress.currentStreak(),
                progress.currentRank(),
                progress.nextRank(),
                progress.remainingToNextRank(),
                todaySentence
        );
    }

    private String getCurrentLoginId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return authentication.getName();
    }

    private void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required.");
        }
    }
}
