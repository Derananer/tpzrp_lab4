package com.lisetckiy.lab4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;

@Slf4j
public class CustomWebSocketHandler extends AbstractWebSocketHandler {

    private StreamingService streamingService;

    public CustomWebSocketHandler(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        log.info("New Text Message Received: ");
//        streamingService.addSession(session);
//        streamingService.sendToAll(message);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
        log.info("New Binary Message Received: ");
        streamingService.addMessage(message);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        log.info("New Connection establish session=" + session.getId());
        streamingService.addSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        log.info("Connection removed session=" + session.getId());
        streamingService.removeSession(session);
    }
}
