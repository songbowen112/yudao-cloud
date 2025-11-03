package cn.iocoder.yudao.module.workorder.dal.mysql.quotedpriceorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderStatisticsRespVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder.QuotedPriceOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface QuotedPriceOrderMapper extends BaseMapperX<QuotedPriceOrderDO> {

    default PageResult<QuotedPriceOrderDO> selectPage(QuotedPriceOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<QuotedPriceOrderDO>()
                .likeIfPresent(QuotedPriceOrderDO::getName, reqVO.getName())
                .eqIfPresent(QuotedPriceOrderDO::getConfirmOrderId, reqVO.getConfirmOrderId())
                .eqIfPresent(QuotedPriceOrderDO::getReceiptCompanyId, reqVO.getReceiptCompanyId())
                .eqIfPresent(QuotedPriceOrderDO::getPaymentCompanyId, reqVO.getPaymentCompanyId())
                .eqIfPresent(QuotedPriceOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(QuotedPriceOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(QuotedPriceOrderDO::getId));
    }

    /**
     * 按月统计报价单数据
     *
     * @param beginTime 开始时间，可选
     * @param endTime   结束时间，可选
     * @return 按月统计结果列表
     */
    List<QuotedPriceOrderStatisticsRespVO> selectStatisticsByMonth(
            @Param("beginTime") LocalDateTime beginTime,
            @Param("endTime") LocalDateTime endTime);
}


