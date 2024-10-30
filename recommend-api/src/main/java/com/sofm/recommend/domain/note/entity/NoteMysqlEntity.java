package com.sofm.recommend.domain.note.entity;

import com.sofm.recommend.domain.note.model.Note;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "pet_note")
@Data
public class NoteMysqlEntity extends Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recordId;
    private int uid;
    private int petId;
    private String content;
    private String otherContent;
    private String visibility; // 0公开 1自己可见
    private String cityCode;
    private String url;
    private String topic;
    private String atId;
    private Date createTime;
    private Date lastModifyTime;
    private int commentNum;
    private long likedNum;
    private String type; // 0图文 1视频
    private int ranking;
    private int activityId;
    private long voteNum;
    private int adopt;
    private int adoptNum;
    private String neuter;
    private String vaccine;
    private String health;
    private String adoptionConditions;
    private int commentId;
    private String checkType; // 0通过 1为通过
    private int label;
    private int noteScore;
    private String changeScoreType; // 02 符合app-100 05 广告-100
    private int videoDuration;
    private String status; // 0删除 1正常
    private int fromAdmin;
    private String pictureInfo;

}
