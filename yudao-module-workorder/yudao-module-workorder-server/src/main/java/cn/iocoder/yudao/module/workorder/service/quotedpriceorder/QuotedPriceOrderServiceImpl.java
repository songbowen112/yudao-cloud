package cn.iocoder.yudao.module.workorder.service.quotedpriceorder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderPageReqVO;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.QuotedPriceOrderSaveReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder.QuotedPriceOrderDO;
import cn.iocoder.yudao.module.workorder.dal.mysql.quotedpriceorder.QuotedPriceOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.workorder.enums.ErrorCodeConstants.*;

@Service
@Validated
public class QuotedPriceOrderServiceImpl implements QuotedPriceOrderService {

    @Resource
    private QuotedPriceOrderMapper quotedPriceOrderMapper;

    @Override
    public Long create(QuotedPriceOrderSaveReqVO createReqVO) {
        QuotedPriceOrderDO entity = BeanUtils.toBean(createReqVO, QuotedPriceOrderDO.class);
        quotedPriceOrderMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(QuotedPriceOrderSaveReqVO updateReqVO) {
        validateExists(updateReqVO.getId());
        QuotedPriceOrderDO entity = BeanUtils.toBean(updateReqVO, QuotedPriceOrderDO.class);
        quotedPriceOrderMapper.updateById(entity);
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

    private void validateExists(Long id) {
        if (quotedPriceOrderMapper.selectById(id) == null) {
            throw exception(QUOTED_PRICE_ORDER_NOT_EXISTS);
        }
    }
}


