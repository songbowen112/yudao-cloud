package cn.iocoder.yudao.module.workorder.controller.admin.tag.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class WorkorderTagPageReqVO extends PageParam {
    private String tagName;
    private Long parentTagId;
    private Integer status;
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}


