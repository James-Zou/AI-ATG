package com.aiatg.controller;

import com.aiatg.common.Result;
import com.aiatg.dto.ImportSkillDTO;
import com.aiatg.dto.SkillDTO;
import com.aiatg.service.SkillService;
import com.aiatg.util.UserHolder;
import com.aiatg.vo.SkillVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 技能管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/skills")
public class SkillController {

    @Resource
    private SkillService skillService;

    /**
     * 获取技能列表
     */
    @GetMapping("/list")
    public Result<Page<SkillVO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<SkillVO> page = skillService.listSkills(name, type, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 创建技能（脚本类型）
     */
    @PostMapping("/create")
    public Result<Long> create(@Validated @RequestBody SkillDTO skillDTO) {
        Long userId = Long.valueOf(UserHolder.get().getUserId());
        Long skillId = skillService.createSkill(skillDTO, userId);
        return Result.success(skillId);
    }

    /**
     * 从测试套件导入技能
     */
    @PostMapping("/import-from-suite")
    public Result<Long> importFromSuite(@Validated @RequestBody ImportSkillDTO importSkillDTO) {
        Long userId = Long.valueOf(UserHolder.get().getUserId());
        
        Long skillId = skillService.importFromTestSuite(
            importSkillDTO.getTestSuiteId(), 
            importSkillDTO.getName(), 
            importSkillDTO.getDescription(), 
            userId,
            importSkillDTO.getConfigData()
        );
        return Result.success(skillId);
    }

    /**
     * 更新技能
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                                @Validated @RequestBody SkillDTO skillDTO) {
        Long userId = Long.valueOf(UserHolder.get().getUserId());
        skillService.updateSkill(id, skillDTO, userId);
        return Result.success();
    }

    /**
     * 删除技能
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return Result.success();
    }

    /**
     * 获取技能详情
     */
    @GetMapping("/{id}")
    public Result<SkillVO> detail(@PathVariable Long id) {
        SkillVO vo = skillService.getSkillById(id);
        return Result.success(vo);
    }

    /**
     * 执行技能
     */
    @PostMapping("/{id}/execute")
    public Result<Long> execute(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> parameters) {
        Long userId = Long.valueOf(UserHolder.get().getUserId());
        Long executionId = skillService.executeSkill(id, userId, parameters != null ? parameters : new java.util.HashMap<>());
        return Result.success(executionId);
    }
}
