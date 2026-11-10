package com.polytechnique.ds.gestioncatalogue5dsgr1.controller;

import com.polytechnique.ds.gestioncatalogue5dsgr1.tools.ProduitTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final ProduitTools produitTools;

    public ChatController(
            ChatClient.Builder chatclient,
            ChatMemory chatMemory,
            ProduitTools productTools) {
        this.produitTools = produitTools;

        this.chatClient = chatclient
                .defaultAdvisors(advisors -> advisors
                        .advisors(
                                MessageChatMemoryAdvisor.builder(chatMemory).build()
                        )
                        .param(ChatMemory.CONVERSATION_ID, "default")
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

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
    @PostMapping("/chat/confirm")
    public String confirmProduct(@RequestParam String reference) {
        return produitTools.confirmProduct(reference);
    }
}