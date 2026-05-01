package youngju.seonbimind.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import youngju.seonbimind.auth.entity.AuthMember;
import youngju.seonbimind.auth.repository.AuthMemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentMemberService {

    private final AuthMemberRepository authMemberRepository;

    public AuthMember getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }

        return authMemberRepository.findByLoginId(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated member was not found."));
    }
}
