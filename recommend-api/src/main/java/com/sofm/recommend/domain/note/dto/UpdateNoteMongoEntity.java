package com.sofm.recommend.domain.note.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class UpdateNoteMongoEntity implements Serializable {
    private int recordId;
    private int uid;
    private int petId;
    private Integer petType;
    private boolean visibility;
    private List<String> topic;
    private int activityId;
    private boolean waitAdopt;
    private boolean checkType;
    private int noteScore;
    private int contentLength;
    private int videoDuration;
    private int imageNum;
    private String province;
    private String city;
    private String status;
    private Date createTime;
    private Date lastModifyTime;
    private double qualityPoint;
}
