package com.sofm.recommend.domain.model.mongo;

import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Document(collection = "recommend_log")
@Data
public class RecommendLog implements Serializable {
    @Id
    private String id;
    @Indexed
    private int uid;
    @Indexed
    private int recommendType;
    @Indexed
    private Date createTime;
    private Map<String, Set<String>> channelItem;
}
