package cn.iocoder.yudao.module.workorder.service.confirmorder;

import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;

/**
 * 确认单文件生成服务
 */
public interface ConfirmOrderFileGenerateService {

    /**
     * 生成确认单文件
     *
     * @param confirmOrder 确认单信息（包含 tagIds 字段）
     * @return 文件路径
     */
    String generateFile(ConfirmOrderDO confirmOrder);

}
