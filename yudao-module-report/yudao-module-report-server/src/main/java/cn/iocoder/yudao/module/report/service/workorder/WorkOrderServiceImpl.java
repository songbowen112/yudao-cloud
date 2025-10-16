package cn.iocoder.yudao.module.report.service.workorder;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.report.controller.admin.workorder.vo.*;
import cn.iocoder.yudao.module.report.dal.dataobject.workorder.WorkOrderDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.report.dal.mysql.workorder.WorkOrderMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.*;

/**
 * 工单信息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WorkOrderServiceImpl implements WorkOrderService {

    @Resource
    private WorkOrderMapper workOrderMapper;

    @Override
    public Long createWorkOrder(WorkOrderSaveReqVO createReqVO) {
        // 插入
        WorkOrderDO workOrder = BeanUtils.toBean(createReqVO, WorkOrderDO.class);
        workOrderMapper.insert(workOrder);

        // 返回
        return workOrder.getId();
    }

    @Override
    public void updateWorkOrder(WorkOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateWorkOrderExists(updateReqVO.getId());
        // 更新
        WorkOrderDO updateObj = BeanUtils.toBean(updateReqVO, WorkOrderDO.class);
        workOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteWorkOrder(Long id) {
        // 校验存在
        validateWorkOrderExists(id);
        // 删除
        workOrderMapper.deleteById(id);
    }

    @Override
        public void deleteWorkOrderListByIds(List<Long> ids) {
        // 删除
        workOrderMapper.deleteByIds(ids);
        }


    private void validateWorkOrderExists(Long id) {
        if (workOrderMapper.selectById(id) == null) {
            throw exception(WORK_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public WorkOrderDO getWorkOrder(Long id) {
        return workOrderMapper.selectById(id);
    }

    @Override
    public PageResult<WorkOrderDO> getWorkOrderPage(WorkOrderPageReqVO pageReqVO) {
        return workOrderMapper.selectPage(pageReqVO);
    }

}