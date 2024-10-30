package com.sofm.recommend.ui.dto.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class RecommendRequest implements Serializable {
    private int user_id;
    private int page;
    private boolean near_by;
    private String ip;
}
