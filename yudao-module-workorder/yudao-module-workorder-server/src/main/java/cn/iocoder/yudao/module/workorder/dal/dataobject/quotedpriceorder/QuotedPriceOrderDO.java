package cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import java.math.BigDecimal;

/**
 * 报价单信息 DO
 */
@TableName("workorder_quoted_price_order")
@KeySequence("workorder_quoted_price_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotedPriceOrderDO extends TenantBaseDO {

    @TableId
    private Long id;
    private String name;
    private Long confirmOrderId;
    private String confirmOrderName;
    private Long receiptCompanyId;
    private String receiptCompanyName;
    private Long paymentCompanyId;
    private String paymentCompanyName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private BigDecimal advancePayment;
    private BigDecimal finalPayment;
    private BigDecimal discountPrice;
    private Integer status;
    private Integer fileType;
    private String fileUrl;
    private String remark;
}


