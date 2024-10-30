package com.sofm.recommend.domain.model.mysql;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "pet_pet")
@Data
public class Pet implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recordId;
    private int uid;
    private String nickName;
    private String avatar;
    private Date age;
    private String gender;
    private int type;
    private Date createTime;
    private Date lastModifyTime;
    private int color;
    private String status; //0 删除 1正常 2迁移
    private int familyId;
    private int deleteFromHome;
    private int fromAdmin;
    private String uuid;
    private int is_migration;
}
