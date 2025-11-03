package cn.iocoder.yudao.module.workorder.dal.mysql.confirmorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConfirmOrderMapper extends BaseMapperX<ConfirmOrderDO> {

    default PageResult<ConfirmOrderDO> selectPage(ConfirmOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ConfirmOrderDO>()
                .likeIfPresent(ConfirmOrderDO::getName, reqVO.getName())
                .eqIfPresent(ConfirmOrderDO::getContractCompanyId, reqVO.getContractCompanyId())
                .eqIfPresent(ConfirmOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ConfirmOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ConfirmOrderDO::getId));
    }

    /**
     * 查询所有初始化状态的确认单（status = 1）
     *
     * @return 确认单列表
     */
    default List<ConfirmOrderDO> selectListByInitStatus() {
        return selectList(new LambdaQueryWrapperX<ConfirmOrderDO>()
                .eq(ConfirmOrderDO::getStatus, 1) // 1-初始化
                .orderByDesc(ConfirmOrderDO::getId));
    }
}


