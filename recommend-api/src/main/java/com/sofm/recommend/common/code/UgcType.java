package com.sofm.recommend.common.code;

import lombok.Getter;

@Getter
public enum UgcType {


    VIEWS("VIEWS", "曝光", 0d),
    CLICK("CLICK", "点击", 0.5d),
    LIKE("LIKE", "喜欢", 1d),
    CANCEL_LIKE("CLICK", "取消喜欢", 0d),
    COMMENT("COMMENT", "评论", 1d),
    SHARE("SHARE", "分享", 1d),
    DISLIKE_USER("DISLIKE_USER", "反馈-不喜欢用户", -0.5d),
    DISLIKE_PET("DISLIKE_PET", "反馈-不喜欢宠物", -0.5d),
    DISLIKE_ITEM("DISLIKE_ITEM", "反馈-不喜欢帖子", -0.5d),
    ;

    private final String code;
    private final String text;
    private final double weight;


    UgcType(String code, String text, double weight) {
        this.code = code;
        this.text = text;
        this.weight = weight;
    }

    public static double getWeightByCode(String action) {
        for (UgcType ugcType : UgcType.values()) {
            if (ugcType.code.equals(action)) {
                return ugcType.getWeight();
            }
        }
        return 0d;
    }
}
