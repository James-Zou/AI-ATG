package com.aiatg.controller;

import com.aiatg.common.Result;
import com.aiatg.dto.ChatMessageDTO;
import com.aiatg.service.ATGBotService;
import com.aiatg.vo.ChatResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * ATGBot控制器
 */
@Slf4j
@RestController
@RequestMapping("/atgbot")
public class ATGBotController {
    
    @Autowired
    private ATGBotService atgBotService;
    
    /**
     * 发送聊天消息并分析指令
     */
    @PostMapping("/chat")
    public Result<ChatResponseVO> chat(@RequestBody ChatMessageDTO dto, HttpServletRequest request) {
        try {
            // 从session获取用户ID（简化处理，实际项目中应从认证信息获取）
            Long userId = getUserIdFromSession(request);
            
            ChatResponseVO response = atgBotService.processMessage(dto.getMessage(), userId);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("处理聊天消息失败", e);
            return Result.error("处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行测试套件
     */
    @PostMapping("/execute/{suiteId}")
    public Result<Map<String, Object>> execute(@PathVariable Long suiteId, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromSession(request);
            
            Long executionId = atgBotService.executeSuite(suiteId, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("executionId", executionId);
            result.put("message", "测试套件执行已启动");
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("执行测试套件失败", e);
            return Result.error("执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 从session获取用户ID
     * 简化处理，实际项目应从JWT或其他认证机制获取
     */
    private Long getUserIdFromSession(HttpServletRequest request) {
        Object userId = request.getSession().getAttribute("userId");
        if (userId != null) {
            return Long.valueOf(userId.toString());
        }
        return 1L; // 默认用户ID
    }
}
