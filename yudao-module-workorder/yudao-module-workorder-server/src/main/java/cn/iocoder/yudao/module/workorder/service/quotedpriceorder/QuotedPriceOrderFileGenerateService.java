package cn.iocoder.yudao.module.workorder.service.quotedpriceorder;

import cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder.QuotedPriceOrderDO;

/**
 * 报价单文件生成服务
 */
public interface QuotedPriceOrderFileGenerateService {

    /**
     * 生成报价单文件
     *
     * @param quotedPriceOrder 报价单信息
     * @return 文件路径
     */
    String generateFile(QuotedPriceOrderDO quotedPriceOrder);

}
