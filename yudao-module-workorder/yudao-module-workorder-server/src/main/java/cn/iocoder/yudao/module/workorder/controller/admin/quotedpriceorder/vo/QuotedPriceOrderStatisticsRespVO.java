package cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 报价单按月统计响应 VO
 */
@Data
public class QuotedPriceOrderStatisticsRespVO {
    /**
     * 月份（格式：YYYY-MM）
     */
    private String month;
    
    /**
     * 总条数
     */
    private Long totalCount;
    
    /**
     * 总数量（所有报价单的数量之和）
     */
    private Long totalQuantity;
    
    /**
     * 总价款（所有报价单的总价之和）
     */
    private BigDecimal totalPrice;
    
    /**
     * 总尾款（所有报价单的尾款之和）
     */
    private BigDecimal totalFinalPayment;
}

