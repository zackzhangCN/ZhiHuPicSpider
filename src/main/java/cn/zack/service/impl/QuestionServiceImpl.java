package cn.zack.service.impl;

import cn.zack.config.HttpConnectionManager;
import cn.zack.entity.AnswerData;
import cn.zack.entity.AnswerVo;
import cn.zack.service.QuestionService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zack
 * 爬虫实现类
 * 本意为爬取某个问题下的所有回答信息, 这里只取了每个回答中的图片
 */
@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {

    /**
     * http连接池
     */
    @Autowired
    private HttpConnectionManager httpConnectionManager;

    /**
     * 线程池
     */
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 问题下的回答列表api
     */
    @Value("${question.api}")
    private String baseApiUrl;
    @Value("${question.param.url}")
    private String paramApiUrl;

    /**
     * 要保存到本地的路径
     */
    private String downloadUrl = "";

    @Override
    public String getAnswerListByQuestionId(String questionId, String path) {
        // 修改保存路径
        downloadUrl = path + "\\";
        String targetUrl = baseApiUrl + questionId + paramApiUrl;
        // 请求第一页, 获取总回答数
        Integer totalAnswer = getMessage(targetUrl + 0);
        // 计算需要请求多少次
        Integer requestCount;
        if ((totalAnswer % 20) == 0) {
            requestCount = totalAnswer / 20;
        } else {
            requestCount = (totalAnswer / 20) + 1;
        }
        // 继续从第二页开始请求
        for (Integer i = 1; i <= requestCount; i++) {
            String thisUrl = targetUrl + 20 * i;
            getMessage(thisUrl);
        }
        return "downloading complete !!!";
    }

    /**
     * 爬取数据
     *
     * @param url 目标url
     * @return
     */
    public Integer getMessage(String url) {
        // http请求
        HttpGet httpGet = new HttpGet(url);
        // 设置请求头
        httpGet.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
        // 从http连接池获取一个连接
        CloseableHttpClient httpClient = httpConnectionManager.getHttpClient();

        try {
            // 发起http请求
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int responseCode = response.getStatusLine().getStatusCode();
            // 判定响应状态码
            if (responseCode == 200) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                // 解析为AnswerVo
                AnswerVo answerVo = JSONObject.parseObject(result, AnswerVo.class);
                // 取出回答部分
                List<AnswerData> answerDataList = answerVo.getData();
                threadPoolTaskExecutor.execute(
                        () -> {
                            // 解析出图片url, 并下载图片
                            parseImgUrl(answerDataList);
                        }
                );
                // 总回答数
                Integer totals = answerVo.getPaging().getTotals();
                return totals;
            } else if (responseCode == 403) {
                // todo 解决验证码, 打码平台
                log.info("===========无权限, 待处理验证码, 当前处理到: {}=========", url);
                return null;
            } else {
                log.error("=========遇到其他错误, 当前处理到: {}========", url);
                return null;
            }
        } catch (IOException e) {
            log.error("========请求失败========");
            return null;
        }
    }

    /**
     * 根据回答详情集合, 解析出回答中的图片列表
     *
     * @param answerDataList
     */
    public void parseImgUrl(List<AnswerData> answerDataList) {

        String imgRegex = "(<img.*src\\s*=\\s*(.*?)[^>]*?>)";
        String srcRegex = "src\\s*=\\s*\"?(.*?)(\"|>|\\s+)";
        if (answerDataList != null && answerDataList.size() > 0) {
            for (AnswerData answerData : answerDataList) {
                // 此回答者的昵称, 拼接回答URL, 解决匿名用户/已注销/已重置用户名字重复问题
                String username = answerData.getAuthor().getName() + "(question=" + answerData.getQuestion().getId() + ")" + "(AnswerId=" +answerData.getId() + ")";
                // 此回答具体内容
                String content = answerData.getContent();
                // 此回答中的图片地址集合
                ArrayList<String> srcArrayList = new ArrayList<>();

                // 正则取图片地址
                Matcher imgMatcher = Pattern.compile(imgRegex, Pattern.CASE_INSENSITIVE).matcher(content);
                String img = "";
                while (imgMatcher.find()) {
                    // 得到img标签
                    img = imgMatcher.group();
                    // 再从img标签中取src数据
                    Matcher srcMatcher = Pattern.compile(srcRegex).matcher(img);
                    while (srcMatcher.find()) {
                        String thisUrl = srcMatcher.group(1);
                        // svg图片地址不保存
                        if (thisUrl.startsWith("data:image/svg")) {
                        } else {
                            srcArrayList.add(thisUrl);
                        }
                    }
                }
                // 下载图片
                if (srcArrayList != null && srcArrayList.size() > 0) {
                    threadPoolTaskExecutor.execute(
                            () -> {
                                // 每个图片连续重复4次, 但是url不同, 此处直接循环跨度为4达到去重目的
                                for (int i = 0; i < srcArrayList.size(); i += 4) {
                                    downloadImg(srcArrayList.get(i), username, i + ".jpg");
                                }
                            }
                    );
                }
            }
        }

    }

    /**
     * 根据图片url下载图片到本地
     *
     * @param imgUrl
     * @param prePath
     */
    public void downloadImg(String imgUrl, String prePath, String picName) {
        // 图片最终存储地址 D:\\图片\\user1\\001.jpg
        String imgPath = downloadUrl + prePath + picName;

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            // 根据图片地址获取IO流
            URL url = new URL(imgUrl);
            URLConnection urlConnection = url.openConnection();
            inputStream = urlConnection.getInputStream();

            // 写入到指定的文件中
            fileOutputStream = new FileOutputStream(imgPath);

            byte arr[] = new byte[1024 * 8];
            int len;
            while ((len = inputStream.read(arr)) != -1) {
                fileOutputStream.write(arr, 0, len);
            }
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            log.error("==============图片保存失败!==============");
        }
    }
}
