package cn.iocoder.yudao.module.workorder.service.company;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.company.vo.WorkorderCompanyPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.company.vo.WorkorderCompanySaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.company.WorkorderCompanyDO;
import cn.iocoder.yudao.module.workorder.dal.mysql.company.WorkorderCompanyMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.workorder.enums.ErrorCodeConstants.*;

@Service
@Validated
public class WorkorderCompanyServiceImpl implements WorkorderCompanyService {

    @Resource
    private WorkorderCompanyMapper workorderCompanyMapper;

    @Override
    public Long create(WorkorderCompanySaveReqVO createReqVO) {
        WorkorderCompanyDO entity = BeanUtils.toBean(createReqVO, WorkorderCompanyDO.class);
        workorderCompanyMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(WorkorderCompanySaveReqVO updateReqVO) {
        validateExists(updateReqVO.getId());
        WorkorderCompanyDO entity = BeanUtils.toBean(updateReqVO, WorkorderCompanyDO.class);
        workorderCompanyMapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        validateExists(id);
        workorderCompanyMapper.deleteById(id);
    }

    @Override
    public WorkorderCompanyDO get(Long id) {
        return workorderCompanyMapper.selectById(id);
    }

    @Override
    public PageResult<WorkorderCompanyDO> getPage(WorkorderCompanyPageReqVO pageReqVO) {
        return workorderCompanyMapper.selectPage(pageReqVO);
    }

    private void validateExists(Long id) {
        if (workorderCompanyMapper.selectById(id) == null) {
            throw exception(WORKORDER_COMPANY_NOT_EXISTS);
        }
    }
}


