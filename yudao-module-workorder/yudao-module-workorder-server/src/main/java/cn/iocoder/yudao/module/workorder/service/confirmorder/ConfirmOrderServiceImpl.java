package cn.iocoder.yudao.module.workorder.service.confirmorder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderSaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.tag.WorkorderTagDO;
import cn.iocoder.yudao.module.workorder.dal.mysql.confirmorder.ConfirmOrderMapper;
import cn.iocoder.yudao.module.workorder.service.tag.WorkorderTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.workorder.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class ConfirmOrderServiceImpl implements ConfirmOrderService {

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private ConfirmOrderFileGenerateService fileGenerateService;

    @Resource
    private WorkorderTagService workorderTagService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ConfirmOrderSaveReqVO createReqVO) {
        // 1. 保存确认单信息
        ConfirmOrderDO entity = BeanUtils.toBean(createReqVO, ConfirmOrderDO.class);
        // 清除creator和updater等字段，让MyBatis-Plus自动填充
        entity.clean();
        
        // 验证必填字段（用于调试）
        log.info("创建确认单 - 工单名称: '{}', 标的企业: '{}', 文件类型: {}", 
                entity.getName(), entity.getContractCompanyName(), entity.getFileType());
        
        confirmOrderMapper.insert(entity);
        
        // 重新查询以确保所有字段都已正确设置（特别是自增ID）
        entity = confirmOrderMapper.selectById(entity.getId());

        // 2. 生成文件（如果文件类型不为空）
        if (entity.getFileType() != null) {
            log.info("开始生成文件 - 标签IDs: {}", entity.getTagIds());
            String fileUrl = fileGenerateService.generateFile(entity);
            // 更新文件路径
            entity.setFileUrl(fileUrl);
            confirmOrderMapper.updateById(entity);
        }

        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ConfirmOrderSaveReqVO updateReqVO) {
        validateExists(updateReqVO.getId());
        // 1. 先查询现有数据，确保包含所有字段
        ConfirmOrderDO existingEntity = confirmOrderMapper.selectById(updateReqVO.getId());
        if (existingEntity == null) {
            throw exception(CONFIRM_ORDER_NOT_EXISTS);
        }
        
        // 2. 将更新数据复制到现有实体（只更新非空字段）
        BeanUtils.copyProperties(updateReqVO, existingEntity);
        // 清除creator和updater等字段，让MyBatis-Plus自动填充（防止前端传递这些字段）
        existingEntity.clean();
        
        // 3. 更新确认单信息
        confirmOrderMapper.updateById(existingEntity);

        // 4. 生成文件（如果文件类型不为空）
        if (existingEntity.getFileType() != null) {
            log.info("开始生成文件 - 标签IDs: {}", existingEntity.getTagIds());
            String fileUrl = fileGenerateService.generateFile(existingEntity);
            // 更新文件路径
            existingEntity.setFileUrl(fileUrl);
            confirmOrderMapper.updateById(existingEntity);
        }
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

    @Override
    public List<ConfirmOrderDO> getListByInitStatus() {
        return confirmOrderMapper.selectListByInitStatus();
    }

    private void validateExists(Long id) {
        if (confirmOrderMapper.selectById(id) == null) {
            throw exception(CONFIRM_ORDER_NOT_EXISTS);
        }
    }

    /**
     * 根据标签ID字符串获取标签名称列表
     *
     * @param tagIds 标签ID字符串，多个用逗号分隔
     * @return 标签名称列表
     */
    private List<String> getTagNames(String tagIds) {
        if (StrUtil.isBlank(tagIds)) {
            return new ArrayList<>();
        }

        // 解析标签ID
        List<Long> tagIdList = Arrays.stream(tagIds.split(","))
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(tagIdList)) {
            return new ArrayList<>();
        }

        // 查询标签信息
        List<WorkorderTagDO> tags = new ArrayList<>();
        for (Long tagId : tagIdList) {
            WorkorderTagDO tag = workorderTagService.get(tagId);
            if (tag != null) {
                tags.add(tag);
            }
        }

        // 提取标签名称
        return tags.stream()
                .map(WorkorderTagDO::getTagName)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }
}


