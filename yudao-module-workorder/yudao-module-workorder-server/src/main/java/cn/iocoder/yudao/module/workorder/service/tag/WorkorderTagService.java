package cn.iocoder.yudao.module.workorder.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.workorder.controller.admin.tag.vo.WorkorderTagPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.tag.vo.WorkorderTagSaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.tag.WorkorderTagDO;

import javax.validation.Valid;

public interface WorkorderTagService {
    Long create(@Valid WorkorderTagSaveReqVO createReqVO);
    void update(@Valid WorkorderTagSaveReqVO updateReqVO);
    void delete(Long id);
    WorkorderTagDO get(Long id);
    PageResult<WorkorderTagDO> getPage(WorkorderTagPageReqVO pageReqVO);
}


