package com.sofm.recommend.domain.note.entity;

import com.sofm.recommend.domain.note.model.Note;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

import java.util.Date;
import java.util.List;

@Document(collection = "note")
@Data
public class NoteMongoEntity extends Note {

    @Id
    private int recordId;
    @Indexed
    private int uid;
    @Indexed
    private int petId;
    private Integer petType;
    @Indexed
    private boolean visibility;
    @Indexed
    private List<String> topic;
    @Indexed
    private int activityId;
    @Indexed
    private boolean waitAdopt;
    private boolean checkType;
    private int noteScore;
    private int contentLength;
    private int videoDuration;
    private int imageNum;
    @Indexed
    private String province;
    @Indexed
    private String city;
    @Indexed
    private String status;
    @Indexed
    private Date createTime;
    private Date lastModifyTime;
    private double qualityPoint;
}
