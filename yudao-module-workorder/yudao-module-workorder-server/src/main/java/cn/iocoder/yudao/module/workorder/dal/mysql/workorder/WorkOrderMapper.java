package cn.iocoder.yudao.module.workorder.dal.mysql.workorder;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.workorder.dal.dataobject.workorder.WorkOrderDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.workorder.controller.admin.workorder.vo.*;

/**
 * 工单信息 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WorkOrderMapper extends BaseMapperX<WorkOrderDO> {

    default PageResult<WorkOrderDO> selectPage(WorkOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WorkOrderDO>()
                .likeIfPresent(WorkOrderDO::getName, reqVO.getName())
                .eqIfPresent(WorkOrderDO::getAreaId, reqVO.getAreaId())
                .eqIfPresent(WorkOrderDO::getTagIds, reqVO.getTagIds())
                .eqIfPresent(WorkOrderDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(WorkOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WorkOrderDO::getId));
    }

}


