package cn.iocoder.yudao.module.workorder.service.confirmorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderSaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;

import javax.validation.Valid;
import java.util.List;

public interface ConfirmOrderService {

    Long create(@Valid ConfirmOrderSaveReqVO createReqVO);

    void update(@Valid ConfirmOrderSaveReqVO updateReqVO);

    void delete(Long id);

    ConfirmOrderDO get(Long id);

    PageResult<ConfirmOrderDO> getPage(ConfirmOrderPageReqVO pageReqVO);

    /**
     * 查询所有初始化状态的确认单（status = 1）
     *
     * @return 确认单列表
     */
    List<ConfirmOrderDO> getListByInitStatus();
}


