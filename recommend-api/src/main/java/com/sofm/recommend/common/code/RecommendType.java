package com.sofm.recommend.common.code;

import lombok.Getter;

@Getter
public enum RecommendType {
    Normal(0, "正常模式"),
    NearBy(1, "地区模式"),
    Random(2, "探索模式");

    private final int code;
    private final String text;

    RecommendType(int code, String text) {
        this.code = code;
        this.text = text;
    }

}
