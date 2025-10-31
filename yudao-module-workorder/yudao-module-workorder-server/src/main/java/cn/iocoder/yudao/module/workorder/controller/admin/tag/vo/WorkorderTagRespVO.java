package cn.iocoder.yudao.module.workorder.controller.admin.tag.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkorderTagRespVO {
    private Long id;
    private String tagName;
    private Long parentTagId;
    private Integer status;
    private LocalDateTime createTime;
}


