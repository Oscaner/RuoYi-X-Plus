package com.ruoyi.demo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.demo.domain.bo.TestTreeBo;
import com.ruoyi.demo.domain.to.TestTreeQuery;
import com.ruoyi.demo.domain.vo.TestTreeVo;
import com.ruoyi.demo.service.ITestTreeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * 测试树表Controller
 * !!!不推荐模块内写控制器
 * @author Lion Li
 * @date 2021-07-26
 */
@Validated
@Tag(description = "测试树表控制器", name = "TestTreeService")
@RequiredArgsConstructor
@RestController
@RequestMapping("/demo/tree")
public class TestTreeController {

    private final ITestTreeService iTestTreeService;

    /**
     * 查询测试树表列表
     */
    @Operation(description = "查询测试树表列表", summary = "TestTreeServiceGetList")
    @SaCheckPermission("demo:tree:list")
    @GetMapping("/list")
    public R<List<TestTreeVo>> list(@Validated TestTreeQuery query) {
        List<TestTreeVo> list = iTestTreeService.queryList(query);
        return R.ok(list);
    }

    /**
     * 导出测试树表列表
     */
    @Operation(description = "导出测试树表列表", summary = "TestTreeServiceGetExport")
    @SaCheckPermission("demo:tree:export")
    @Log(title = "测试树表", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(@Validated TestTreeQuery query, HttpServletResponse response) {
        List<TestTreeVo> list = iTestTreeService.queryList(query);
        ExcelUtil.exportExcel(list, "测试树表", TestTreeVo.class, response);
    }

    /**
     * 获取测试树表详细信息
     */
    @Operation(description = "获取测试树表详细信息", summary = "TestTreeServiceGetGetInfo")
    @SaCheckPermission("demo:tree:query")
    @GetMapping("/{id}")
    public R<TestTreeVo> getInfo(@Parameter(description = "测试树ID")
                                          @NotNull(message = "主键不能为空")
                                          @PathVariable("id") Long id) {
        return R.ok(iTestTreeService.queryById(id));
    }

    /**
     * 新增测试树表
     */
    @Operation(description = "新增测试树表", summary = "TestTreeServicePostAdd")
    @SaCheckPermission("demo:tree:add")
    @Log(title = "测试树表", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping()
    public R<Void> add(@Validated @RequestBody TestTreeBo bo) {
        return toAjax(iTestTreeService.insertByBo(bo) ? 1 : 0);
    }

    /**
     * 修改测试树表
     */
    @Operation(description = "修改测试树表", summary = "TestTreeServicePutEdit")
    @SaCheckPermission("demo:tree:edit")
    @Log(title = "测试树表", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping()
    public R<Void> edit(@Validated @RequestBody TestTreeBo bo) {
        return toAjax(iTestTreeService.updateByBo(bo) ? 1 : 0);
    }

    /**
     * 删除测试树表
     */
    @Operation(description = "删除测试树表", summary = "TestTreeServiceDeleteRemove")
    @SaCheckPermission("demo:tree:remove")
    @Log(title = "测试树表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@Parameter(description = "测试树ID串")
                                   @NotEmpty(message = "主键不能为空")
                                   @PathVariable Long[] ids) {
        return toAjax(iTestTreeService.deleteWithValidByIds(Arrays.asList(ids), true) ? 1 : 0);
    }

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected R<Void> toAjax(int rows) {
        return rows > 0 ? R.ok() : R.fail();
    }
}
