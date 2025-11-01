package cn.iocoder.yudao.module.workorder.controller.admin.tag;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.workorder.controller.admin.tag.vo.*;
import cn.iocoder.yudao.module.workorder.dal.dataobject.tag.WorkorderTagDO;
import cn.iocoder.yudao.module.workorder.service.tag.WorkorderTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 标签")
@RestController
@RequestMapping("/workorder/tag")
@Validated
public class WorkorderTagController {

    @Resource
    private WorkorderTagService workorderTagService;

    @PostMapping("/create")
    @Operation(summary = "创建标签")
    @PreAuthorize("@ss.hasPermission('workorder:tag:create')")
    public CommonResult<Long> create(@Valid @RequestBody WorkorderTagSaveReqVO createReqVO) {
        return success(workorderTagService.create(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新标签")
    @PreAuthorize("@ss.hasPermission('workorder:tag:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody WorkorderTagSaveReqVO updateReqVO) {
        workorderTagService.update(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除标签")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:tag:delete')")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        workorderTagService.delete(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得标签")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('workorder:tag:query')")
    public CommonResult<WorkorderTagRespVO> get(@RequestParam("id") Long id) {
        WorkorderTagDO data = workorderTagService.get(id);
        return success(BeanUtils.toBean(data, WorkorderTagRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得标签分页")
    @PreAuthorize("@ss.hasPermission('workorder:tag:query')")
    public CommonResult<PageResult<WorkorderTagRespVO>> page(WorkorderTagPageReqVO pageReqVO) {
        PageResult<WorkorderTagDO> pageResult = workorderTagService.getPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WorkorderTagRespVO.class));
    }

    @GetMapping("/list-parent")
    @Operation(summary = "获得所有一级标签")
    @Parameter(name = "status", description = "状态，可选。不传则查询所有状态的标签，传值则按状态过滤（0=启用，1=禁用）", required = false)
    @PreAuthorize("@ss.hasPermission('workorder:tag:query')")
    public CommonResult<List<WorkorderTagRespVO>> getParentTagList(@RequestParam(value = "status", required = false) Integer status) {
        List<WorkorderTagDO> list = workorderTagService.getListByParentTagIdIsNull(status);
        return success(BeanUtils.toBean(list, WorkorderTagRespVO.class));
    }

    @GetMapping("/list-by-parent")
    @Operation(summary = "根据一级标签获得所有二级标签")
    @Parameter(name = "parentTagId", description = "一级标签编号", required = true)
    @Parameter(name = "status", description = "状态，可选。不传则查询所有状态的标签，传值则按状态过滤（0=启用，1=禁用）", required = false)
    @PreAuthorize("@ss.hasPermission('workorder:tag:query')")
    public CommonResult<List<WorkorderTagRespVO>> getTagListByParent(
            @RequestParam("parentTagId") Long parentTagId,
            @RequestParam(value = "status", required = false) Integer status) {
        List<WorkorderTagDO> list = workorderTagService.getListByParentTagId(parentTagId, status);
        return success(BeanUtils.toBean(list, WorkorderTagRespVO.class));
    }
}


