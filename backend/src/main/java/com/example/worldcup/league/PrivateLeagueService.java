package com.example.worldcup.league;

import com.example.worldcup.league.dto.PrivateLeagueResponse;
import com.example.worldcup.leaderboard.LeaderboardEntry;
import com.example.worldcup.user.User;
import com.example.worldcup.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PrivateLeagueService {

    private static final String INVITE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_LENGTH = 8;

    private final PrivateLeagueRepository leagueRepo;
    private final LeagueMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final SecureRandom random = new SecureRandom();

    public PrivateLeagueService(PrivateLeagueRepository leagueRepo,
                                LeagueMemberRepository memberRepo,
                                UserRepository userRepo) {
        this.leagueRepo = leagueRepo;
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public PrivateLeagueResponse create(String name, Long ownerId) {
        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String code = generateUniqueCode();
        PrivateLeague league = leagueRepo.save(new PrivateLeague(name, code, owner));
        memberRepo.save(new LeagueMember(league, owner));

        return toResponse(league, ownerId, 1);
    }

    @Transactional
    public PrivateLeagueResponse join(String inviteCode, Long userId) {
        PrivateLeague league = leagueRepo.findByInviteCode(inviteCode.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid invite code"));

        if (memberRepo.existsByLeagueIdAndUserId(league.getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already a member of this league");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        memberRepo.save(new LeagueMember(league, user));
        int count = memberRepo.countByLeagueId(league.getId());
        return toResponse(league, userId, count);
    }

    @Transactional(readOnly = true)
    public List<PrivateLeagueResponse> getMyLeagues(Long userId) {
        return memberRepo.findByUserIdWithLeague(userId).stream()
                .map(m -> {
                    PrivateLeague l = m.getLeague();
                    int count = memberRepo.countByLeagueId(l.getId());
                    return toResponse(l, userId, count);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeagueLeaderboard(Long leagueId, Long requesterId) {
        if (!leagueRepo.existsById(leagueId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "League not found");
        }
        if (!memberRepo.existsByLeagueIdAndUserId(leagueId, requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this league");
        }

        Set<Long> memberIds = memberRepo.findByLeagueId(leagueId).stream()
                .map(m -> m.getUser().getId())
                .collect(Collectors.toSet());

        List<User> users = userRepo.findAllByIdInOrderByTotalPointsDescUsernameAsc(memberIds);

        List<LeaderboardEntry> result = new ArrayList<>(users.size());
        int rank = 0;
        Long lastScore = null;
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (lastScore == null || u.getTotalPoints() != lastScore) {
                rank = i + 1;
                lastScore = u.getTotalPoints();
            }
            result.add(new LeaderboardEntry(rank, u.getId(), u.getUsername(), u.getTotalPoints()));
        }
        return result;
    }

    private PrivateLeagueResponse toResponse(PrivateLeague l, Long requesterId, int memberCount) {
        boolean isOwner = l.getOwner().getId().equals(requesterId);
        return new PrivateLeagueResponse(
                l.getId(), l.getName(), l.getInviteCode(),
                l.getOwner().getUsername(), isOwner, memberCount, l.getCreatedAt()
        );
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(INVITE_LENGTH);
            for (int i = 0; i < INVITE_LENGTH; i++) {
                sb.append(INVITE_CHARS.charAt(random.nextInt(INVITE_CHARS.length())));
            }
            code = sb.toString();
        } while (leagueRepo.existsByInviteCode(code));
        return code;
    }
}
