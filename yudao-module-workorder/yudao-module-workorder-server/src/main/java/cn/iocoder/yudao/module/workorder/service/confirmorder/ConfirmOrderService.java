package cn.iocoder.yudao.module.workorder.service.confirmorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderSaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;

import javax.validation.Valid;

public interface ConfirmOrderService {

    Long create(@Valid ConfirmOrderSaveReqVO createReqVO);

    void update(@Valid ConfirmOrderSaveReqVO updateReqVO);

    void delete(Long id);

    ConfirmOrderDO get(Long id);

    PageResult<ConfirmOrderDO> getPage(ConfirmOrderPageReqVO pageReqVO);
}


