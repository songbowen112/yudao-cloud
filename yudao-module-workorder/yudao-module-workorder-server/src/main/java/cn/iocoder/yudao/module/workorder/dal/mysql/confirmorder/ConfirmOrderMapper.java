package cn.iocoder.yudao.module.workorder.dal.mysql.confirmorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConfirmOrderMapper extends BaseMapperX<ConfirmOrderDO> {

    default PageResult<ConfirmOrderDO> selectPage(ConfirmOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ConfirmOrderDO>()
                .likeIfPresent(ConfirmOrderDO::getName, reqVO.getName())
                .eqIfPresent(ConfirmOrderDO::getReceiptCompanyId, reqVO.getReceiptCompanyId())
                .eqIfPresent(ConfirmOrderDO::getPaymentCompanyId, reqVO.getPaymentCompanyId())
                .eqIfPresent(ConfirmOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ConfirmOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ConfirmOrderDO::getId));
    }
}


