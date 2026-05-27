package com.example.worldcup.admin;

import com.example.worldcup.admin.dto.ScoreRecalculationResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/scores")
public class AdminScoreController {

    private final AdminScoreService adminScoreService;

    public AdminScoreController(AdminScoreService adminScoreService) {
        this.adminScoreService = adminScoreService;
    }

    @PostMapping("/recalculate")
    public ScoreRecalculationResponse recalculateAll() {
        return adminScoreService.recalculateAll();
    }

    @PostMapping("/recalculate/matches/{matchId}")
    public ScoreRecalculationResponse recalculateMatch(@PathVariable Long matchId) {
        return adminScoreService.recalculateMatch(matchId);
    }
}
