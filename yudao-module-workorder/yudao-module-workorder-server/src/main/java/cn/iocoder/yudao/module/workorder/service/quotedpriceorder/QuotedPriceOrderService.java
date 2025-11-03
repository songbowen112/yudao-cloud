package cn.iocoder.yudao.module.workorder.service.quotedpriceorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderSaveReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderStatisticsRespVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder.QuotedPriceOrderDO;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

public interface QuotedPriceOrderService {
    Long create(@Valid QuotedPriceOrderSaveReqVO createReqVO);
    void update(@Valid QuotedPriceOrderSaveReqVO updateReqVO);
    void delete(Long id);
    QuotedPriceOrderDO get(Long id);
    PageResult<QuotedPriceOrderDO> getPage(QuotedPriceOrderPageReqVO pageReqVO);
    
    /**
     * 获取按月统计数据
     *
     * @param beginTime 开始时间，可选。如果为空，则统计所有数据
     * @param endTime   结束时间，可选。如果为空，则统计所有数据
     * @return 按月统计结果列表
     */
    List<QuotedPriceOrderStatisticsRespVO> getStatisticsByMonth(LocalDateTime beginTime, LocalDateTime endTime);
}


