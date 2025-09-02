package com.hoarse.auction.web.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.hoarse.auction.web.dto.chat.ChatMessageDto;
import com.hoarse.auction.web.entity.chat.ChatMessage;
import com.hoarse.auction.web.repository.chat.ChatMessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;

    // 채팅 저장

    // 받아온 채팅 레디스에 저장해서 총 10개의 요소가 저장되면 한번 db에 저장하기
    public void saveChat(ChatMessage message) {

        String key = message.getRoomId();
        String json = convertToJson(message);
        // 왼쪽에 push
        stringRedisTemplate.opsForList().leftPush(key, json);
        // 채팅 갯수 확인
        Long chatCount = stringRedisTemplate.opsForList().size(key);
        if (chatCount >= 10) {
            saveChatMessageToDB(Long.valueOf(message.getRoomId()));
            stringRedisTemplate.delete(key); // 레디스에 저장됐던 메시지 삭제
        }
    }


    private String convertToJson(ChatMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("채팅 JSON 직렬화 실패", e);
        }
    }

    public void saveChatMessageToDB(Long roomId) {
        String key = String.valueOf(roomId);

        // Redis에서 리스트 가져오기
        List<String> messages = stringRedisTemplate.opsForList().range(key, 0, -1);

        if (messages != null && !messages.isEmpty()) {
            for (String json : messages) {
                try {
                    ChatMessage chatMessage = objectMapper.readValue(json, ChatMessage.class);
                    chatMessageRepository.save(chatMessage);
                } catch (JsonProcessingException e) {
                    // 로깅 처리 권장
                    System.err.println("JSON 파싱 에러: " + e.getMessage());
                }
            }
        }
    }

    //채팅 불러오기
    public List<ChatMessageDto> loadChat(String roomId) {
        List<ChatMessageDto> messageList = new ArrayList<>();

        // 1) DB에서 최근 50개 가져오기
        List<ChatMessage> chatMessageList =
                chatMessageRepository.findTop50ByRoomIdOrderByTimeDesc(roomId);

        for (ChatMessage chatMessage : chatMessageList) {
            messageList.add(new ChatMessageDto(chatMessage));
        }

        // 2) Redis에서 최근 10개 가져오기
        List<String> redisMessages = stringRedisTemplate.opsForList().range(roomId, 0, 10);

        if (redisMessages != null && !redisMessages.isEmpty()) {
            for (String redisMessage : redisMessages) {
                try {
                    ChatMessage chatMessage = objectMapper.readValue(redisMessage, ChatMessage.class);
                    messageList.add(new ChatMessageDto(chatMessage));
                } catch (JsonProcessingException e) {
                    // 로깅 권장
                    System.err.println("JSON 파싱 에러: " + e.getMessage());
                }
            }
        }

        return messageList;
    }
}


