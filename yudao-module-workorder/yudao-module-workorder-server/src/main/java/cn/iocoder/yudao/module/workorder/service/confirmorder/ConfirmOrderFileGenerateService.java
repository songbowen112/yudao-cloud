package cn.iocoder.yudao.module.workorder.service.confirmorder;

import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;

import java.util.List;

/**
 * 确认单文件生成服务
 */
public interface ConfirmOrderFileGenerateService {

    /**
     * 生成确认单文件
     *
     * @param confirmOrder 确认单信息
     * @param tagNames 标签名称列表
     * @return 文件路径
     */
    String generateFile(ConfirmOrderDO confirmOrder, List<String> tagNames);

}
