package youngju.seonbimind.classic.progress.dto;

public record ProgressResponse(
        Integer totalSolvedCount,
        Integer currentStreak,
        String currentRank,
        String nextRank,
        Integer remainingToNextRank
) {
}
