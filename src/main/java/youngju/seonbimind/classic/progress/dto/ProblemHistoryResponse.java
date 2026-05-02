package youngju.seonbimind.classic.progress.dto;

import java.time.LocalDateTime;
import youngju.seonbimind.classic.progress.entity.SolvedProblemHistory;

public record ProblemHistoryResponse(
        Long historyId,
        String originalText,
        String readingText,
        String meaning,
        String userAnswer,
        Boolean correct,
        String explanation,
        LocalDateTime solvedAt
) {

    public static ProblemHistoryResponse from(SolvedProblemHistory history) {
        return new ProblemHistoryResponse(
                history.getId(),
                history.getOriginalText(),
                history.getReadingText(),
                history.getMeaning(),
                history.getUserBlindTypingAnswer(),
                history.isCorrect(),
                getExplanation(history),
                history.getSolvedAt()
        );
    }

    private static String getExplanation(SolvedProblemHistory history) {
        if (history.getGptReason() != null && !history.getGptReason().isBlank()) {
            return history.getGptReason();
        }

        String explanation = history.getExplanation();
        if (explanation == null || explanation.isBlank()) {
            return explanation;
        }

        String prefix = "GPT reason:";
        int prefixIndex = explanation.indexOf(prefix);
        if (prefixIndex >= 0) {
            return explanation.substring(prefixIndex + prefix.length()).trim();
        }

        return explanation;
    }
}
