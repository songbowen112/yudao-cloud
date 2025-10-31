package cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class QuotedPriceOrderPageReqVO extends PageParam {
    private String name;
    private Long confirmOrderId;
    private Long receiptCompanyId;
    private Long paymentCompanyId;
    private Integer status;
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}


