package com.hoarse.auction.web.module;

import java.util.*;

public class RandomMaker {


    public static String func() {

        String[] charArray = {"가", "나", "다", "라", "마", "바", "사", "아", "자", "차", "카", "타", "파", "하"};

        Random random = new Random();
        int randomIndex = random.nextInt(charArray.length);

        double randomValue = Math.random();


        return charArray[randomIndex] + (int) (randomValue * 100000);

    }
}

