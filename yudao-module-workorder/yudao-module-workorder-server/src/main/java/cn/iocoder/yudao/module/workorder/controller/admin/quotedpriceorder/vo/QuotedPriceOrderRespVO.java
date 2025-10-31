package cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QuotedPriceOrderRespVO {
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
    private LocalDateTime createTime;
}


