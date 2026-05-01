package youngju.seonbimind.auth.dto;

public record AuthResponse(
        String name,
        String accessToken,
        String loginId,
        Integer totalSolvedCount,
        Integer currentStreak,
        String currentRank,
        String nextRank,
        Integer remainingToNextRank,
        TodaySentenceResponse todaySentence
) {
}
