package cn.zack.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    // 类型
    private String type;
    // id
    private Long id;
    // 问题名称
    private String title;
    // 问题类型
    private String question_type;
    // 创建时间
    private String created;
    // 修改时间
    private String updated_time;
    // api地址
    private String url;
}
