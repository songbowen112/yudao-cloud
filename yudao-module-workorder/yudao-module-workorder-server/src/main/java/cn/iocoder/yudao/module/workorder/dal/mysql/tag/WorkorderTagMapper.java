package cn.iocoder.yudao.module.workorder.dal.mysql.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.workorder.controller.admin.tag.vo.WorkorderTagPageReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.tag.WorkorderTagDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkorderTagMapper extends BaseMapperX<WorkorderTagDO> {
    default PageResult<WorkorderTagDO> selectPage(WorkorderTagPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WorkorderTagDO>()
                .likeIfPresent(WorkorderTagDO::getTagName, reqVO.getTagName())
                .eqIfPresent(WorkorderTagDO::getParentTagId, reqVO.getParentTagId())
                .eqIfPresent(WorkorderTagDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(WorkorderTagDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WorkorderTagDO::getId));
    }
}


