package cn.iocoder.yudao.module.workorder.controller.admin.company;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.company.vo.*;
import cn.iocoder.yudao.module.workorder.dal.dataobject.company.WorkorderCompanyDO;
import cn.iocoder.yudao.module.workorder.service.company.WorkorderCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 企业")
@RestController
@RequestMapping("/workorder/company")
@Validated
public class WorkorderCompanyController {

    @Resource
    private WorkorderCompanyService workorderCompanyService;

    @PostMapping("/create")
    @Operation(summary = "创建企业")
    @PreAuthorize("@ss.hasPermission('workorder:company:create')")
    public CommonResult<Long> create(@Valid @RequestBody WorkorderCompanySaveReqVO createReqVO) {
        return success(workorderCompanyService.create(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新企业")
    @PreAuthorize("@ss.hasPermission('workorder:company:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody WorkorderCompanySaveReqVO updateReqVO) {
        workorderCompanyService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除企业")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:company:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        workorderCompanyService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得企业")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:company:query')")
    public CommonResult<WorkorderCompanyRespVO> get(@RequestParam("id") Long id) {
        WorkorderCompanyDO data = workorderCompanyService.get(id);
        return success(BeanUtils.toBean(data, WorkorderCompanyRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得企业分页")
    @PreAuthorize("@ss.hasPermission('workorder:company:query')")
    public CommonResult<PageResult<WorkorderCompanyRespVO>> page(WorkorderCompanyPageReqVO pageReqVO) {
        PageResult<WorkorderCompanyDO> pageResult = workorderCompanyService.getPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WorkorderCompanyRespVO.class));
    }
}


