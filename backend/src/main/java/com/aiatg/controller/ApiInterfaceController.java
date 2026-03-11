package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.dto.ApiInterfaceDTO;
import com.aiatg.dto.ApiInterfaceQueryDTO;
import com.aiatg.service.ApiInterfaceService;
import com.aiatg.util.SecurityUtil;
import com.aiatg.vo.ApiInterfaceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API接口控制器
 */
@Tag(name = "API接口管理")
@RestController
@RequestMapping("/interface")
public class ApiInterfaceController {
    
    @Autowired
    private ApiInterfaceService apiInterfaceService;
    
    /**
     * 创建接口
     */
    @Operation(summary = "创建接口")
    @PostMapping
    public Result<ApiInterfaceVO> createInterface(@RequestBody ApiInterfaceDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        ApiInterfaceVO vo = apiInterfaceService.createInterface(dto, userId);
        return Result.success(vo);
    }
    
    /**
     * 更新接口
     */
    @Operation(summary = "更新接口")
    @PutMapping("/{id}")
    public Result<ApiInterfaceVO> updateInterface(
        @PathVariable Long id,
        @RequestBody ApiInterfaceDTO dto
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        ApiInterfaceVO vo = apiInterfaceService.updateInterface(id, dto, userId);
        return Result.success(vo);
    }
    
    /**
     * 删除接口
     */
    @Operation(summary = "删除接口")
    @DeleteMapping("/{id}")
    public Result<Void> deleteInterface(@PathVariable Long id) {
        apiInterfaceService.deleteInterface(id);
        return Result.success();
    }
    
    /**
     * 获取接口详情
     */
    @Operation(summary = "获取接口详情")
    @GetMapping("/{id}")
    public Result<ApiInterfaceVO> getInterface(@PathVariable Long id) {
        ApiInterfaceVO vo = apiInterfaceService.getInterfaceById(id);
        return Result.success(vo);
    }
    
    /**
     * 分页查询接口列表
     */
    @Operation(summary = "分页查询接口列表")
    @GetMapping("/list")
    public Result<PageResult<ApiInterfaceVO>> getInterfaceList(ApiInterfaceQueryDTO query) {
        PageResult<ApiInterfaceVO> result = apiInterfaceService.getInterfaceList(query);
        return Result.success(result);
    }
    
    /**
     * 从cURL导入接口
     */
    @Operation(summary = "从cURL导入接口")
    @PostMapping("/import/curl")
    public Result<ApiInterfaceVO> importFromCurl(@RequestBody Map<String, Object> request) {
        String curl = (String) request.get("curl");
        Long projectId = ((Number) request.get("projectId")).longValue();
        
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        ApiInterfaceVO vo = apiInterfaceService.importFromCurl(curl, projectId, userId);
        return Result.success(vo);
    }
    
    /**
     * 发布接口
     */
    @Operation(summary = "发布接口")
    @PutMapping("/{id}/publish")
    public Result<Void> publishInterface(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        apiInterfaceService.publishInterface(id, userId);
        return Result.success();
    }
    
    /**
     * 获取已发布的接口列表
     */
    @Operation(summary = "获取已发布的接口列表")
    @GetMapping("/published")
    public Result<List<ApiInterfaceVO>> getPublishedInterfaces(@RequestParam(required = false) Long projectId) {
        List<ApiInterfaceVO> interfaces = apiInterfaceService.getPublishedInterfaces(projectId);
        return Result.success(interfaces);
    }
}
