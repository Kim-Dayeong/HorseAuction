package com.hoarse.auction.web.serviceImpl.member;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;


@Service
public class MemberTokenService {


    // 로그인 사용자 정보 가져오기
    public String getUsername(HttpServletRequest request) {
        String refreshToken = request.getHeader("Authorization"); // 헤더에서 토큰 가져오기

        if (refreshToken == null || refreshToken.isEmpty()) {
            return "Unauthorized";
        }

        String username = getUsertoken(refreshToken);
        if (username == null) {
            return "Unauthorized";
        }

        return username;
    }

    // 헤더에서 리프레쉬 토큰 정보 가져오기 (로그인 사용자)
    public String getUsertoken(String refreshToken) {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            // Redis에서 모든 refreshToken 키 검색
            for (String key : jedis.keys("refreshToken:*")) {
                if (refreshToken.equals(jedis.get(key))) {
                    // 키에서 username 추출
                    return key.replace("refreshToken:", "");
                }
            }
        }
        return null; // 일치하는 토큰이 없는 경우
    }
}
