package cn.zack.service;

/**
 * @author zack
 * @description 爬虫接口
 */
public interface QuestionService {
    String getAnswerListByQuestionId(String questionId, String path);
}
