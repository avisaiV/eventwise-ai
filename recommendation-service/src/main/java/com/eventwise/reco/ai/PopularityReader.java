package com.eventwise.reco.ai;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PopularityReader {

    private final RestClient http = RestClient.builder()
            .baseUrl("http://localhost:8084")
            .build();

    public long scoreOf(long eventId) {
        // 1) Try the envelope shape: { "value": [ {eventId, score}, ... ], "Count": N }
        try {
            RecommendationsResponse resp = http.get()
                    .uri("/recommendations?limit=100")
                    .retrieve()
                    .body(RecommendationsResponse.class);

            if (resp != null && resp.value != null) {
                return resp.value.stream()
                        .filter(r -> r.eventId != null && r.eventId == eventId)
                        .map(r -> r.score == null ? 0L : r.score)
                        .findFirst()
                        .orElse(0L);
            }
        } catch (Exception ignored) {
            // fall through to array shape
        }

        // 2) Fallback: bare array shape: [ {eventId, score}, ... ]
        try {
            List<Rec> list = http.get()
                    .uri("/recommendations?limit=100")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Rec>>() {});

            if (list != null) {
                return list.stream()
                        .filter(r -> r.eventId != null && r.eventId == eventId)
                        .map(r -> r.score == null ? 0L : r.score)
                        .findFirst()
                        .orElse(0L);
            }
        } catch (Exception ignored) {
            // give up and return 0
        }

        return 0L;
    }

    // --- DTOs for both shapes ---
    public static final class RecommendationsResponse {
        public List<Rec> value;
        public Integer Count;
    }

    public static final class Rec {
        public Long eventId;
        public Long score;

        public Rec() {}
        public Rec(Long eventId, Long score) {
            this.eventId = eventId;
            this.score = score;
        }
    }
}
