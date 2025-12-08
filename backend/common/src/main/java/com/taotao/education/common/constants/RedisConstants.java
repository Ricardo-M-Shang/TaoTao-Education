package com.taotao.education.common.constants;

/**
 * Redis常量类
 */
public class RedisConstants {
    
    /**
     * 用户Token前缀
     */
    public static final String USER_TOKEN_PREFIX = "user:token:";
    
    /**
     * 用户信息前缀
     */
    public static final String USER_INFO_PREFIX = "user:info:";
    
    /**
     * 课程信息前缀
     */
    public static final String COURSE_INFO_PREFIX = "course:info:";
    
    /**
     * 课程分类前缀
     */
    public static final String COURSE_CATEGORY_PREFIX = "course:category:";
    
    /**
     * 验证码前缀
     */
    public static final String CAPTCHA_PREFIX = "captcha:";
    
    /**
     * 短信验证码前缀
     */
    public static final String SMS_CODE_PREFIX = "sms:code:";
    
    /**
     * 学习进度前缀
     */
    public static final String LEARNING_PROGRESS_PREFIX = "learning:progress:";
    
    /**
     * Token过期时间 (24小时)
     */
    public static final long TOKEN_EXPIRE_TIME = 24 * 60 * 60;
    
    /**
     * 验证码过期时间 (5分钟)
     */
    public static final long CAPTCHA_EXPIRE_TIME = 5 * 60;
    
    /**
     * 短信验证码过期时间 (5分钟)
     */
    public static final long SMS_CODE_EXPIRE_TIME = 5 * 60;
    
    /**
     * 课程信息缓存时间 (1小时)
     */
    public static final long COURSE_CACHE_TIME = 60 * 60;
}

