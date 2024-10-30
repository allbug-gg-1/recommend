package com.sofm.recommend.common.dto;

import com.sofm.recommend.common.code.RecommendType;
import lombok.Data;

import java.io.Serializable;

@Data
public class RecommendContext implements Serializable {

    private int userId;
    private RecommendType recommendType;
    private String ip;
    private ProvinceCityDto provinceCityDto;
    private UserMab userMab;

    public RecommendContext(int userId, String ip, ProvinceCityDto provinceCityDto) {
        this.userId = userId;
        this.ip = ip;
        this.provinceCityDto = provinceCityDto;
    }

    public RecommendContext() {
    }
}
