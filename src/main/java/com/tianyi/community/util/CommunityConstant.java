package com.tianyi.community.util;

public interface CommunityConstant {
    /**
     * activation success
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * repeat activation
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * activation failure
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * (default) login ticket expiration seconds
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12; // 0.5 day
    /**
     * (remember me) login ticket expiration seconds
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30; // 1 month
    /**
     * entity type: post
     */
    int ENTITY_TYPE_POST = 1;
    /**
     * entity type: comment
     */
    int ENTITY_TYPE_COMMENT = 2;
    /**
     * entity type: user
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * topic: comment
     */
    String TOPIC_COMMENT = "comment";
    /**
     * topic: like
     */
    String TOPIC_LIKE = "like";
    /**
     * topic: follow
     */
    String TOPIC_FOLLOW = "follow";
    /**
     * topic: publish
     */
    String TOPIC_PUBLISH = "publish";
    /**
     * topic: delete
     */
    String TOPIC_DELETE = "delete";
    /**
     * topic: share
     */
    String TOPIC_SHARE = "share";
    /**
     * System notice userId: 1
     * */
    int SYSTEM_USER_ID =  1;
    /**
     * authority type: user
     */
    String AUTHORITY_USER = "user";
    /**
     * authority type: admin
     */
    String AUTHORITY_ADMIN = "admin";
    /**
     * authority type: moderator
     */
    String AUTHORITY_MODERATOR = "moderator";


}
