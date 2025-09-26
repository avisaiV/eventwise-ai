package com.eventwise.reco.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RuleExplainer {

  private final OllamaClient ollama;
  private final String mode;

  public RuleExplainer(OllamaClient ollama,
                       @Value("${explainer.mode:rule}") String mode) {
    this.ollama = ollama;
    this.mode = mode;
  }

  private static final Map<String, String> CATEGORY_INSIGHTS = Map.of(
      "Tech",   "hands-on demos and builder networking",
      "Music",  "a strong live vibe and crowd energy",
      "Sports", "a competitive atmosphere and team spirit",
      "Career", "recruiters, resume tips, and mentors"
  );

  /** Build an explanation. If mode=llm, call Ollama; else rule-based. */
  public String explain(String category, String title, long eventId, long score) {
    String cat = (category == null || category.isBlank()) ? "General" : category;

    if ("llm".equalsIgnoreCase(mode)) {
      String system = "You are a helpful assistant creating ONE short, friendly recommendation sentence. " +
                      "Max 30 words. No emojis. Be specific and grounded in the data provided.";
      String user = """
          Explain why this event is recommended.

          Title: %s
          Category: %s
          Popularity score: %d

          Output: one single sentence.
          """.formatted(title, cat, score);

      return ollama.chat(system, user);
    }

    // fallback: rule-based
    String angle =
        (score >= 20) ? "It’s trending hard—lots of people are jumping on this." :
        (score >= 10) ? "Solid interest from others right now." :
        (score >= 5)  ? "Picking up momentum—worth a look." :
                        "New or niche—could be a hidden gem.";

    String insight = CATEGORY_INSIGHTS.getOrDefault(cat, "a good crowd and relevant content");
    return "%s Expect %s. If you’re into %s, this lines up.".formatted(angle, insight, cat.toLowerCase());
  }
}
