package com.hoarse.auction.web.controller.member;

import com.hoarse.auction.web.config.jwt.JwtConfig;
import com.hoarse.auction.web.config.security.SecurityUser;
import com.hoarse.auction.web.dto.horse.HorseResponseDto;
import com.hoarse.auction.web.dto.jwt.JwtResponseDTO;
import com.hoarse.auction.web.dto.member.LoginDto;
import com.hoarse.auction.web.dto.member.MemberDto;
import com.hoarse.auction.web.dto.member.MemberRequestDto;
import com.hoarse.auction.web.dto.member.updateResponseMemberDto;
import com.hoarse.auction.web.service.auth.AuthService;
import com.hoarse.auction.web.service.member.MemberService;
import com.hoarse.auction.web.service.redis.TokenService;
import com.hoarse.auction.web.serviceImpl.hoarse.HorseServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


@Slf4j
@RestController
@RequestMapping("api/members")
@RequiredArgsConstructor
@Tag(name = "회원 API")
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;
    private final JwtConfig jwtConfig;
    private final HorseServiceImpl horseService;
    private final TokenService tokenService;

    private final PasswordEncoder passwordEncoder;



    @Operation(summary = "회원가입 API")
    @PostMapping("/signup")
    public MemberDto createUser(@RequestBody MemberRequestDto memberRequestDto) {
        return memberService.createUser(memberRequestDto);
    }


    @Operation(summary = "로그인 API")
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@RequestBody LoginDto loginDto) {
        // 사용자 정보 확인 및 토큰 생성
        MemberDto member = memberService.findByEmailAndPassword(loginDto.getUsername(), loginDto.getPassword());

        JwtResponseDTO jwtResponse = jwtConfig.createToken(member.getUsername(),
                Collections.singletonList(member.getRole().getValue()));

        // 레디스에 토큰 저장
        tokenService.saveRefreshToken(member.getUsername(), jwtResponse.getRefreshToken(), 7 * 24 * 60 * 60 * 1000); // 7일
        // 토큰은 헤더로 응답
        return ResponseEntity.ok(jwtResponse);
    }


    //회원 수정
    @Operation(summary = "회원정보 수정")
    @PutMapping("")
    public ResponseEntity updateMember(MemberRequestDto memberRequestDto,
                                       @AuthenticationPrincipal SecurityUser principal){

        String username = principal.getUsername(); //로그인 정보 가져오기

        // 회원정보 수정
        memberRequestDto.setPassword(passwordEncoder.encode(memberRequestDto.getPassword()));
        memberService.updateMember(memberRequestDto, username);
        return new ResponseEntity(HttpStatus.OK);

    }



//    @Operation(summary = "어드민 - 모든 회원 조회 API")
//    @GetMapping("/admin")
//    @PreAuthorize("isAuthenticated() and hasRole('ROLE_ADMIN')")
//    public List<MemberDto> findAllUser() {
//        return memberService.findAll();
//    }
//



    @Operation(summary = "로그인 회원 정보 API")
    @GetMapping("/my")
    public String findUser(@RequestHeader(AUTHORIZATION)String token) {
        if (token == null) {
            throw new BadCredentialsException("회원 정보를 찾을 수 없습니다.(로그인 안됨)");
        }
        String member = authService.getMemberFromToken(token).getUsername();
        return member;
    }


    @Operation(summary = "회원 소유 말목록 엑셀 내보내기 API")
    @GetMapping("/my/horse/exel")
    public void horseExport(HttpServletResponse response) throws IOException {  //엑셀로 말 목록 내보내기


        List<HorseResponseDto>  horseList =  horseService.hoarseList();

        Workbook wb = new XSSFWorkbook();

        Sheet sheet = wb.createSheet("내 소유 말 목록");
        Row row = null;
        Cell cell = null;
        int rowNum = 0;

        // Header
        row = sheet.createRow(rowNum++);
        cell = row.createCell(0);
        cell.setCellValue("번호");
        cell = row.createCell(1);
        cell.setCellValue("마 명");
        cell = row.createCell(2);
        cell.setCellValue("소유주");
        cell = row.createCell(3);
        cell.setCellValue("생산자");
        cell = row.createCell(4);
        cell.setCellValue("고유번호");


        // Body
        for(int i=0; i<horseList.size(); i++){

                row = sheet.createRow(rowNum++);
                cell = row.createCell(0);
                HorseResponseDto horse =  horseList.get(i);
            cell.setCellValue(i);
            cell = row.createCell(1);
            cell.setCellValue(horse.getName());
            cell = row.createCell(2);
            if(horse.getOwner() != null){
                cell.setCellValue(String.valueOf(horse.getOwner().getName()));
            }
            cell.setCellValue("");
            cell = row.createCell(3);
            if(horse.getProducer() != null){
                cell.setCellValue(String.valueOf(horse.getProducer().getName()));
            }
            cell.setCellValue("");

            cell = row.createCell(4);
            cell.setCellValue(String.valueOf(horse.getUniqueNum()));

        }


        // 컨텐츠 타입과 파일명 지정
        response.setContentType("ms-vnd/excel");
//        response.setHeader("Content-Disposition", "attachment;filename=example.xls");
        response.setHeader("Content-Disposition", "attachment;filename=example.xlsx");

        // Excel File Output
        wb.write(response.getOutputStream());
        wb.close();
    }



    // 회원탈퇴
    @Operation(summary = "회원 탈퇴 API")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMember(@RequestHeader(AUTHORIZATION)String token){
        if (token == null) {
            throw new BadCredentialsException("회원 정보를 찾을 수 없습니다.(로그인 안됨)");
        }

       memberService.deleteMember(token);

       return ResponseEntity.ok().build();
    }



}
