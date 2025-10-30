package cn.iocoder.yudao.module.workorder.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * workorder 错误码枚举类
 *
 * workorder 系统，使用 1-005-000-000 段
 */
public interface ErrorCodeConstants {

    ErrorCode WORK_ORDER_NOT_EXISTS = new ErrorCode(1005001000, "工单不存在");

}


