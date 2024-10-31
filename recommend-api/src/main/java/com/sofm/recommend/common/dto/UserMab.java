package com.sofm.recommend.common.dto;

import com.sofm.recommend.application.config.UserMabWeightConfig;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
public class UserMab {
    private double hot;
    private double cache;
    private double random;
    private double region;
    private double social;
    private double news;
    private double interest;

    public UserMab() {
        this.hot = UserMabWeightConfig.DEFAULT_HOT_WEIGHT;
        this.cache = UserMabWeightConfig.DEFAULT_CACHE_WEIGHT;
        this.region = UserMabWeightConfig.DEFAULT_REGION_WEIGHT;
        this.social = UserMabWeightConfig.DEFAULT_SOCIAL_WEIGHT;
        this.news = UserMabWeightConfig.DEFAULT_NEWS_WEIGHT;
        this.interest = UserMabWeightConfig.DEFAULT_INTEREST_WEIGHT;
        this.random = UserMabWeightConfig.DEFAULT_RANDOM_WEIGHT;
    }

    public double getChannelWeight(String channel) {
        return switch (channel) {
            case "hot" -> this.getHot();
            case "cache" -> this.getCache();
            case "region" -> this.getRegion();
            case "social" -> this.getSocial();
            case "news" -> this.getNews();
            case "interest" -> this.getInterest();
            case "random" -> this.getRandom();
            default -> 0d;
        };
    }

    public void updateChannelWeight(String channel, double weight) {
        switch (channel) {
            case "hot" -> this.setHot(weight);
            case "cache" -> this.setCache(weight);
            case "region" -> this.setRegion(weight);
            case "social" -> this.setSocial(weight);
            case "news" -> this.setNews(weight);
            case "interest" -> this.setInterest(weight);
            case "random" -> this.setRandom(weight);
        }
        ;
    }
}
