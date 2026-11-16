package com.polytechnique.ds.gestioncatalogue5dsgr1.controller;

import com.polytechnique.ds.gestioncatalogue5dsgr1.tools.ProduitTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final ProduitTools produitTools;

    public ChatController(
            ChatClient.Builder chatclient,
            ChatMemory chatMemory,
            ProduitTools productTools) {
        this.produitTools = productTools;

        this.chatClient = chatclient
                .defaultAdvisors(advisors -> advisors
                        .advisors(
                                MessageChatMemoryAdvisor.builder(chatMemory).build()
                        )
                )
                .defaultTools(productTools)
                .defaultSystem("defaultmessage= \"\"\"\n" +
                        "Tu es un assistant e- commerce d’elite pour la boutique Polytechnique Sousse Regles strictes que tu dois imperativement respecter :\n" +
                        "- Reponds UNIQUEMENT aux questions relatives aux produits du catalogue .\n" +
                        "- Utilise SYSTEMATIQUEMENT tes outils pour verifier les prix et disponibilites .\n" +
                        "- Si la question concerne un autre sujet , reponds exactement ’Je ne sais pas ’\n" +
                        "- Ne divulgue jamais de fausses informations inventees .\n" +
                        "\"\"\"")
                .build();
    }

    @GetMapping("/agent")
    public String chat(
            @RequestParam String message,
            @RequestParam(required = false) String conversationId) {
        String resolvedConversationId = resolveConversationId(conversationId);
        return chatClient.prompt()
                .advisors(advisors -> advisors
                        .param(ChatMemory.CONVERSATION_ID, resolvedConversationId))
                .user(message)
                .call()
                .content();
    }
    @PostMapping("/chat/confirm")
    public String confirmProduct(@RequestParam String reference) {
        return produitTools.confirmProduct(reference);
    }

    @ExceptionHandler(NonTransientAiException.class)
    public ResponseEntity<String> handleAiException(NonTransientAiException exception) {
        if (isRateLimit(exception)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("La limite de requêtes du service Mistral est atteinte. Réessayez plus tard.");
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("Le service Mistral est temporairement indisponible.");
    }

    private String resolveConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        if (!conversationId.matches("[A-Za-z0-9._-]{1,128}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Identifiant de conversation invalide."
            );
        }

        return conversationId;
    }

    private boolean isRateLimit(NonTransientAiException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            String message = cause.getMessage();
            if (message != null) {
                String normalizedMessage = message.toLowerCase(Locale.ROOT);
                if (normalizedMessage.contains("429")
                        || normalizedMessage.contains("rate limit")) {
                    return true;
                }
            }
        }
        return false;
    }
}