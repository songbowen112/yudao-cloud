package cn.iocoder.yudao.module.workorder.service.company;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.workorder.controller.admin.company.vo.WorkorderCompanyPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.company.vo.WorkorderCompanySaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.company.WorkorderCompanyDO;

import javax.validation.Valid;

public interface WorkorderCompanyService {
    Long create(@Valid WorkorderCompanySaveReqVO createReqVO);
    void update(@Valid WorkorderCompanySaveReqVO updateReqVO);
    void delete(Long id);
    WorkorderCompanyDO get(Long id);
    PageResult<WorkorderCompanyDO> getPage(WorkorderCompanyPageReqVO pageReqVO);
}


