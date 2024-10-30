package com.sofm.recommend.domain.activity.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "pet_activity")
@Data
public class Activity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recordId;
    private String majorTitle;
    private String minorTitle;
    private String type; // 0日常 1普通
    private String detail1;
    private String detail2;
    private String detail3;
    private String detail4;
    private String detail5;
    private String bgImg;

    private String showImg;
    private String wayText;
    private String awardText;
    private String receiveText;
    private String statement;
    private int voteNum;
    private int noteCount;
    private int clickCount;
    private int userCount;
    private Date startTime;
    private Date endTime;
    private Date createTime;
    private Date lastModifyTime;
    private String status; // 0删除 1正常

    private int orderNum;

    private int shareNum;

    private int showTop;

    private String shareImg;

    private String topImg;

    private int finished;

    private int winners;
}
