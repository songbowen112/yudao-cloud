package cn.iocoder.yudao.module.workorder.dal.dataobject.tag;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;

@TableName("workorder_tag")
@KeySequence("workorder_tag_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkorderTagDO extends TenantBaseDO {
    @TableId
    private Long id;
    private String tagName;
    private Long parentTagId;
    private Integer status;
}


