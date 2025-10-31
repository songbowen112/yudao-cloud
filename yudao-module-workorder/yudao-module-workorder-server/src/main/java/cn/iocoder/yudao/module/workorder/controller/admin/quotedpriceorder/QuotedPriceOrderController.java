package cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.confirmorder.vo.ConfirmOrderRespVO;
import cn.iocoder.yudao.module.workorder.controller.admin.quotedpriceorder.vo.*;
import cn.iocoder.yudao.module.workorder.dal.dataobject.quotedpriceorder.QuotedPriceOrderDO;
import cn.iocoder.yudao.module.workorder.service.quotedpriceorder.QuotedPriceOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 报价单")
@RestController
@RequestMapping("/workorder/quoted-price-order")
@Validated
public class QuotedPriceOrderController {

    @Resource
    private QuotedPriceOrderService quotedPriceOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建报价单")
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:create')")
    public CommonResult<Long> create(@Valid @RequestBody QuotedPriceOrderSaveReqVO createReqVO) {
        return success(quotedPriceOrderService.create(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新报价单")
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody QuotedPriceOrderSaveReqVO updateReqVO) {
        quotedPriceOrderService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除报价单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        quotedPriceOrderService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得报价单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:query')")
    public CommonResult<QuotedPriceOrderRespVO> get(@RequestParam("id") Long id) {
        QuotedPriceOrderDO data = quotedPriceOrderService.get(id);
        return success(BeanUtils.toBean(data, QuotedPriceOrderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得报价单分页")
    @PreAuthorize("@ss.hasPermission('workorder:quoted-price-order:query')")
    public CommonResult<PageResult<QuotedPriceOrderRespVO>> page(QuotedPriceOrderPageReqVO pageReqVO) {
        PageResult<QuotedPriceOrderDO> pageResult = quotedPriceOrderService.getPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, QuotedPriceOrderRespVO.class));
    }
}


