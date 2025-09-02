package com.hoarse.auction.web.serviceImpl.member;

import com.hoarse.auction.web.config.jwt.JwtAuthenticationFilter;
import com.hoarse.auction.web.config.jwt.JwtConfig;
import com.hoarse.auction.web.dto.member.MemberDto;
import com.hoarse.auction.web.dto.member.MemberRequestDto;
import com.hoarse.auction.web.entity.member.Member;
import com.hoarse.auction.web.repository.member.MemberRepository;
import com.hoarse.auction.web.service.auth.AuthService;
import com.hoarse.auction.web.service.member.MemberService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final AuthService authService;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Override
    public MemberDto createUser(MemberRequestDto memberRequestDto) {
        Member member = memberRepository.save(
                Member.builder().password(bCryptPasswordEncoder.encode(memberRequestDto.getPassword())).name(memberRequestDto.getName()).username(memberRequestDto.getUsername()).role(memberRequestDto.getRole()).phone(memberRequestDto.getPhone()).build());
        return MemberDto.builder().id(member.getId()).name(member.getName()).password(member.getPassword()).role(member.getRole()).username(member.getUsername()).phone(member.getPhone()).build();
    }

    // 유저 객체 생성
    @Override
    public MemberDto findUser(String email) {
        Member member = Optional.ofNullable(memberRepository.findByUsername(email)).orElseThrow(() -> new BadCredentialsException("회원 정보를 찾을 수 없습니다."));
        return MemberDto.builder().id(member.getId()).password(member.getPassword()).phone(member.getPhone()).role(member.getRole()).username(member.getUsername()).build();
    }

    // 로그인
    public MemberDto findByEmailAndPassword(String email, String password) {
        Member member = Optional.ofNullable(memberRepository.findByUsername(email))
                .orElseThrow(() -> new BadCredentialsException("이메일이나 비밀번호를 확인해주세요."));

        // 사용자 정보만 가져오기 때문에 비밀번호 검증만 수행
        if (!bCryptPasswordEncoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // 사용자 정보 반환
        return MemberDto.builder()
                .id(member.getId())
                .password(member.getPassword())
                .role(member.getRole())
                .username(member.getUsername())
                .build();
    }

    // 사용자 탈퇴
    @Override
    public void deleteMember(String token) {
        Member member = authService.getMemberFromToken(token);


        System.out.println(member.getUsername()+"유저 정보가 탈퇴됩니다.");

        memberRepository.delete(member);
    }


    // 사용자 정보 수정
    @Override
    public MemberDto updateMember( MemberRequestDto requestDto,String username) {
        Member member = memberRepository.findByUsername(username);

        member.setName(requestDto.getName());
        member.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        member.setPhone(requestDto.getPhone());
        memberRepository.save(member);

        return MemberDto.builder()
                .id(member.getId())
                .name(member.getName())
                .username(member.getUsername())
                .role(member.getRole())
                .password(member.getPassword())
                .phone(member.getPhone())
                .build();

    }



    // 전체 유저 정보
    @Override
    public List<MemberDto> findAll() {
        return memberRepository.findAll().stream().map(u->MemberDto.builder().id(u.getId()).password(u.getPassword()).name(u.getName()).phone(u.getPhone()).role(u.getRole()).username(u.getUsername()).build()).collect(Collectors.toList());
    }

//    public MemberDto findByEmailAndPassword(String email, String password) {
//        Member member = Optional.ofNullable(memberRepository.findByUsername(email)).orElseThrow(()->new BadCredentialsException("이메일이나 비밀번호를 확인해주세요."));
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
//
//        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
//
//        if (bCryptPasswordEncoder.matches(password,member.getPassword()) == false) {
//            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
//        }
//
//        return MemberDto.builder().id(member.getId()).password(member.getPassword()).role(member.getRole()).username(member.getUsername()).build();
//    }







}

