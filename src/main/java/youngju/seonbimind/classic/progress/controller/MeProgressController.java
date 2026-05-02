package youngju.seonbimind.classic.progress.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import youngju.seonbimind.classic.progress.dto.ProblemHistoryResponse;
import youngju.seonbimind.classic.progress.service.ProgressService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class MeProgressController {

    private final ProgressService progressService;

    @GetMapping("/problem-history")
    public ResponseEntity<List<ProblemHistoryResponse>> getProblemHistory() {
        return ResponseEntity.ok(progressService.getMyProblemHistory());
    }
}
