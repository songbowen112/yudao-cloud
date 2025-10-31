package cn.iocoder.yudao.module.workorder.dal.dataobject.tag;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

@TableName("workorder_tag")
@KeySequence("workorder_tag_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkorderTagDO extends BaseDO {
    @TableId
    private Long id;
    private String tagName;
    private Long parentTagId;
    private Integer status;
}


