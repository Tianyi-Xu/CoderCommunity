package com.tianyi.community.util;

public class RedisKeyUtil {
    private static final String DELIMITER = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_POST = "post";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";

    // like:entity:entityType:entityId -> set(userIds)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + DELIMITER + entityType + DELIMITER + entityId;
    }
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + DELIMITER + userId;
    }

    // key for a user's followed entity,  use the time as the ranking
    // followee:userId:entityType -> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + DELIMITER + userId + DELIMITER + entityType;
    }

    // key for the entity's followers
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + DELIMITER + entityType + DELIMITER + entityId;
    }

    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + DELIMITER + owner;
    }

    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + DELIMITER + ticket;
    }

    public static String getUserKey(int userId) {
        return PREFIX_USER + DELIMITER + userId;
    }

    // daily UV
    public static String getUVKey(String date) {
        return PREFIX_UV + DELIMITER + date;
    }

    // range UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + DELIMITER + startDate + DELIMITER + endDate;
    }

    // daily DAU
    public static String getDAUKey(String date) {
        return PREFIX_DAU + DELIMITER + date;
    }

    // range DAU
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + DELIMITER + startDate + DELIMITER + endDate;
    }

    public static String getPostScoreKey() {
        return PREFIX_POST + DELIMITER + "score";
    }

}
