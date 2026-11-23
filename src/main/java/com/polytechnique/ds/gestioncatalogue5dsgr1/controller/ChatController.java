
package com.polytechnique.ds.gestioncatalogue5dsgr1.controller;

import com.polytechnique.ds.gestioncatalogue5dsgr1.tools.ProduitTools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.retry.NonTransientAiException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final ProduitTools produitTools;

    public ChatController(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            SyncMcpToolCallbackProvider mcpTools,
            ProduitTools produitTools) {

        this.produitTools = produitTools;

        this.chatClient = chatClientBuilder

                // Chat memory
                .defaultAdvisors(advisors -> advisors
                        .advisors(
                                MessageChatMemoryAdvisor
                                        .builder(chatMemory)
                                        .build()
                        )
                )

                // Tools exposed by the MCP server
                .defaultTools(mcpTools)

                // System prompt
                .defaultSystem("""
                        Tu es un assistant e-commerce pour
                        la boutique Polytechnique Sousse.

                        Règles strictes :

                        - Réponds uniquement aux questions
                          relatives aux produits du catalogue.

                        - Utilise systématiquement les outils
                          disponibles pour vérifier les prix,
                          les quantités et les disponibilités.

                        - Si la question concerne un autre sujet,
                          réponds exactement :
                          Je ne sais pas.

                        - N'invente jamais les informations
                          concernant les produits.

                        - Réponds en français de manière
                          claire et concise.
                        """)

                .build();
    }

    // Endpoint Angular chatbot
    @GetMapping("/agent")
    public String chat(
            @RequestParam String message,
            @RequestParam(required = false) String conversationId) {

        String resolvedConversationId =
                resolveConversationId(conversationId);

        return chatClient.prompt()
                .advisors(advisors -> advisors
                        .param(
                                ChatMemory.CONVERSATION_ID,
                                resolvedConversationId
                        )
                )
                .user(message)
                .call()
                .content();
    }

    // Legacy local product confirmation
    @PostMapping("/chat/confirm")
    public String confirmProduct(
            @RequestParam String reference) {

        return produitTools.confirmProduct(reference);
    }

    // Mistral exception handling
    @ExceptionHandler(NonTransientAiException.class)
    public ResponseEntity<String> handleAiException(
            NonTransientAiException exception) {

        if (isRateLimit(exception)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(
                            "La limite de requêtes Mistral est atteinte. "
                                    + "Veuillez réessayer plus tard."
                    );
        }

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(
                        "Le service Mistral est temporairement indisponible."
                );
    }

    // Individual conversation management
    private String resolveConversationId(
            String conversationId) {

        if (conversationId == null
                || conversationId.isBlank()) {

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

    // Detect API rate limiting
    private boolean isRateLimit(
            NonTransientAiException exception) {

        for (Throwable cause = exception;
             cause != null;
             cause = cause.getCause()) {

            String message = cause.getMessage();

            if (message != null) {

                String normalizedMessage =
                        message.toLowerCase(Locale.ROOT);

                if (normalizedMessage.contains("429")
                        || normalizedMessage.contains("rate limit")) {

                    return true;
                }
            }
        }

        return false;
    }
}
