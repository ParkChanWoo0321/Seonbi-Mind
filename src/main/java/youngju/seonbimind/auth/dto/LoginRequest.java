package youngju.seonbimind.auth.dto;

public record LoginRequest(
        String id,
        String loginId,
        String password
) {

    public String resolvedLoginId() {
        if (loginId != null && !loginId.isBlank()) {
            return loginId;
        }
        return id;
    }
}
