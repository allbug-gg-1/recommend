package com.sofm.recommend.ui.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RecommendResponse implements Serializable {

    private String recommend_id;
    private List<Integer> data;
}
