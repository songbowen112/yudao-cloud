package cn.iocoder.yudao.module.workorder.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.tag.vo.WorkorderTagPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.tag.vo.WorkorderTagSaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.tag.WorkorderTagDO;
import cn.iocoder.yudao.module.workorder.dal.mysql.tag.WorkorderTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.workorder.enums.ErrorCodeConstants.*;

@Service
@Validated
public class WorkorderTagServiceImpl implements WorkorderTagService {

    @Resource
    private WorkorderTagMapper workorderTagMapper;

    @Override
    public Long create(WorkorderTagSaveReqVO createReqVO) {
        WorkorderTagDO entity = BeanUtils.toBean(createReqVO, WorkorderTagDO.class);
        workorderTagMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(WorkorderTagSaveReqVO updateReqVO) {
        validateExists(updateReqVO.getId());
        WorkorderTagDO entity = BeanUtils.toBean(updateReqVO, WorkorderTagDO.class);
        workorderTagMapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        validateExists(id);
        workorderTagMapper.deleteById(id);
    }

    @Override
    public WorkorderTagDO get(Long id) {
        return workorderTagMapper.selectById(id);
    }

    @Override
    public PageResult<WorkorderTagDO> getPage(WorkorderTagPageReqVO pageReqVO) {
        return workorderTagMapper.selectPage(pageReqVO);
    }

    private void validateExists(Long id) {
        if (workorderTagMapper.selectById(id) == null) {
            throw exception(WORKORDER_TAG_NOT_EXISTS);
        }
    }
}


