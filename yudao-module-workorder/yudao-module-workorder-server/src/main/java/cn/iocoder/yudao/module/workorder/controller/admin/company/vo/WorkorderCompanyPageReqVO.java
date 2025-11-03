package cn.iocoder.yudao.module.workorder.controller.admin.company.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class WorkorderCompanyPageReqVO extends PageParam {
    private String name;
    private Integer status;
    /** 是否属于自己的 0-否 1-是 */
    private Integer isOwn;
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}


