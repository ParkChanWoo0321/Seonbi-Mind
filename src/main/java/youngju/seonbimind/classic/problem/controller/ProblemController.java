package youngju.seonbimind.classic.problem.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import youngju.seonbimind.classic.problem.dto.AnswerRequest;
import youngju.seonbimind.classic.problem.dto.ProblemAnswerResponse;
import youngju.seonbimind.classic.problem.dto.ProblemStartRequest;
import youngju.seonbimind.classic.problem.dto.ProblemStartResponse;
import youngju.seonbimind.classic.problem.service.ProblemService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping("/start")
    public ResponseEntity<ProblemStartResponse> startProblem(@RequestBody ProblemStartRequest request) {
        return ResponseEntity.ok(problemService.startProblem(request));
    }

    @PostMapping("/order-answer/{problemId}")
    public ResponseEntity<ProblemAnswerResponse> submitOrderAnswer(
            @PathVariable Long problemId,
            @RequestBody AnswerRequest request
    ) {
        return ResponseEntity.ok(problemService.submitOrderAnswer(problemId, getAnswer(request)));
    }

    @PostMapping("/copy-typing/{problemId}")
    public ResponseEntity<ProblemAnswerResponse> submitCopyTypingAnswer(
            @PathVariable Long problemId,
            @RequestBody AnswerRequest request
    ) {
        return ResponseEntity.ok(problemService.submitCopyTypingAnswer(problemId, getAnswer(request)));
    }

    @PostMapping("/blind-typing/{problemId}")
    public ResponseEntity<ProblemAnswerResponse> submitBlindTypingAnswer(
            @PathVariable Long problemId,
            @RequestBody AnswerRequest request
    ) {
        return ResponseEntity.ok(problemService.submitBlindTypingAnswer(problemId, getAnswer(request)));
    }

    private String getAnswer(AnswerRequest request) {
        return request == null ? null : request.answer();
    }
}
