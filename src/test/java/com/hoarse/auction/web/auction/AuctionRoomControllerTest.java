package com.hoarse.auction.web.auction;

import com.hoarse.auction.web.controller.auction.AuctionRoomController;
import com.hoarse.auction.web.entity.auction.AuctionRoom;
import com.hoarse.auction.web.service.auction.AuctionRoomService;
import com.hoarse.auction.web.service.redis.TokenService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static reactor.core.publisher.Mono.when;

@WebMvcTest(AuctionRoomController.class)
public class AuctionRoomControllerTest {


}
