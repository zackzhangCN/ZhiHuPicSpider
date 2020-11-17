package cn.zack.client;

import cn.zack.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zack
 * 客户端UI界面
 */
@Component("ZhiHuPicSpiderUI")
public class ZhiHuPicSpiderUI extends JFrame {
    private static ZhiHuPicSpiderUI instance = null;

    @Autowired
    private QuestionService questionService;

    public ZhiHuPicSpiderUI() {
    }

    // 创建窗口对象
    public static ZhiHuPicSpiderUI getInstance() {
        if (null == instance) {
            synchronized (ZhiHuPicSpiderUI.class) {
                if (null == instance) {
                    instance = new ZhiHuPicSpiderUI();
                }
            }
        }
        return instance;
    }

    public void initUI() {
        // 窗口标题
        this.setTitle("知乎图片爬虫");
        // 窗口大小
        this.setSize(380, 160);
        // 不可调整窗口大小
        this.setResizable(false);
        // 窗口关闭时退出程序
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 居中显示
        this.setLocationRelativeTo(null);

        // 流式布局
        FlowLayout flowLayout = new FlowLayout();
        this.setLayout(flowLayout);

        // 文本框
        JLabel urlJlabel = new JLabel("链接地址");
        this.add(urlJlabel);
        JTextField urlJTextField = new JTextField();
        Dimension dimension = new Dimension(250, 30);
        urlJTextField.setPreferredSize(dimension);
        this.add(urlJTextField);

        // 目录选择
        JButton pathButton = new JButton("保存路径");
        this.add(pathButton);
        JTextField pathJTextField = new JTextField();
        Dimension pathDimension = new Dimension(215, 30);
        pathJTextField.setPreferredSize(pathDimension);
        this.add(pathJTextField);

        // 按钮
        JButton spiderButton = new JButton("开始爬取");
        this.add(spiderButton);

        // 展示窗口
        this.setVisible(true);

        // 目录选择按钮点击事件
        pathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("选择保存目录");
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int i = jFileChooser.showOpenDialog(null);
                if (i == JFileChooser.APPROVE_OPTION) {
                    String selectPath = jFileChooser.getSelectedFile().getPath();
                    pathJTextField.setText(selectPath);
                }
            }
        });

        // 爬取按钮点击事件
        spiderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取输入的url地址
                String url = urlJTextField.getText();
                // 截取问题id
                String questionId = url.split("question/")[1];

                // 获取选择的路径
                String filePath = pathJTextField.getText();

                // 启动爬虫
                String result = questionService.getAnswerListByQuestionId(questionId, filePath);
                if (result.equals("downloading complete !!!")) {
                    JOptionPane.showMessageDialog(null, "爬取完成", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }
}
