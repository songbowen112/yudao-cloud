package cn.iocoder.yudao.module.workorder.service.quotedpriceorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderSaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder.QuotedPriceOrderDO;

import javax.validation.Valid;

public interface QuotedPriceOrderService {
    Long create(@Valid QuotedPriceOrderSaveReqVO createReqVO);
    void update(@Valid QuotedPriceOrderSaveReqVO updateReqVO);
    void delete(Long id);
    QuotedPriceOrderDO get(Long id);
    PageResult<QuotedPriceOrderDO> getPage(QuotedPriceOrderPageReqVO pageReqVO);
}


