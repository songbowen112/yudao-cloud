package cn.iocoder.yudao.module.workorder.controller.admin.workorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 工单信息新增/修改 Request VO")
@Data
public class WorkOrderSaveReqVO {

    @Schema(description = "工单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21320")
    private Long id;

    @Schema(description = "工单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "工单名称不能为空")
    private String name;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "区域ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5569")
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    @Schema(description = "标签ID列表，多个用逗号分隔", requiredMode = Schema.RequiredMode.REQUIRED, example = "1,2,3")
    private String tagIds;

    @Schema(description = "工单状态：1-待处理 2-待审核 3-审核完成 4-审核失败 5-通知完成 6-通知失败", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer status;

    @Schema(description = "文件类型：1-PDF 2-DOC 3-XLS 4-PIC", example = "1")
    private Integer fileType;

    @Schema(description = "文件地址", example = "https://www.iocoder.cn/yudao.pdf")
    private String fileUrl;

}





