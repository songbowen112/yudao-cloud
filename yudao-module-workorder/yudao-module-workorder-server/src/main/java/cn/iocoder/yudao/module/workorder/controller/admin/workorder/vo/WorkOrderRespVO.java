package cn.iocoder.yudao.module.workorder.controller.admin.workorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 工单信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WorkOrderRespVO {

    @Schema(description = "工单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21320")
    @ExcelProperty("工单ID")
    private Long id;

    @Schema(description = "工单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("工单名称")
    private String name;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "区域ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5569")
    @ExcelProperty("区域ID")
    private Long areaId;

    @Schema(description = "标签ID列表，多个用逗号分隔", requiredMode = Schema.RequiredMode.REQUIRED, example = "1,2,3")
    @ExcelProperty(value = "标签ID列表", converter = DictConvert.class)
    @DictFormat("tag_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private String tagIds;

    @Schema(description = "工单状态：1-待处理 2-待审核 3-审核完成 4-审核失败 5-通知完成 6-通知失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "工单状态：1-待处理 2-待审核 3-审核完成 4-审核失败 5-通知完成 6-通知失败", converter = DictConvert.class)
    @DictFormat("work_order_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "文件类型：1-PDF 2-DOC 3-XLS 4-PIC", example = "1")
    @ExcelProperty(value = "文件类型", converter = DictConvert.class)
    @DictFormat("file_type") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer fileType;

    @Schema(description = "文件地址", example = "https://www.iocoder.cn/yudao.pdf")
    @ExcelProperty("文件地址")
    private String fileUrl;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}


