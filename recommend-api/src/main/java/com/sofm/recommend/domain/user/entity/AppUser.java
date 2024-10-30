package com.sofm.recommend.domain.user.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "pet_appuser")
@Data
public class AppUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recordId;
    private String openId;
    private String phoneNumber;
    private String nickName;
    private String avatar;
    private String gender;
    private String saying;
    private String status = "1"; // 0为正常 1为锁定
    private Date createTime;
    private Date lastModifyTime = new Date();
    private Date lastLoginTime;
    private Long ip;
    private String cityCode;
    private String interests;
    private int pid;
    private int noteId;
    private int activityId;
    private String homeName;
    private int homeId;
    private int bindUid;
    private Date registerTime;

}
