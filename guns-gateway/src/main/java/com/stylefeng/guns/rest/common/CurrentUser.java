package com.stylefeng.guns.rest.common;

/**
 * 建议换成redis实现，这里占用jvm内存不太好
 */

public class CurrentUser {

    // todo InheritableThreadLocal避免和豪猪的线程池发生冲突，底层相关我还不太了解
    // 线程绑定的存储空间
    private static final InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<>();
    public static void saveUserId(String userId){
        threadLocal.set(userId);
    }
    public static String getCurrentUser(){
        return threadLocal.get();
    }

     // 将用户信息放入存储空间
//    public static void saveUserInfo(UserInfoModel userInfoModel){
//        threadLocal.set(userInfoModel);
//    }
//
//    // 将用户信息取出
//    public static UserInfoModel getCurrentUser(){
//        return threadLocal.get();
//    }

}
