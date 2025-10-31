package cn.iocoder.yudao.module.workorder.controller.admin.tag.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class WorkorderTagSaveReqVO {
    private Long id;
    @NotNull(message = "标签名称不能为空")
    private String tagName;
    private Long parentTagId;
    private Integer status;
}


