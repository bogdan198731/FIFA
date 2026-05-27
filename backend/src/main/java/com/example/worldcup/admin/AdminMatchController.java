package com.example.worldcup.admin;

import com.example.worldcup.admin.dto.AdminMatchRequest;
import com.example.worldcup.admin.dto.MatchResultRequest;
import com.example.worldcup.match.dto.MatchResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/matches")
public class AdminMatchController {

    private final AdminMatchService adminMatchService;

    public AdminMatchController(AdminMatchService adminMatchService) {
        this.adminMatchService = adminMatchService;
    }

    @PostMapping
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody AdminMatchRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminMatchService.create(req));
    }

    @PutMapping("/{matchId}")
    public MatchResponse update(@PathVariable Long matchId,
                                @Valid @RequestBody AdminMatchRequest req) {
        return adminMatchService.update(matchId, req);
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> delete(@PathVariable Long matchId) {
        adminMatchService.delete(matchId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{matchId}/result")
    public MatchResponse updateResult(@PathVariable Long matchId,
                                      @Valid @RequestBody MatchResultRequest req) {
        return adminMatchService.updateResult(matchId, req);
    }

    @PutMapping("/{matchId}/lock")
    public MatchResponse lock(@PathVariable Long matchId) {
        return adminMatchService.lock(matchId);
    }

    @PutMapping("/{matchId}/unlock")
    public MatchResponse unlock(@PathVariable Long matchId) {
        return adminMatchService.unlock(matchId);
    }
}
