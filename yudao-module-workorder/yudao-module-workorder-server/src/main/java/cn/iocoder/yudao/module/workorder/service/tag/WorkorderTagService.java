package cn.iocoder.yudao.module.workorder.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.workorder.controller.admin.tag.vo.WorkorderTagPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.tag.vo.WorkorderTagSaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.tag.WorkorderTagDO;

import javax.validation.Valid;
import java.util.List;

public interface WorkorderTagService {
    Long create(@Valid WorkorderTagSaveReqVO createReqVO);
    void update(@Valid WorkorderTagSaveReqVO updateReqVO);
    void delete(Long id);
    WorkorderTagDO get(Long id);
    PageResult<WorkorderTagDO> getPage(WorkorderTagPageReqVO pageReqVO);
    
    /**
     * 查询所有一级标签（父标签ID为null或-1）
     *
     * @param status 状态，可选。如果为空则查询所有状态的标签
     */
    List<WorkorderTagDO> getListByParentTagIdIsNull(Integer status);
    
    /**
     * 根据一级标签ID查询所有二级标签
     *
     * @param parentTagId 父标签ID
     * @param status 状态，可选。如果为空则查询所有状态的标签
     */
    List<WorkorderTagDO> getListByParentTagId(Long parentTagId, Integer status);

    /**
     * 根据多个一级标签ID查询所有二级标签
     *
     * @param parentTagIds 父标签ID列表
     * @param status 状态，可选。如果为空则查询所有状态的标签
     */
    List<WorkorderTagDO> getListByParentTagIds(List<Long> parentTagIds, Integer status);
}


