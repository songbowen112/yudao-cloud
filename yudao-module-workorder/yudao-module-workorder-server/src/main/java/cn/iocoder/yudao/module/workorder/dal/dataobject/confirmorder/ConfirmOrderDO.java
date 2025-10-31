package cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 确认单信息 DO
 */
@TableName("workorder_confirm_order")
@KeySequence("workorder_confirm_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmOrderDO extends BaseDO {

    /** 工单ID */
    @TableId
    private Long id;
    /** 工单名称 */
    private String name;
    /** 收款企业ID */
    private Long receiptCompanyId;
    /** 收款企业名称 */
    private String receiptCompanyName;
    /** 付款企业ID */
    private Long paymentCompanyId;
    /** 付款企业名称 */
    private String paymentCompanyName;
    /** 标签ID列表，多个用逗号分隔 */
    private String tagIds;
    /** 工单状态：1-初始化 2-报价完成 3-报价失败 4-通知完成 5-通知失败 */
    private Integer status;
    /** 文件类型 1-PDF 2-DOC 3-XLS */
    private Integer fileType;
    /** 文件路径 */
    private String fileUrl;
    /** 备注 */
    private String remark;
}


