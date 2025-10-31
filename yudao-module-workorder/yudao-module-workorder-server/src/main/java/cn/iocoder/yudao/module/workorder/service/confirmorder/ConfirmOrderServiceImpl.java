package cn.iocoder.yudao.module.workorder.service.confirmorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderSaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;
import cn.iocoder.yudao.module.workorder.dal.mysql.confirmorder.ConfirmOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.workorder.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ConfirmOrderServiceImpl implements ConfirmOrderService {

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Override
    public Long create(ConfirmOrderSaveReqVO createReqVO) {
        ConfirmOrderDO entity = BeanUtils.toBean(createReqVO, ConfirmOrderDO.class);
        confirmOrderMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(ConfirmOrderSaveReqVO updateReqVO) {
        validateExists(updateReqVO.getId());
        ConfirmOrderDO entity = BeanUtils.toBean(updateReqVO, ConfirmOrderDO.class);
        confirmOrderMapper.updateById(entity);
    }

    @Override
    public void delete(Long id) {
        validateExists(id);
        confirmOrderMapper.deleteById(id);
    }

    @Override
    public ConfirmOrderDO get(Long id) {
        return confirmOrderMapper.selectById(id);
    }

    @Override
    public PageResult<ConfirmOrderDO> getPage(ConfirmOrderPageReqVO pageReqVO) {
        return confirmOrderMapper.selectPage(pageReqVO);
    }

    private void validateExists(Long id) {
        if (confirmOrderMapper.selectById(id) == null) {
            throw exception(CONFIRM_ORDER_NOT_EXISTS);
        }
    }
}


