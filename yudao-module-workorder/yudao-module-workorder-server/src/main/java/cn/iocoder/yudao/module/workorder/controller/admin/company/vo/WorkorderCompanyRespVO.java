package cn.iocoder.yudao.module.workorder.controller.admin.company.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkorderCompanyRespVO {
    private Long id;
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
    /** 是否属于自己的 0-否 1-是 */
    private Integer isOwn;
    private LocalDateTime createTime;
}


