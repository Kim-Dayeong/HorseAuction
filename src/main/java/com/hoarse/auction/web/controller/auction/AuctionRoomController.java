package com.hoarse.auction.web.controller.auction;

import com.hoarse.auction.web.entity.auction.AuctionRoom;
import com.hoarse.auction.web.service.auction.AuctionRoomService;
import com.hoarse.auction.web.serviceImpl.member.MemberTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/auction")
@Tag(name = "경매방 API")
public class AuctionRoomController {

    private final AuctionRoomService auctionRoomService;
    private final MemberTokenService memberTokenService;


    @Operation(summary = "경매방 생성 API")
    @PostMapping("/room")
    @ResponseBody
    public AuctionRoom createRoom(
            @RequestParam String roomName,
            @RequestParam String hoarseId, HttpServletRequest request) {


        return auctionRoomService.createRoom(roomName, hoarseId, memberTokenService.getUsername(request));
    }

    //로그인 사용자 정보 가져오기


//    @Operation(summary = "특정 경매방 조회 API")
//    @GetMapping("/room/{roomId}")
//    @ResponseBody
//    public AuctionRoom roomInfo(@PathVariable String roomId) {
//        return auctionRoomService.findById(roomId);
//    }

//    @Operation(summary = "경매 리스트 화면 API")
//    @GetMapping("/room")
//    public String rooms(Model model) {
//        return "/auction/roomlist";
//    }

    //    @Operation(summary = "경매방 입장 API")
//    @GetMapping("/room/enter/{roomId}")
//    public String roomDetail(Model model, @PathVariable String roomId) {
//        model.addAttribute("roomId", roomId);
//
//        return "/auction/roomdetail";
//    }

    //    @Operation(summary = "모든 경매방 리스트 API")
//    @GetMapping("/rooms")
//    @ResponseBody
//    public List<AuctionRoom> room() {
//        return auctionRoomService.findAllRoom();
//    }


}


