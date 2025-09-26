package com.eventwise.reco.ai;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/explanations")
public class ExplanationController {

    private final RuleExplainer explainer;
    private final PopularityReader popularity;
    private final EventClient eventClient;

    @Autowired
    public ExplanationController(RuleExplainer explainer,
                                 PopularityReader popularity,
                                 EventClient eventClient) {
        this.explainer = explainer;
        this.popularity = popularity;
        this.eventClient = eventClient;
    }

    @GetMapping
    public ResponseEntity<ExplanationResponse> explain(@RequestParam("eventId") long eventId) {
        // get score from popularity state
        long score = popularity.scoreOf(eventId);

        // fetch real event data
        var ev = eventClient.findById(eventId);
        String title = (ev != null && ev.title() != null) ? ev.title() : ("Event " + eventId);
        String category = (ev != null && ev.category() != null) ? ev.category() : "General";

        // build explanation
        String explanation = explainer.explain(category, title, eventId, score);

        var body = new ExplanationResponse(eventId, title, category, score, explanation);
        return ResponseEntity.ok(body);
    }

    /**
     * DTO for API response
     */
    public record ExplanationResponse(
            long eventId,
            String title,
            String category,
            long score,
            String explanation
    ) {}
}
