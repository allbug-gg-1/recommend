package com.sofm.recommend.domain.family.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "pet_family")
@Data
public class Family implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recordId;
    private String name;
    private int petId;
    private int userId;
    private Date createTime;
    private Date lastModifyTime;
    private String status; // 0删除 1正常
    private String detail;
}
