package com.hoarse.auction.web.service.auction;

import com.hoarse.auction.web.entity.auction.AuctionMessage;
import com.hoarse.auction.web.entity.auction.AuctionRoom;
import com.hoarse.auction.web.entity.horse.Horse;
import com.hoarse.auction.web.entity.member.Member;
import com.hoarse.auction.web.repository.Auction.AuctionRoomRepository;
import com.hoarse.auction.web.repository.hoarse.HorseRepository;
import com.hoarse.auction.web.repository.member.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuctionService {

    private static final String AUCTION_KEY = "auction";

    private final HorseRepository horseRepository;
    private final AuctionRoomRepository auctionRoomRepository;

    private final MemberRepository memberRepository;
    public static final long AUCTION_DURATION = TimeUnit.MINUTES.toMillis(1); // 1분


    private final String REDIS_HOST;
    private final int REDIS_PORT;

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    public AuctionService( //@allautowired 대신 직접 생성자
            HorseRepository horseRepository,
            AuctionRoomRepository auctionRoomRepository,
            MemberRepository memberRepository,
            @Value("${spring.redis.host}") String redisHost,
            @Value("${redis.port}") int redisPort) {
        this.horseRepository = horseRepository;
        this.auctionRoomRepository = auctionRoomRepository;
        this.memberRepository = memberRepository;
        this.REDIS_HOST = redisHost;
        this.REDIS_PORT = redisPort;
    }

    // 기본값 설정
    public void auctionInit(){
        Jedis jedis = getJedis();
        String key = "backupKey";
        String value = "300"; // 단위: 만원
        jedis.set(key,value);
        String endtimeKey = "endtime";
        long endtime = System.currentTimeMillis() + AUCTION_DURATION;
        jedis.set(endtimeKey, String.valueOf(endtime));
        jedis.close();
    }
//    @PostConstruct //bean 생성후 한번만 실행
//    public void init() {
//        auctionInit();
//    }

    private void auction(String value, AuctionMessage message) {

        AuctionRoom auctionRoom = auctionRoomRepository.findByRoomId(message.getRoomId());
        System.out.println("경매룸 아이디:"+message.getRoomId());

        if (auctionRoom == null) {
            logger.error("경매 방을 찾을 수 없습니다.");
            return;
        }

        Horse hoarse = auctionRoom.getHorse();

        try (Jedis jedis = getJedis()) {
            System.out.println("실행 분기 테스트");

            Long endTime = Long.parseLong(jedis.get("endTime"));

            if (System.currentTimeMillis() < endTime) {

                // 비교값 저장
                String key = AUCTION_KEY + ":" + hoarse.getName() ;
                jedis.set(key, value);
                String backupKey = "backupKey";
                String backupValue = value;
                jedis.set(backupKey,backupValue);
                System.out.println("sender:"+message.getUsername());
                System.out.println("Value saved in Redis: " + value);
                System.out.println(key);
                addChatMessage(message.getRoomId(), message.getUsername(), message.getMessage()); // 값 저장

            } else {
                System.out.println("옥션이 종료되었습니다 이후 값은 레디스 저장 안됨 ");
                System.out.println("==========낙찰되었습니다 낙찰정보 : "+ jedis.hgetAll(message.getRoomId()));


                Map<String, String> ownerInfoMap = jedis.hgetAll(message.getRoomId());
                String username = null;
                String finalvalue = null;
                // 모든 필드와 그에 해당하는 값 출력
                for (Map.Entry<String, String> entry : ownerInfoMap.entrySet()) {
                    username = entry.getKey();
                   finalvalue = entry.getValue();

                }
                System.out.println("낙찰자 아이디: " + username + ", 낙찰가: " + finalvalue);
                // 낙찰자 저장
                Member owner = memberRepository.findById(Long.valueOf(username))
                        .orElseThrow(() -> new IllegalArgumentException("해당하는 멤버를 찾을 수 없습니다."));
                hoarse.setOwner(owner);
                hoarse.setBidPrice(Long.valueOf(finalvalue));
                horseRepository.save(hoarse);

            }

        } catch (NumberFormatException e) {
            logger.error("Redis 연결 중 오류가 발생했습니다."+ e);
        }
    }

    public void addChatMessage(String roomId,String sender, String message) { // 낙찰 되었을때만 실행
        try (Jedis jedis = getJedis()) {

            jedis.hset(roomId, sender, message);// hash에 저장,계속 덮어씌움 키값:roomid
        }
    }


    // 값 비교 후 더 높은값 제시

    public void auctionCompare(AuctionMessage message) {
        try (Jedis jedis =getJedis()) {
            System.out.println("비교메서드!!!!!");

            long backupKey = 0;
            long value = 0;
                    String backupKeyString = jedis.get("backupKey");

            if (backupKeyString != null) {
                try {
                    backupKey = Long.parseLong(backupKeyString);
                   value = Long.parseLong(message.getMessage());

                    if (value > backupKey) {
                        auction(String.valueOf(value), message);
                    } else {
                        logger.error("이전값 보다 더 높은값을 제시해야 합니다.");
                    }
                } catch (NumberFormatException e) {
                    logger.error("유효한 숫자가 아닙니다.");
                }
            } else {
                logger.error("backupKey의 값이 null입니다.");
            }
        }
    }



    public List<String> getChatMessages(String roomId, int start, int end) {
        try (Jedis jedis = getJedis()) {
            String roomKey = roomId;
            return jedis.lrange(roomKey, start, end); // 지정된 범위의 채팅 메시지 가져오기
        }
    }

    private Jedis getJedis() {
        return new Jedis(REDIS_HOST, REDIS_PORT);
    }

}