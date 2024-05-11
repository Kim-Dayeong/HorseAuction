package com.hoarse.auction.web.dto.horse;

import com.hoarse.auction.web.entity.horse.Horse;
import com.hoarse.auction.web.entity.member.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HorseupdateDto {
    private String name;
    private String furcolor;
    private Member owner;
    private Horse mother;
    private Horse father;
}
