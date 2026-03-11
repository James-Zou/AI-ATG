package com.aiatg.controller;

import com.aiatg.common.PageResult;
import com.aiatg.common.Result;
import com.aiatg.dto.RequirementDTO;
import com.aiatg.dto.RequirementQueryDTO;
import com.aiatg.service.RequirementService;
import com.aiatg.util.UserHolder;
import com.aiatg.vo.RequirementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 需求控制器
 */
@RestController
@RequestMapping("/requirement")
@CrossOrigin
public class RequirementController {
    
    @Autowired
    private RequirementService requirementService;
    
    /**
     * 创建需求
     */
    @PostMapping
    public Result<RequirementVO> createRequirement(@Valid @RequestBody RequirementDTO dto) {
        try {
            Long userId = getCurrentUserId();
            RequirementVO vo = requirementService.createRequirement(dto, userId);
            return Result.success("创建成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新需求
     */
    @PutMapping("/{id}")
    public Result<RequirementVO> updateRequirement(
        @PathVariable Long id,
        @Valid @RequestBody RequirementDTO dto
    ) {
        try {
            RequirementVO vo = requirementService.updateRequirement(id, dto);
            return Result.success("更新成功", vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除需求
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRequirement(@PathVariable Long id) {
        try {
            requirementService.deleteRequirement(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取需求详情
     */
    @GetMapping("/{id}")
    public Result<RequirementVO> getRequirementById(@PathVariable Long id) {
        try {
            RequirementVO vo = requirementService.getRequirementById(id);
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 分页查询需求列表
     */
    @GetMapping("/list")
    public Result<PageResult<RequirementVO>> getRequirementList(RequirementQueryDTO query) {
        try {
            PageResult<RequirementVO> pageResult = requirementService.getRequirementList(query);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新需求状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            requirementService.updateStatus(id, status);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        String userId = UserHolder.getUserId();
        return userId != null ? Long.valueOf(userId) : null;
    }
}
