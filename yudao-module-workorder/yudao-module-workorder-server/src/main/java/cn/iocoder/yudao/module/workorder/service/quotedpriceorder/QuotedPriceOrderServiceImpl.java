package cn.iocoder.yudao.module.workorder.service.quotedpriceorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderSaveReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderStatisticsRespVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder.QuotedPriceOrderDO;
import cn.iocoder.yudao.module.workorder.dal.mysql.confirmorder.ConfirmOrderMapper;
import cn.iocoder.yudao.module.workorder.dal.mysql.quotedpriceorder.QuotedPriceOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.workorder.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class QuotedPriceOrderServiceImpl implements QuotedPriceOrderService {

    @Resource
    private QuotedPriceOrderMapper quotedPriceOrderMapper;

    @Resource
    private QuotedPriceOrderFileGenerateService fileGenerateService;

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(QuotedPriceOrderSaveReqVO createReqVO) {
        Long confirmOrderId = createReqVO.getConfirmOrderId();
        if (confirmOrderId == null) {
            throw new IllegalArgumentException("确认单ID不能为空");
        }

        try {
            // 1. 验证确认单是否存在
            ConfirmOrderDO confirmOrder = confirmOrderMapper.selectById(confirmOrderId);
            if (confirmOrder == null) {
                log.error("创建报价单失败 - 确认单不存在，confirmOrderId: {}", confirmOrderId);
                throw new IllegalArgumentException("确认单不存在，ID: " + confirmOrderId);
            }

            // 2. 计算价格
            QuotedPriceOrderDO entity = BeanUtils.toBean(createReqVO, QuotedPriceOrderDO.class);
            // 清除creator和updater等字段，让MyBatis-Plus自动填充
            entity.clean();
            calculatePrices(entity);

            log.info("创建报价单 - 确认单ID: {}, 工单名称: '{}', 单价: {}, 数量: {}, 总价: {}", 
                    confirmOrderId, entity.getConfirmOrderName(), entity.getPrice(), entity.getQuantity(), entity.getTotalPrice());
            
            // 3. 保存报价单信息
            quotedPriceOrderMapper.insert(entity);
            
            // 4. 重新查询以确保所有字段都已正确设置
            entity = quotedPriceOrderMapper.selectById(entity.getId());

            // 5. 生成文件（如果文件类型不为空）
            if (entity.getFileType() != null) {
                log.info("开始生成报价单文件 - 文件类型: {}", entity.getFileType());
                String fileUrl = fileGenerateService.generateFile(entity);
                // 更新文件路径
                entity.setFileUrl(fileUrl);
                quotedPriceOrderMapper.updateById(entity);
            }

            // 6. 更新确认单状态为 2-报价完成
            updateConfirmOrderStatus(confirmOrderId, 2);
            log.info("更新确认单状态成功 - confirmOrderId: {}, status: 2-报价完成", confirmOrderId);

            return entity.getId();
        } catch (Exception e) {
            // 发生异常时，在独立事务中更新确认单状态为 3-报价失败
            // 使用 REQUIRES_NEW 确保即使主事务回滚，状态更新也能提交
            log.error("创建报价单失败，尝试更新确认单状态为报价失败 - confirmOrderId: {}", confirmOrderId, e);
            try {
                updateConfirmOrderStatusInNewTransaction(confirmOrderId, 3);
                log.info("更新确认单状态为报价失败成功 - confirmOrderId: {}, status: 3-报价失败", confirmOrderId);
            } catch (Exception updateException) {
                log.error("更新确认单状态为报价失败时发生异常 - confirmOrderId: {}", confirmOrderId, updateException);
                // 这里不抛出异常，因为主要异常应该被抛出
            }
            // 重新抛出原始异常，触发主事务回滚
            throw new RuntimeException("创建报价单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新确认单状态（在同一事务中）
     *
     * @param confirmOrderId 确认单ID
     * @param status 状态：1-初始化 2-报价完成 3-报价失败 4-通知完成 5-通知失败
     */
    private void updateConfirmOrderStatus(Long confirmOrderId, Integer status) {
        ConfirmOrderDO confirmOrder = confirmOrderMapper.selectById(confirmOrderId);
        if (confirmOrder == null) {
            log.warn("更新确认单状态失败 - 确认单不存在，confirmOrderId: {}", confirmOrderId);
            return;
        }
        confirmOrder.setStatus(status);
        // 清除creator和updater等字段，让MyBatis-Plus自动填充
        confirmOrder.clean();
        confirmOrderMapper.updateById(confirmOrder);
        log.debug("更新确认单状态 - confirmOrderId: {}, status: {}", confirmOrderId, status);
    }

    /**
     * 更新确认单状态（独立事务，用于失败场景）
     * 使用 REQUIRES_NEW 确保即使主事务回滚，状态更新也能提交
     *
     * @param confirmOrderId 确认单ID
     * @param status 状态：1-初始化 2-报价完成 3-报价失败 4-通知完成 5-通知失败
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateConfirmOrderStatusInNewTransaction(Long confirmOrderId, Integer status) {
        try {
            ConfirmOrderDO confirmOrder = confirmOrderMapper.selectById(confirmOrderId);
            if (confirmOrder == null) {
                log.warn("更新确认单状态失败 - 确认单不存在，confirmOrderId: {}", confirmOrderId);
                return;
            }
            confirmOrder.setStatus(status);
            // 清除creator和updater等字段，让MyBatis-Plus自动填充
            confirmOrder.clean();
            confirmOrderMapper.updateById(confirmOrder);
            log.info("独立事务更新确认单状态成功 - confirmOrderId: {}, status: {}", confirmOrderId, status);
        } catch (Exception e) {
            log.error("独立事务更新确认单状态失败 - confirmOrderId: {}, status: {}", confirmOrderId, status, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(QuotedPriceOrderSaveReqVO updateReqVO) {
        validateExists(updateReqVO.getId());
        // 1. 先查询现有数据，确保包含所有字段
        QuotedPriceOrderDO existingEntity = quotedPriceOrderMapper.selectById(updateReqVO.getId());
        if (existingEntity == null) {
            throw exception(QUOTED_PRICE_ORDER_NOT_EXISTS);
        }
        
        // 2. 将更新数据复制到现有实体
        BeanUtils.copyProperties(updateReqVO, existingEntity);
        // 清除creator和updater等字段，让MyBatis-Plus自动填充（防止前端传递这些字段）
        existingEntity.clean();
        
        // 3. 重新计算价格（因为单价或数量可能发生变化）
        calculatePrices(existingEntity);
        
        // 4. 更新报价单信息
        quotedPriceOrderMapper.updateById(existingEntity);

        // 5. 生成文件（如果文件类型不为空）
        if (existingEntity.getFileType() != null) {
            log.info("开始生成报价单文件 - 文件类型: {}", existingEntity.getFileType());
            String fileUrl = fileGenerateService.generateFile(existingEntity);
            // 更新文件路径
            existingEntity.setFileUrl(fileUrl);
            quotedPriceOrderMapper.updateById(existingEntity);
        }
    }

    /**
     * 计算价格：总价、预付款、尾款
     */
    private void calculatePrices(QuotedPriceOrderDO entity) {
        BigDecimal price = entity.getPrice() != null ? entity.getPrice() : BigDecimal.ZERO;
        Integer quantity = entity.getQuantity() != null ? entity.getQuantity() : 0;
        
        // 总价 = 单价 × 数量
        BigDecimal totalPrice = price.multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);
        entity.setTotalPrice(totalPrice);
        
        // 预付款 = 总价 × 30%
        BigDecimal advancePayment = totalPrice.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
        entity.setAdvancePayment(advancePayment);
        
        // 尾款 = 总价 × 70%
        BigDecimal finalPayment = totalPrice.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
        entity.setFinalPayment(finalPayment);
        
        log.debug("价格计算 - 单价: {}, 数量: {}, 总价: {}, 预付款: {}, 尾款: {}", 
                price, quantity, totalPrice, advancePayment, finalPayment);
    }

    @Override
    public void delete(Long id) {
        validateExists(id);
        quotedPriceOrderMapper.deleteById(id);
    }

    @Override
    public QuotedPriceOrderDO get(Long id) {
        return quotedPriceOrderMapper.selectById(id);
    }

    @Override
    public PageResult<QuotedPriceOrderDO> getPage(QuotedPriceOrderPageReqVO pageReqVO) {
        return quotedPriceOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<QuotedPriceOrderStatisticsRespVO> getStatisticsByMonth(LocalDateTime beginTime, LocalDateTime endTime) {
        log.info("获取报价单按月统计数据 - beginTime: {}, endTime: {}", beginTime, endTime);
        List<QuotedPriceOrderStatisticsRespVO> result = quotedPriceOrderMapper.selectStatisticsByMonth(beginTime, endTime);
        log.info("报价单按月统计数据查询完成 - 结果数量: {}", result != null ? result.size() : 0);
        return result;
    }

    private void validateExists(Long id) {
        if (quotedPriceOrderMapper.selectById(id) == null) {
            throw exception(QUOTED_PRICE_ORDER_NOT_EXISTS);
        }
    }
}


