package com.hoarse.auction.web.entity.role;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum Role {


    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");
    private String value;
}
