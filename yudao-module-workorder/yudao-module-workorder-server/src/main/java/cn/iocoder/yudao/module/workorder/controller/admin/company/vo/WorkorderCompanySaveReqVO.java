package cn.iocoder.yudao.module.workorder.controller.admin.company.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class WorkorderCompanySaveReqVO {
    private Long id;
    @NotNull(message = "企业名称不能为空")
    private String name;
    private String shortName;
    private String licenseNo;
    private String legalPerson;
    private String address;
    private String tel;
    private String email;
    private String logoUrl;
    private String bankName;
    private String bankAccount;
    private String remark;
    private Integer status;
}


