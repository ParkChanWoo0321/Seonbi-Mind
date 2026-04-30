package youngju.seonbimind.classic.problem.dto;

public record ProblemAnswerResponse(
        Long problemId,
        Boolean correct,
        String nextStep,
        Boolean completed,
        Boolean gptCorrect,
        String gptReason
) {
}
