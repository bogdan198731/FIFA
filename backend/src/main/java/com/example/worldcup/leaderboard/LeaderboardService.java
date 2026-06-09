package com.example.worldcup.leaderboard;

import com.example.worldcup.user.User;
import com.example.worldcup.user.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;

    public LeaderboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the leaderboard ordered by total points (desc), then username
     * (asc) for stable display. Ranks follow the standard "competition"
     * convention — tied users share the same rank, and the next distinct
     * score jumps over the ties.
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard(boolean anonymous) {
        List<User> users = userRepository.findAllByOrderByTotalPointsDescUsernameAsc();
        List<LeaderboardEntry> result = new ArrayList<>(users.size());

        int rank = 0;
        Long lastScore = null;
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (lastScore == null || user.getTotalPoints() != lastScore) {
                rank = i + 1;
                lastScore = user.getTotalPoints();
            }
            result.add(new LeaderboardEntry(
                    rank,
                    anonymous ? null : user.getId(),
                    anonymous ? maskUsername(user.getUsername()) : user.getUsername(),
                    user.getTotalPoints()
            ));
        }
        return result;
    }

    private String maskUsername(String username) {
        if (username == null || username.isEmpty()) return "***";
        return Character.toUpperCase(username.charAt(0)) + "***";
    }
}
