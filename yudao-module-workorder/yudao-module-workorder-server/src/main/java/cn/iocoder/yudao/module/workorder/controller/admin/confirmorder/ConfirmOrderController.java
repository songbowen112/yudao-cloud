package cn.iocoder.yudao.module.workorder.controller.admin.confirmorder;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.*;
import cn.iocoder.yudao.module.workorder.dal.dataobject.confirmorder.ConfirmOrderDO;
import cn.iocoder.yudao.module.workorder.service.confirmorder.ConfirmOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 确认单")
@RestController
@RequestMapping("/workorder/confirm-order")
@Validated
public class ConfirmOrderController {

    @Resource
    private ConfirmOrderService confirmOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建确认单")
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:create')")
    public CommonResult<Long> create(@Valid @RequestBody ConfirmOrderSaveReqVO createReqVO) {
        return success(confirmOrderService.create(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新确认单")
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ConfirmOrderSaveReqVO updateReqVO) {
        confirmOrderService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除确认单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        confirmOrderService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得确认单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:query')")
    public CommonResult<ConfirmOrderRespVO> get(@RequestParam("id") Long id) {
        ConfirmOrderDO data = confirmOrderService.get(id);
        return success(BeanUtils.toBean(data, ConfirmOrderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得确认单分页")
    @PreAuthorize("@ss.hasPermission('workorder:confirm-order:query')")
    public CommonResult<PageResult<ConfirmOrderRespVO>> page(ConfirmOrderPageReqVO pageReqVO) {
        PageResult<ConfirmOrderDO> pageResult = confirmOrderService.getPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ConfirmOrderRespVO.class));
    }
}


