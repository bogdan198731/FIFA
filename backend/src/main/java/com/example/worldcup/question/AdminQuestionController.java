package com.example.worldcup.question;

import com.example.worldcup.question.dto.TournamentQuestionResponse;
import com.example.worldcup.question.dto.UpdateQuestionOptionSourceRequest;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/questions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController {

    private final TournamentQuestionService service;

    public AdminQuestionController(TournamentQuestionService service) {
        this.service = service;
    }

    @PatchMapping("/{id}/option-source")
    public TournamentQuestionResponse updateOptionSource(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuestionOptionSourceRequest req) {
        return service.updateOptionSource(id, req.optionSource());
    }
}
