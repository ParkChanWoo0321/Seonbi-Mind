package youngju.seonbimind.classic.gpt.dto;

public record MeaningEvaluationResult(
        Boolean correct,
        String reason
) {

    public boolean isCorrect() {
        return Boolean.TRUE.equals(correct);
    }
}
