package youngju.seonbimind.classic.problem.dto;

import java.util.List;

public record ProblemStartResponse(
        Long problemId,
        Long sentenceId,
        Boolean isTodaySentence,
        String originalText,
        String readingText,
        String meaning,
        List<String> shuffledWords
) {
}
