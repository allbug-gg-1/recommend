package com.sofm.recommend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendResult implements Serializable {
    private String recommend_id;
    private List<Integer> data;

    public static RecommendResult of(String recommend_id, List<Integer> data) {
        return new RecommendResult(recommend_id, data);
    }
}
