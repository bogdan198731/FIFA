package com.example.worldcup.question;

import com.example.worldcup.common.ApiException;
import com.example.worldcup.match.MatchRepository;
import com.example.worldcup.player.PlayerRepository;
import com.example.worldcup.question.dto.SubmitAnswerRequest;
import com.example.worldcup.question.dto.TournamentAnswerResponse;
import com.example.worldcup.question.dto.TournamentQuestionResponse;
import com.example.worldcup.question.dto.UpdateAnswerRequest;
import com.example.worldcup.user.User;
import com.example.worldcup.user.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

@Service
public class TournamentQuestionService {

    private final TournamentQuestionRepository questionRepository;
    private final TournamentAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    public TournamentQuestionService(TournamentQuestionRepository questionRepository,
                                     TournamentAnswerRepository answerRepository,
                                     UserRepository userRepository,
                                     MatchRepository matchRepository,
                                     PlayerRepository playerRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<TournamentQuestionResponse> listQuestions() {
        Instant now = Instant.now();
        return questionRepository.findAll().stream()
                .sorted(Comparator.comparing(TournamentQuestion::getDeadline)
                        .thenComparing(TournamentQuestion::getId))
                .map(q -> TournamentQuestionResponse.from(q, now, resolveOptions(q)))
                .toList();
    }

    private List<String> resolveOptions(TournamentQuestion q) {
        if (q.getOptionSource() == null || q.getOptionSource() == OptionSource.STATIC) {
            return q.getOptions();
        }
        if (q.getOptionSource() == OptionSource.TEAMS) {
            TreeSet<String> teams = new TreeSet<>(matchRepository.findAllDistinctHomeTeams());
            teams.addAll(matchRepository.findAllDistinctAwayTeams());
            return List.copyOf(teams);
        }
        return switch (q.getOptionSource()) {
            case PLAYERS             -> playerRepository.findAllByOrderByNameAsc()
                                            .stream().map(p -> p.getName()).toList();
            case PLAYERS_GOALKEEPERS -> playerRepository.findByPositionOrderByNameAsc("Goalkeeper")
                                            .stream().map(p -> p.getName()).toList();
            case PLAYERS_DEFENDERS   -> playerRepository.findByPositionOrderByNameAsc("Defender")
                                            .stream().map(p -> p.getName()).toList();
            case PLAYERS_MIDFIELDERS -> playerRepository.findByPositionOrderByNameAsc("Midfielder")
                                            .stream().map(p -> p.getName()).toList();
            case PLAYERS_ATTACKERS   -> playerRepository.findByPositionOrderByNameAsc("Attacker")
                                            .stream().map(p -> p.getName()).toList();
            default                  -> q.getOptions();
        };
    }

    @Transactional(readOnly = true)
    public List<TournamentAnswerResponse> myAnswers(Long userId) {
        Instant now = Instant.now();
        return answerRepository.findByUserId(userId).stream()
                .map(a -> TournamentAnswerResponse.from(a, now))
                .toList();
    }

    @Transactional
    public TournamentAnswerResponse submit(Long userId, SubmitAnswerRequest req) {
        TournamentQuestion question = questionRepository.findById(req.questionId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));

        Instant now = Instant.now();
        ensureNotLocked(question, now);

        if (answerRepository.findByUserIdAndQuestionId(userId, question.getId()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "An answer already exists for this question — update it instead");
        }

        String trimmed = req.answer().trim();
        ensureValidOption(question, trimmed);

        User user = userRepository.getReferenceById(userId);
        TournamentAnswer answer = new TournamentAnswer(user, question, trimmed);
        answerRepository.save(answer);

        return TournamentAnswerResponse.from(answer, now);
    }

    @Transactional
    public TournamentAnswerResponse update(Long userId, Long answerId, UpdateAnswerRequest req) {
        TournamentAnswer existing = answerRepository.findById(answerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Answer not found"));

        if (!Objects.equals(existing.getUser().getId(), userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Cannot edit another user's answer");
        }

        Instant now = Instant.now();
        ensureNotLocked(existing.getQuestion(), now);

        String trimmed = req.answer().trim();
        ensureValidOption(existing.getQuestion(), trimmed);

        existing.setAnswer(trimmed);
        return TournamentAnswerResponse.from(existing, now);
    }

    private void ensureNotLocked(TournamentQuestion question, Instant now) {
        if (!now.isBefore(question.getDeadline())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This question is locked — its deadline has passed");
        }
    }

    @Transactional
    public TournamentQuestionResponse updateOptionSource(Long questionId, OptionSource source) {
        TournamentQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
        question.setOptionSource(source);
        if (source != OptionSource.STATIC) {
            question.setOptions(null);
        }
        return TournamentQuestionResponse.from(question, Instant.now(), resolveOptions(question));
    }

    private void ensureValidOption(TournamentQuestion question, String answer) {
        List<String> options = resolveOptions(question);
        if (options != null && !options.isEmpty() && !options.contains(answer)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Answer must be one of the available options");
        }
    }
}
