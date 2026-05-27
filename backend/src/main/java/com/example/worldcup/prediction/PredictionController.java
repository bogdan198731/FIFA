package com.example.worldcup.prediction;

import com.example.worldcup.prediction.dto.PredictionRequest;
import com.example.worldcup.prediction.dto.PredictionResponse;
import com.example.worldcup.prediction.dto.UpdatePredictionRequest;
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
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/my")
    public List<PredictionResponse> myPredictions(@AuthenticationPrincipal UserPrincipal principal) {
        return predictionService.myPredictions(principal.getId());
    }

    @PostMapping
    public ResponseEntity<PredictionResponse> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PredictionRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(predictionService.submit(principal.getId(), req));
    }

    @PutMapping("/{predictionId}")
    public PredictionResponse update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long predictionId,
            @Valid @RequestBody UpdatePredictionRequest req
    ) {
        return predictionService.update(principal.getId(), predictionId, req);
    }
}
