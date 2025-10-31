package cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ConfirmOrderSaveReqVO {

    @Schema(description = "主键，更新时必传")
    private Long id;

    @Schema(description = "工单名称")
    @NotNull(message = "工单名称不能为空")
    private String name;

    private Long receiptCompanyId;
    private String receiptCompanyName;
    private Long paymentCompanyId;
    private String paymentCompanyName;
    private String tagIds;
    private Integer status;
    private Integer fileType;
    private String fileUrl;
    private String remark;
}


