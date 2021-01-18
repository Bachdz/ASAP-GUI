package com.example.demo.api;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController {


    @MessageMapping("/hello")
    @SendTo("/topic/public")
    public String greeting() throws Exception {
        return "test test";
    }

}