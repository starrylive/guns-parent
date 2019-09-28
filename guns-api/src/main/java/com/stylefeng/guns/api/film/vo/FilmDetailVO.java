package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class FilmDetailVO implements Serializable {

    private String filmId;
    private String filmName;
    private String filmEnName;
    private String imgAddress;
    private String score;
    private String scoreNum;
    private String totalBox;
    // 类型
    private String info01;
    // 地区 / 时长
    private String info02;
    // 上映时间地点
    private String info03;
    // 演员列表
    private InfoRequstVO info04;


}
