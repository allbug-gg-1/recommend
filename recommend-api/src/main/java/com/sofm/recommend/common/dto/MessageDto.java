package com.sofm.recommend.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MessageDto implements Serializable {

    private Integer UserId;
    private String type;
    private List<String> itemsId;
    private Long timestamp;
    private String recommend_id;

}
