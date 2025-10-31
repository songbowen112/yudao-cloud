package cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConfirmOrderRespVO {
    private Long id;
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
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}


