package cn.iocoder.yudao.module.report.controller.admin.workorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 工单信息分页 Request VO")
@Data
public class WorkOrderPageReqVO extends PageParam {

    @Schema(description = "工单名称", example = "赵六")
    private String name;

    @Schema(description = "区域ID", example = "5569")
    private Long areaId;

    @Schema(description = "标签ID列表，多个用逗号分隔", example = "1,2,3")
    private String tagIds;

    @Schema(description = "工单状态：1-待处理 2-待审核 3-审核完成 4-审核失败 5-通知完成 6-通知失败", example = "2")
    private Integer status;

    @Schema(description = "文件类型：1-PDF 2-DOC 3-XLS 4-PIC", example = "1")
    private Integer fileType;

    @Schema(description = "文件地址", example = "https://www.iocoder.cn/yudao.pdf")
    private String fileUrl;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}