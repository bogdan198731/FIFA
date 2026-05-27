package com.example.worldcup.question;

import com.example.worldcup.common.ApiException;
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

@Service
public class TournamentQuestionService {

    private final TournamentQuestionRepository questionRepository;
    private final TournamentAnswerRepository answerRepository;
    private final UserRepository userRepository;

    public TournamentQuestionService(TournamentQuestionRepository questionRepository,
                                     TournamentAnswerRepository answerRepository,
                                     UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TournamentQuestionResponse> listQuestions() {
        Instant now = Instant.now();
        return questionRepository.findAll().stream()
                .sorted(Comparator.comparing(TournamentQuestion::getDeadline)
                        .thenComparing(TournamentQuestion::getId))
                .map(q -> TournamentQuestionResponse.from(q, now))
                .toList();
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

        User user = userRepository.getReferenceById(userId);
        TournamentAnswer answer = new TournamentAnswer(user, question, req.answer().trim());
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

        existing.setAnswer(req.answer().trim());
        return TournamentAnswerResponse.from(existing, now);
    }

    private void ensureNotLocked(TournamentQuestion question, Instant now) {
        if (!now.isBefore(question.getDeadline())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This question is locked — its deadline has passed");
        }
    }
}
