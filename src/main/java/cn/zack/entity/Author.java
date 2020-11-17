package cn.zack.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    // 回答者id
    private String id;
    // 回答者唯一标识token
    private String url_token;
    // 回答者姓名
    private String name;
    // 头像小图地址
    private String avatar_url;
    // 头像原图地址
    private String avatar_url_template;
    // is_org ???
    private Boolean is_org;
    // 回答者用户类型
    private String type;
    // 忽略此字段, 直接访问为404
    private String url;
    // 回答者用户类型?
    private String user_type;
    // 一句话描述
    private String headline;
    // 性别
    private int gender;
    // 是否为广告商
    private Boolean is_advertiser;
    // 粉丝数
    private Long follower_count;
}
