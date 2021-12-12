package com.nowcoder.community.util;

//常量接口解耦合
public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认登录凭证过期时间
     */
    int DEFAULT_EXPIRED_SECONDS = 60*60*12;

    /**
     * 勾选记住我   记住状态的登录凭证过期时间
     */
    int REMEMBER_EXPIRED_SECONDS = 60*60*24*100;

}
