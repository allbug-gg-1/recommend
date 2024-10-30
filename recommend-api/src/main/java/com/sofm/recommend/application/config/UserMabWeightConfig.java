package com.sofm.recommend.application.config;

public class UserMabWeightConfig {

    public static final double DEFAULT_HOT_WEIGHT = 1.3d;    // 热度稍高
    public static final double DEFAULT_CACHE_WEIGHT = 1.0d;
    public static final double DEFAULT_RANDOM_WEIGHT = 0.8d; // 随机召回稍低
    public static final double DEFAULT_REGION_WEIGHT = 1.0d;
    public static final double DEFAULT_SOCIAL_WEIGHT = 1.0d;
    public static final double DEFAULT_NEWS_WEIGHT = 0.9d;   // 新品召回稍低
    public static final double DEFAULT_INTEREST_WEIGHT = 1.3d;  // 兴趣召回稍高
    public static final double DEFAULT_ONLY_REGION_WEIGHT = 5.0d;
    public static final double DEFAULT_ONLY_RANDOM_WEIGHT = 5.0d;

    public static final int DEFAULT_CHANNEL_COUNT = 200;
    public static final int MAX_CHANNEL_COUNT = 400;
    public static final int MIN_CHANNEL_COUNT = 100;

    public static double getChannelDefaultWeight(String channel) {
        return switch (channel) {
            case "hot" -> UserMabWeightConfig.DEFAULT_HOT_WEIGHT;
            case "cache" -> UserMabWeightConfig.DEFAULT_CACHE_WEIGHT;
            case "region" -> UserMabWeightConfig.DEFAULT_REGION_WEIGHT;
            case "social" -> UserMabWeightConfig.DEFAULT_SOCIAL_WEIGHT;
            case "news" -> UserMabWeightConfig.DEFAULT_NEWS_WEIGHT;
            case "interest" -> UserMabWeightConfig.DEFAULT_INTEREST_WEIGHT;
            case "random" -> UserMabWeightConfig.DEFAULT_RANDOM_WEIGHT;
            default -> 0.0d;
        };
    }

    public static double getOnlyNearByWeight() {
        return DEFAULT_ONLY_REGION_WEIGHT;
    }

    public static double getOnlyRandomWeight() {
        return DEFAULT_ONLY_RANDOM_WEIGHT;
    }

    private UserMabWeightConfig() {
    }
}
