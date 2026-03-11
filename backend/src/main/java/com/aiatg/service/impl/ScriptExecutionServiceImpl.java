package com.aiatg.service.impl;

import com.aiatg.entity.ScriptExecution;
import com.aiatg.entity.Skill;
import com.aiatg.mapper.ScriptExecutionMapper;
import com.aiatg.service.ScriptExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 脚本执行服务实现类
 */
@Slf4j
@Service
public class ScriptExecutionServiceImpl implements ScriptExecutionService {

    @Resource
    private ScriptExecutionMapper scriptExecutionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long executeScript(Skill skill, Long userId) {
        // 创建执行记录
        ScriptExecution execution = new ScriptExecution();
        execution.setSkillId(skill.getId());
        execution.setExecutionName("执行技能: " + skill.getName());
        execution.setScriptLanguage(skill.getScriptLanguage());
        execution.setScriptContent(skill.getScriptContent());
        execution.setStatus("PENDING");
        execution.setCreateBy(userId);
        
        scriptExecutionMapper.insert(execution);
        log.info("创建脚本执行记录, id: {}, skillId: {}", execution.getId(), skill.getId());
        
        // 异步执行脚本
        asyncExecuteScript(execution);
        
        return execution.getId();
    }

    @Async
    public void asyncExecuteScript(ScriptExecution execution) {
        Long executionId = execution.getId();
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 更新状态为执行中
            execution.setStatus("RUNNING");
            execution.setStartTime(startTime);
            scriptExecutionMapper.updateById(execution);
            
            log.info("开始执行脚本, executionId: {}, language: {}", executionId, execution.getScriptLanguage());
            
            // 根据脚本语言选择执行器
            String output;
            int exitCode;
            
            switch (execution.getScriptLanguage().toLowerCase()) {
                case "python":
                    output = executePython(execution.getScriptContent());
                    exitCode = 0;
                    break;
                case "javascript":
                    output = executeJavaScript(execution.getScriptContent());
                    exitCode = 0;
                    break;
                case "shell":
                    output = executeShell(execution.getScriptContent());
                    exitCode = 0;
                    break;
                default:
                    throw new RuntimeException("不支持的脚本语言: " + execution.getScriptLanguage());
            }
            
            // 更新为成功状态
            LocalDateTime endTime = LocalDateTime.now();
            execution.setStatus("SUCCESS");
            execution.setOutput(output);
            execution.setExitCode(exitCode);
            execution.setEndTime(endTime);
            execution.setDurationMs(java.time.Duration.between(startTime, endTime).toMillis());
            
            scriptExecutionMapper.updateById(execution);
            log.info("脚本执行成功, executionId: {}, durationMs: {}", executionId, execution.getDurationMs());
            
        } catch (Exception e) {
            // 更新为失败状态
            LocalDateTime endTime = LocalDateTime.now();
            execution.setStatus("FAILED");
            execution.setErrorMessage(e.getMessage());
            execution.setEndTime(endTime);
            execution.setDurationMs(java.time.Duration.between(startTime, endTime).toMillis());
            
            scriptExecutionMapper.updateById(execution);
            log.error("脚本执行失败, executionId: {}", executionId, e);
        }
    }

    /**
     * 执行 Python 脚本
     */
    private String executePython(String scriptContent) throws Exception {
        return executeScriptWithCommand("python3", scriptContent, ".py");
    }

    /**
     * 执行 JavaScript 脚本
     */
    private String executeJavaScript(String scriptContent) throws Exception {
        return executeScriptWithCommand("node", scriptContent, ".js");
    }

    /**
     * 执行 Shell 脚本
     */
    private String executeShell(String scriptContent) throws Exception {
        return executeScriptWithCommand("bash", scriptContent, ".sh");
    }

    /**
     * 通用脚本执行方法
     * 
     * @param command 执行命令（python3/node/bash）
     * @param scriptContent 脚本内容
     * @param extension 文件扩展名
     * @return 执行输出
     */
    private String executeScriptWithCommand(String command, String scriptContent, String extension) throws Exception {
        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = File.createTempFile("script_" + UUID.randomUUID(), extension);
            Files.write(tempFile.toPath(), scriptContent.getBytes(StandardCharsets.UTF_8));
            
            // 如果是shell脚本，需要添加执行权限
            if (".sh".equals(extension)) {
                tempFile.setExecutable(true);
            }
            
            // 构建执行命令
            List<String> commands = new ArrayList<>();
            commands.add(command);
            commands.add(tempFile.getAbsolutePath());
            
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // 等待执行完成（最多30秒）
            boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                throw new RuntimeException("脚本执行超时（超过30秒）");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new RuntimeException("脚本执行失败，退出码: " + exitCode + "\n输出: " + output);
            }
            
            return output.toString();
            
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Override
    public String getExecutionStatus(Long executionId) {
        ScriptExecution execution = scriptExecutionMapper.selectById(executionId);
        return execution != null ? execution.getStatus() : null;
    }

    @Override
    public String getExecutionOutput(Long executionId) {
        ScriptExecution execution = scriptExecutionMapper.selectById(executionId);
        return execution != null ? execution.getOutput() : null;
    }
}
