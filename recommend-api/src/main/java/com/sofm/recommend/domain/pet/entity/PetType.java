package com.sofm.recommend.domain.pet.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "pet_pettype")
@Data
public class PetType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recordId;
    private String title;
    private String type; // 0主要类型 1次要类型
    private int pid;
    private int orderNum;
    private Date createTime;
    private Date lastModifyTime;
}
