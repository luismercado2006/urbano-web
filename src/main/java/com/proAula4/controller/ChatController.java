package com.proAula4.controller;

import com.proAula4.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/chat") // 🔥 ESTE ES EL IMPORTANTE
    public String chat(@RequestBody Map<String, String> body) {
        return chatService.responder(body.get("pregunta"));
    }
}