package app.schoolbully.service;

import app.schoolbully.model.entity.Signal;
import app.schoolbully.model.entity.User;
import app.schoolbully.model.enums.IssueType;
import app.schoolbully.model.enums.RecommendedAction;
import app.schoolbully.model.enums.SignalSource;
import app.schoolbully.repository.SignalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignalService {
    private final SignalRepository signalRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.api.key:}")
    private String openAiApiKey;

    // Your exact system prompt
    private static final String SYSTEM_PROMPT = """
You are "AI School Safety Assistant" – an empathetic, safe, child-friendly AI moderator designed to help students recognize early signs of bullying, aggression, and unsafe situations in a school environment (online or offline). Your role is twofold:

1) Provide the student with a supportive, clear, psychologically safe, and practically helpful response.
2) Return structured JSON data for classification, logging, and escalation.

Your audience: children aged 8–18.  
Your tone: warm, safe, calm, non-judgmental, supportive.  
Your priority: student well-being, safety, privacy.

----------------------------------------------------------
INPUT:
The user (student) sends a short description of a situation.
----------------------------------------------------------

TASKS:

A) **ANALYZE AND CLASSIFY THE SIGNAL**
Extract and classify the incident in the following categories:

1. **Source of problem** (choose one):
   - "student_to_student"
   - "teacher_to_student"
   - "parent_to_student"
   - "unknown"

2. **Type of issue** (choose one):
   - "physical_aggression"
   - "verbal_bullying"
   - "cyberbullying"
   - "social_exclusion"
   - "harassment"
   - "discriminatory_behavior"
   - "power_abuse"
   - "other"

3. **Urgency level** (1–5)
   1 = no urgency  
   5 = immediate risk of harm

4. **Severity level** (1–5)
   1 = mild conflict  
   5 = severe aggression, risk of trauma or violence

5. **Importance level** (1–5)
   1 = low impact  
   5 = critical issue requiring adult intervention

6. **Credibility / likelihood the report is real** (1–5)
   1 = likely made up or joking  
   5 = highly credible and consistent

7. **Recommended action for specialists**  
   Short classification of what should happen:  
   - "monitor"  
   - "talk_to_student"  
   - "notify_counselor"  
   - "notify_teacher"  
   - "notify_school_psychologist"  
   - "urgent_intervention"

B) **GENERATE A SUPPORTIVE STUDENT RESPONSE**
Provide the student with a supportive, clear, psychologically safe, and practically helpful response in Bulgarian.

Your message to the student MUST:

a) Be empathetic, warm, and non-judgmental.
   - Start by acknowledging the student's feelings in a kind and validating way.
   - Make the student feel heard, understood, and safe.

b) Offer practical, age-appropriate advice for handling the situation right now.
   Examples:
   - Suggest safe ways to remove themselves from the situation.
   - Offer simple strategies to calm down or manage stress.
   - Suggest talking to a trusted adult (parent, teacher, school counselor) if appropriate.
   - Help them identify what is safe and unsafe behavior.

c) Provide psychological support.
   Examples:
   - Normalize their feelings ("It's okay to feel upset/confused.").
   - Reinforce that they are not alone.
   - Encourage self-confidence and healthy boundaries.

d) Give clear and simple preventive strategies for the future,
   even if the current incident is mild or ambiguous.
   Examples:
   - What they can do if someone behaves badly toward them again.
   - How to respond without escalating or entering conflict.
   - How to ask for help safely.
   - How to recognize early warning signs of bullying.

e) Keep the message short, supportive, and actionable:
   - 3 to 6 sentences total.
   - No complex psychological terminology.
   - No blaming or criticism.
   - No confrontation-based instructions.
   - No legal, medical, or disciplinary advice.

f) Encourage emotional safety and resilience:
   - Remind the student they did the right thing by sharing.
   - Remind them that their feelings matter.
   - Empower them gently with choices and safe next steps.

C) **OUTPUT FORMAT**
Return **one combined JSON object**, containing:

{
  "student_response": "<text to show to the student>",
  "classification": {
    "source": "",
    "type": "",
    "urgency": 1–5,
    "severity": 1–5,
    "importance": 1–5,
    "credibility": 1–5,
    "recommended_action": ""
  }
}

Make sure the JSON is valid and contains no additional text outside the JSON.
""";

    @Transactional
    public Signal createSignal(String bodyText, boolean anonymous, 
                              String studentName, String studentPhoneNumber, 
                              User student) {
        Signal signal = new Signal();
        signal.setBodyText(bodyText);
        signal.setAnonymous(anonymous);
        signal.setStudent(student);
        
        if (!anonymous) {
            // If not anonymous, store the student's name and phone
            signal.setStudentName(studentName);
            signal.setStudentPhoneNumber(studentPhoneNumber);
        } else {
            // If anonymous, explicitly set to null (don't store any personal info)
            signal.setStudentName(null);
            signal.setStudentPhoneNumber(null);
        }

        // Analyze with AI
        analyzeSignalWithAI(signal);

        return signalRepository.save(signal);
    }

    private void analyzeSignalWithAI(Signal signal) {
        try {
            if (openAiApiKey == null || openAiApiKey.isEmpty()) {
                log.warn("OpenAI API key not configured, using fallback");
                analyzeWithFallback(signal);
                return;
            }

            // Call OpenAI API using RestTemplate
            String url = "https://api.openai.com/v1/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o"); // Using gpt-4o as in your example
            
            // System message with your exact prompt
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT);
            
            // User message with the signal text
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", signal.getBodyText());
            
            requestBody.put("messages", List.of(systemMessage, userMessage));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 500);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                parseAIResponse(signal, response.getBody());
            } else {
                log.warn("OpenAI API call failed, status: {}, using fallback", response.getStatusCode());
                analyzeWithFallback(signal);
            }
            
        } catch (Exception e) {
            log.error("Error analyzing signal with AI", e);
            analyzeWithFallback(signal);
        }
    }

    private void parseAIResponse(Signal signal, String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode choices = rootNode.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).path("message").path("content").asText();
                
                // Extract JSON from the response (might be wrapped in markdown code blocks)
                content = content.replaceAll("```json", "").replaceAll("```", "").trim();
                
                JsonNode aiResponse = objectMapper.readTree(content);
                
                // Parse student_response
                String studentResponse = aiResponse.path("student_response").asText("");
                signal.setStudentResponse(studentResponse);
                signal.setAiAdvice(studentResponse); // For backward compatibility
                
                // Parse classification
                JsonNode classification = aiResponse.path("classification");
                if (!classification.isMissingNode()) {
                    String sourceStr = classification.path("source").asText("unknown");
                    String typeStr = classification.path("type").asText("other");
                    int urgency = Math.max(1, Math.min(5, classification.path("urgency").asInt(1)));
                    int severity = Math.max(1, Math.min(5, classification.path("severity").asInt(1)));
                    int importance = Math.max(1, Math.min(5, classification.path("importance").asInt(1)));
                    int credibility = Math.max(1, Math.min(5, classification.path("credibility").asInt(1)));
                    String actionStr = classification.path("recommended_action").asText("monitor");
                    
                    signal.setSource(parseSource(sourceStr));
                    signal.setType(parseIssueType(typeStr));
                    signal.setUrgency(urgency);
                    signal.setSeverity(severity);
                    signal.setImportance(importance);
                    signal.setCredibility(credibility);
                    signal.setRecommendedAction(parseRecommendedAction(actionStr));
                    
                    // Calculate seriousnessScore for backward compatibility (severity * 20)
                    signal.setSeriousnessScore(severity * 20);
                }
            } else {
                log.warn("No choices in AI response, using fallback");
                analyzeWithFallback(signal);
            }
        } catch (Exception e) {
            log.error("Error parsing AI response", e);
            analyzeWithFallback(signal);
        }
    }

    private void analyzeWithFallback(Signal signal) {
        // Simple fallback analysis - set default values
        signal.setSource(SignalSource.unknown);
        signal.setType(IssueType.other);
        signal.setUrgency(2);
        signal.setSeverity(2);
        signal.setImportance(2);
        signal.setCredibility(3);
        signal.setRecommendedAction(RecommendedAction.monitor);
        signal.setSeriousnessScore(40);
        signal.setStudentResponse("Благодарим за сигнала. Моля, свържете се с доверен възрастен или учител за помощ.");
        signal.setAiAdvice(signal.getStudentResponse());
    }

    private SignalSource parseSource(String sourceStr) {
        if (sourceStr == null || sourceStr.isEmpty()) return SignalSource.unknown;
        try {
            return SignalSource.valueOf(sourceStr.toLowerCase().trim());
        } catch (Exception e) {
            return SignalSource.unknown;
        }
    }

    private IssueType parseIssueType(String typeStr) {
        if (typeStr == null || typeStr.isEmpty()) return IssueType.other;
        try {
            return IssueType.valueOf(typeStr.toLowerCase().trim());
        } catch (Exception e) {
            return IssueType.other;
        }
    }

    private RecommendedAction parseRecommendedAction(String actionStr) {
        if (actionStr == null || actionStr.isEmpty()) return RecommendedAction.monitor;
        try {
            return RecommendedAction.valueOf(actionStr.toLowerCase().trim());
        } catch (Exception e) {
            return RecommendedAction.monitor;
        }
    }

    public List<Signal> getAllSignals() {
        return signalRepository.findAllByOrderByCreatedOnDesc();
    }

    public Signal findById(UUID id) {
        return signalRepository.findById(id).orElse(null);
    }

    public List<Signal> getSignalsForTeacher() {
        return signalRepository.findByRecommendedActionOrderedByUrgencyAndSeverity(
            RecommendedAction.notify_teacher
        ).stream()
        .limit(10)
        .toList();
    }
    
    public List<Signal> getSignalsForTeacher(boolean authorityNotified) {
        return signalRepository.findByRecommendedActionAndAuthorityNotifiedOrderedByUrgencyAndSeverity(
            RecommendedAction.notify_teacher,
            authorityNotified
        ).stream()
        .limit(50)
        .toList();
    }

    @Transactional
    public Signal markAsNotified(UUID signalId, String notifiedBy) {
        Signal signal = signalRepository.findById(signalId)
            .orElseThrow(() -> new IllegalArgumentException("Signal not found"));
        
        signal.setAuthorityNotified(true);
        signal.setNotifiedAuthority(notifiedBy);
        
        return signalRepository.save(signal);
    }
}

