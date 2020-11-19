package com.lisetckiy.lab4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class StreamingService {


    @Autowired
    private FrameHandler frameHandler;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<WebSocketMessage<?>> mainQueue = new LinkedBlockingQueue<>();

    private final Map<String, LinkedBlockingQueue<WebSocketMessage<?>>> queues = new ConcurrentHashMap<>();
    private final Map<String, Thread> executors = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        new Thread(() ->
                   {
                       while (true) {
                           try {
                               WebSocketMessage<?> message = mainQueue.poll(50, TimeUnit.MILLISECONDS);
                               if (message != null)
                                   queues.forEach((id, queue) -> queue.add(message));
                           } catch (InterruptedException e) {
                               log.error(e.getMessage(), e);
                           }
                       }
                   }).start();
    }

    void initExecutor(String id) {
        final LinkedBlockingQueue<WebSocketMessage<?>> queue = queues.get(id);
        final WebSocketSession session = sessions.get(id);
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    WebSocketMessage<?> m = queue.poll(50, TimeUnit.MILLISECONDS);
                    if (m != null)
                        session.sendMessage(m);
                } catch (InterruptedException | IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        executors.put(id, thread);
        thread.start();
    }

    void addMessage(BinaryMessage message) {
        mainQueue.add(new BinaryMessage(frameHandler.convertAndConsume(message.getPayload())));
//        new BinaryMessage()
//        message.getPayload()
    }


    void addSession(WebSocketSession session) {
        if (!sessions.containsKey(session.getId())) {
            sessions.put(session.getId(), session);
            queues.put(session.getId(), new LinkedBlockingQueue<>());
            initExecutor(session.getId());
        }
    }

    void sendToAll(WebSocketMessage<?> message) {
        sessions.forEach((id, session) -> {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    void removeSession(WebSocketSession session) {
        executors.get(session.getId()).stop();
        executors.remove(session.getId());
        sessions.remove(session.getId());
        queues.remove(session.getId());
    }
}
