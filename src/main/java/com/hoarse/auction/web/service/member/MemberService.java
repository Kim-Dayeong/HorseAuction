package com.hoarse.auction.web.service.member;

import com.hoarse.auction.web.dto.member.MemberDto;
import com.hoarse.auction.web.dto.member.MemberRequestDto;
import com.hoarse.auction.web.entity.member.Member;

import java.util.List;

public interface MemberService {

    MemberDto createUser(MemberRequestDto memberRequestDto);

    MemberDto findUser(String email);

    MemberDto findByEmailAndPassword(String email, String password);

    List<MemberDto> findAll();

    MemberDto updateMember( MemberRequestDto requestDto, String username);

   void deleteMember(String token);
}
