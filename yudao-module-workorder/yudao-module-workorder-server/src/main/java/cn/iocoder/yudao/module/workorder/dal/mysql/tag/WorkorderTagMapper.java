package cn.iocoder.yudao.module.workorder.dal.mysql.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.workorder.controller.admin.tag.vo.WorkorderTagPageReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.tag.WorkorderTagDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WorkorderTagMapper extends BaseMapperX<WorkorderTagDO> {
    default PageResult<WorkorderTagDO> selectPage(WorkorderTagPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WorkorderTagDO>()
                .likeIfPresent(WorkorderTagDO::getTagName, reqVO.getTagName())
                .eqIfPresent(WorkorderTagDO::getParentTagId, reqVO.getParentTagId())
                .eqIfPresent(WorkorderTagDO::getId, reqVO.getTagId())
                .eqIfPresent(WorkorderTagDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(WorkorderTagDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WorkorderTagDO::getId));
    }

    /**
     * 查询所有一级标签（父标签ID为null或-1）
     *
     * @param status 状态，可选。如果为空则查询所有状态的标签
     */
    default List<WorkorderTagDO> selectListByParentTagIdIsNull(Integer status) {
        return selectList(new LambdaQueryWrapperX<WorkorderTagDO>()
                .eqIfPresent(WorkorderTagDO::getStatus, status)
                .and(wrapper -> wrapper.isNull(WorkorderTagDO::getParentTagId)
                        .or()
                        .eq(WorkorderTagDO::getParentTagId, -1L))
                .orderByAsc(WorkorderTagDO::getId));
    }

    /**
     * 根据一级标签ID查询所有二级标签
     *
     * @param parentTagId 父标签ID
     * @param status 状态，可选。如果为空则查询所有状态的标签
     */
    default List<WorkorderTagDO> selectListByParentTagId(Long parentTagId, Integer status) {
        return selectList(new LambdaQueryWrapperX<WorkorderTagDO>()
                .eq(WorkorderTagDO::getParentTagId, parentTagId)
                .eqIfPresent(WorkorderTagDO::getStatus, status)
                .orderByAsc(WorkorderTagDO::getId));
    }
}


