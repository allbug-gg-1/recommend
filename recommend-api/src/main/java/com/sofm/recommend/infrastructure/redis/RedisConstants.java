package com.sofm.recommend.infrastructure.redis;

public class RedisConstants {

    public static final String user_month_exposure = "exposure:{month}:{half}:{user_id}"; // bloom 用户每日浏览序列
    public static final String user_recent_interaction_publisher = "interaction_publisher:{user_id}"; // z set 用户最近交互的发布者
    public static final String user_recent_interaction_topic = "interaction_topic:{user_id}"; // z set 用户最近交互的话题
    public static final String user_recent_item = "recent_item:{user_id}"; // z set 用户最近交互的id
    public static final String user_mab = "user_mab:{user_id}"; // string json 用户的多臂老虎机参数
    public static final String user_family = "user_family:{user_id}"; //  set 用户加入的家族
    public static final String family_user = "family_user:{family_id}"; //  list 家族下的用户
    public static final String adopt_user = "adopt_user"; //  z set 最近互动过领养帖子的用户
    public static final String user_rank_cache_pool = "rank_cache_pool:{user_id}"; //  缓存召回通道 list 长度为500 FIFO
    public static final String user_dislike_items = "dislike_items:{user_id}"; // bloom 用户不喜欢物品
    public static final String user_dislike_pet = "dislike_pet:{user_id}"; //   list 用户不喜欢宠物类型 可以重复
    public static final String user_dislike_user = "dislike_user:{user_id}"; //   list 用户不喜欢发布者 可以重复
    public static final String creator_recent_items = "creator_items:{user_id}"; //  z set 用户最新的帖子 20个 可以重复
    public static final String total_recent_items = "total_recent_items"; // z set 平台新帖子
    public static final String hot_items = "hot_items"; // z set 平台热度帖子
    public static final String ad_items = "ad_items"; // z set 平台广告帖子

    public static final String union_user_follow_key = "union:follow:{user_id}"; //  z set 用户最新的帖子 20个 可以重复


    public static final String family_sync_time = "family_sync_time"; // 同步时间
    public static final String note_sync_time = "note_sync_time"; // 帖子同步时间


    public static final String active_activity = "active_activity"; //set 当前开启的活动

    public static final String recent_run_user = "recent_run_user"; //zset 最近在活动中的用户 并且userMab改变了


    private RedisConstants() {
    }
}
