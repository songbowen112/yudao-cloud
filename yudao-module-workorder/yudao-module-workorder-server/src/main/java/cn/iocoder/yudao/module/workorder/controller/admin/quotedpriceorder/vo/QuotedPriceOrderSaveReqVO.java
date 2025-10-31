package cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class QuotedPriceOrderSaveReqVO {
    private Long id;
    @NotNull(message = "名称不能为空")
    private String name;
    @NotNull(message = "确认单不能为空")
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


