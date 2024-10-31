package com.sofm.recommend.common.code;

import lombok.Getter;

@Getter
public enum RecommendType {
    Exploit(0, "利用模式"),
    NearBy(1, "地区模式"),
    Explore(2, "探索模式");

    private final int code;
    private final String text;

    RecommendType(int code, String text) {
        this.code = code;
        this.text = text;
    }

}
