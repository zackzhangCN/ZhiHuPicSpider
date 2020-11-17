package cn.zack.entity;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerData {
    // id
    private Long id;
    // 类型
    private String type;
    // 回答类型
    private String answer_type;

    private Question question;

    private Author author;

    // 此回答url
    private String url;
    // 是否为关闭状态?
    private Boolean is_collapsed;
    // 回答创建时间
    private String created_time;
    // 回答修改时间
    private String updated_time;
    // 额外的???
    private String extras;
    // 赞成数
    private String voteup_count;
    // 评论数
    private String comment_count;
    // 此回答的详情
    private String content;
}
