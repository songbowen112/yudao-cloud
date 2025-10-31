package cn.iocoder.yudao.module.workorder.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * workorder 错误码枚举类
 *
 * workorder 系统，使用 1-005-000-000 段
 */
public interface ErrorCodeConstants {

    ErrorCode WORK_ORDER_NOT_EXISTS = new ErrorCode(1005001000, "工单不存在");
    ErrorCode CONFIRM_ORDER_NOT_EXISTS = new ErrorCode(1005001001, "确认单不存在");
    ErrorCode QUOTED_PRICE_ORDER_NOT_EXISTS = new ErrorCode(1005001002, "报价单不存在");
    ErrorCode WORKORDER_COMPANY_NOT_EXISTS = new ErrorCode(1005001003, "企业不存在");
    ErrorCode WORKORDER_TAG_NOT_EXISTS = new ErrorCode(1005001004, "标签不存在");

}


