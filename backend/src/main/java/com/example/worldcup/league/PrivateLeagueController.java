package com.example.worldcup.league;

import com.example.worldcup.league.dto.CreateLeagueRequest;
import com.example.worldcup.league.dto.JoinLeagueRequest;
import com.example.worldcup.league.dto.PrivateLeagueResponse;
import com.example.worldcup.leaderboard.LeaderboardEntry;
import com.example.worldcup.user.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
public class PrivateLeagueController {

    private final PrivateLeagueService leagueService;

    public PrivateLeagueController(PrivateLeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @GetMapping
    public List<PrivateLeagueResponse> myLeagues(@AuthenticationPrincipal UserPrincipal principal) {
        return leagueService.getMyLeagues(principal.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrivateLeagueResponse create(@Valid @RequestBody CreateLeagueRequest request,
                                        @AuthenticationPrincipal UserPrincipal principal) {
        return leagueService.create(request.name(), principal.getId());
    }

    @PostMapping("/join")
    public PrivateLeagueResponse join(@Valid @RequestBody JoinLeagueRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return leagueService.join(request.inviteCode(), principal.getId());
    }

    @GetMapping("/{id}/leaderboard")
    public List<LeaderboardEntry> leaderboard(@PathVariable Long id,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        return leagueService.getLeagueLeaderboard(id, principal.getId());
    }
}
