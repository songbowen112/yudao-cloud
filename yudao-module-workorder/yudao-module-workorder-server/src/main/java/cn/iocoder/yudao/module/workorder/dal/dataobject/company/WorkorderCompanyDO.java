package cn.iocoder.yudao.module.workorder.dal.dataobject.company;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;

@TableName("workorder_company")
@KeySequence("workorder_company_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkorderCompanyDO extends BaseDO {

    @TableId
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
}


