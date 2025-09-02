package com.hoarse.auction.web.controller.auction;

import com.hoarse.auction.web.config.jwt.JwtConfig;
import com.hoarse.auction.web.entity.auction.AuctionMessage;
import com.hoarse.auction.web.entity.chat.ChatMessage;
import com.hoarse.auction.web.entity.member.Member;
import com.hoarse.auction.web.repository.member.MemberRepository;
import com.hoarse.auction.web.service.auction.AuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.source.internal.hbm.XmlElementMetadata;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.awt.print.PrinterGraphics;
import java.security.Principal;
import java.util.concurrent.TimeUnit;

import static com.hoarse.auction.web.service.auction.AuctionService.AUCTION_DURATION;


@RestController
@RequiredArgsConstructor
@Tag(name = "경매 API")
public class AuctionMessageController {
    private final SimpMessageSendingOperations sendingOperations;
    private final AuctionService auctionService;

    private final StringRedisTemplate stringRedisTemplate; // 주입받음

    public static final long auctionDuringtime = TimeUnit.MINUTES.toMillis(1); // 1분

    private final JwtConfig jwtConfig;
    private final MemberRepository memberRepository;


    @MessageMapping("/auction/message")
    @Operation(summary = "경매 진행 API")
    public void enter(AuctionMessage message) {
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            message.setMessage(message.getUsername()+ "님이 입장하셨습니다.");
        }
        System.out.println(message.getMessage());

        //토큰
       // String token = jwtConfig.getAuthentication(message.getToken()).getName();
       // Member member = memberRepository.findById(Long.valueOf(token))
        //        .orElseThrow(() -> new IllegalArgumentException("해당하는 멤버를 찾을 수 없습니다."));

        if(message.getMessage().equals("경매시작")){
            handleAuctionStart(message);
        }
        sendingOperations.convertAndSend("/topic/auction/room/" + message.getRoomId(), message);
        // 값 비교
        auctionService.auctionCompare(message);

    }


    private void handleAuctionStart(AuctionMessage message){
        System.out.println("구문 이퀄 확인!!!");
        long endTime = System.currentTimeMillis() + AuctionService.AUCTION_DURATION;
        stringRedisTemplate.opsForValue().set("endTime", String.valueOf(endTime));
    }

}
