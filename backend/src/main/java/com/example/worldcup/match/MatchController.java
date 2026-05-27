package com.example.worldcup.match;

import com.example.worldcup.match.dto.MatchResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public List<MatchResponse> list() {
        return matchService.listAll();
    }

    @GetMapping("/upcoming")
    public List<MatchResponse> upcoming() {
        return matchService.listUpcoming();
    }
}
