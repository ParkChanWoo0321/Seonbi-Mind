package youngju.seonbimind.classic.problem.dto;

import java.util.List;

public record ProblemStartResponse(
        Long historyId,
        Long problemId,
        Long sentenceId,
        Boolean isTodaySentence,
        Boolean completed,
        Boolean reviewMode,
        Boolean inProgress,
        String originalText,
        String readingText,
        String meaning,
        List<String> shuffledWords
) {
}
