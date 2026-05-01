package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityAiService {
    private final GeminiService geminiService;

    public String generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("Gemini Response!: " + aiResponse);
        processAiResponse(activity, aiResponse);
        return aiResponse;
    }

    private void processAiResponse( Activity activity,String aiResponse) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(aiResponse);

            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String jsonContent=textNode.asText()
                    .replaceAll("```json\\n","")
                    .replaceAll("\\```","")
                    .trim();
           // log.info("Parsed Response from ai: " + jsonContent);

            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode =analysisJson.path("analysis");

            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis,analysisNode,"overall","Overall:");
            addAnalysisSection(fullAnalysis,analysisNode,"pace","Pace:");
            addAnalysisSection(fullAnalysis,analysisNode,"heartRate","HeartRate:");
            addAnalysisSection(fullAnalysis,analysisNode,"caloriesBurned","CaloriesBurned:");

            List<String> improvements = extractImprovements(analysisJson);

        }catch(Exception e) {
            e.printStackTrace();
        }




    }

    private List<String> extractImprovements(JsonNode analysisJson) {
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {


        if(!analysisNode.path(key).isMissingNode()){
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");

        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations in the following JSON format:

{
  "analysis": {
    "overall": "Overall analysis here",
    "pace": "Pace analysis here",
    "heartRate": "Heart rate analysis here",
    "caloriesBurned": "Calories analysis here"
  },
  "improvements": [
    {
      "area": "Area name",
      "recommendation": "Detailed recommendation"
    }
  ],
  "suggestions": [
    {
      "workout": "Workout name",
      "description": "Detailed workout description"
    }
  ],
  "safety": [
    "Safety point 1",
    "Safety point 2"
  ]
}

Analyze this activity:
Activity Type: %s
Duration: %d minutes
Calories Burned: %d
Additional Metrics: %s

Provide detailed analysis focusing on performance, improvements, next steps.
Ensure the response follows the EXACT JSON format shown above.
""",
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );


    }
}
