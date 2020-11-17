package cn.zack.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paging {
    // 是否为最后一页
    private Boolean is_end;
    // 是否为第一页
    private Boolean is_start;
    // 下一页url
    private String next;
    // 上一页url
    private String previous;
    // 总回答数
    private Integer totals;
}
