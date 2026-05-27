package com.example.worldcup.question;

import com.example.worldcup.question.dto.SubmitAnswerRequest;
import com.example.worldcup.question.dto.TournamentAnswerResponse;
import com.example.worldcup.question.dto.TournamentQuestionResponse;
import com.example.worldcup.question.dto.UpdateAnswerRequest;
import com.example.worldcup.user.UserPrincipal;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class TournamentQuestionController {

    private final TournamentQuestionService service;

    public TournamentQuestionController(TournamentQuestionService service) {
        this.service = service;
    }

    @GetMapping
    public List<TournamentQuestionResponse> list() {
        return service.listQuestions();
    }

    @GetMapping("/my-answers")
    public List<TournamentAnswerResponse> myAnswers(@AuthenticationPrincipal UserPrincipal principal) {
        return service.myAnswers(principal.getId());
    }

    @PostMapping("/answers")
    public ResponseEntity<TournamentAnswerResponse> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SubmitAnswerRequest req
    ) {
        TournamentAnswerResponse response = service.submit(principal.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/answers/{answerId}")
    public TournamentAnswerResponse update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long answerId,
            @Valid @RequestBody UpdateAnswerRequest req
    ) {
        return service.update(principal.getId(), answerId, req);
    }
}
