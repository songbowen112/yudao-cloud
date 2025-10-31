package cn.iocoder.yudao.module.workorder.dal.mysql.quotedpriceorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder.QuotedPriceOrderDO;
import org.apache.ibatis.annotations.Mapper;

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
}


