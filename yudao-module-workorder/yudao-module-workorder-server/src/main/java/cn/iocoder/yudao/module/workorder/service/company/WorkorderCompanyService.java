package cn.iocoder.yudao.module.workorder.service.company;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.workorder.controller.admin.company.vo.WorkorderCompanyPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.company.vo.WorkorderCompanySaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.company.WorkorderCompanyDO;

import javax.validation.Valid;
import java.util.List;

public interface WorkorderCompanyService {
    Long create(@Valid WorkorderCompanySaveReqVO createReqVO);
    void update(@Valid WorkorderCompanySaveReqVO updateReqVO);
    void delete(Long id);
    WorkorderCompanyDO get(Long id);
    PageResult<WorkorderCompanyDO> getPage(WorkorderCompanyPageReqVO pageReqVO);

    /**
     * 根据状态查询所有企业列表
     *
     * @param status 状态，可选。如果为空则查询所有状态的企业
     */
    List<WorkorderCompanyDO> getListByStatus(Integer status);

    /**
     * 根据是否属于自己的查询企业列表中的第一个
     *
     * @param isOwn 是否属于自己的 0-否 1-是
     * @return 第一个企业，如果不存在则返回null
     */
    WorkorderCompanyDO getFirstByIsOwn(Integer isOwn);
}


