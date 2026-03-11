package com.aiatg.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aiatg.entity.PromptTemplate;
import com.aiatg.mapper.PromptTemplateMapper;
import com.aiatg.service.PromptTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词模板服务实现类
 */
@Service
public class PromptTemplateServiceImpl implements PromptTemplateService {
    
    @Autowired
    private PromptTemplateMapper promptTemplateMapper;
    
    @Override
    public PromptTemplate createTemplate(PromptTemplate template, Long userId) {
        template.setCreatedBy(userId);
        template.setCreatedTime(LocalDateTime.now());
        template.setStatus(1);
        
        if (template.getIsDefault() == null) {
            template.setIsDefault(0);
        }
        
        promptTemplateMapper.insert(template);
        return template;
    }
    
    @Override
    public PromptTemplate updateTemplate(Long id, PromptTemplate template) {
        PromptTemplate existing = promptTemplateMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("模板不存在");
        }
        
        if (template.getName() != null) {
            existing.setName(template.getName());
        }
        if (template.getDescription() != null) {
            existing.setDescription(template.getDescription());
        }
        if (template.getContent() != null) {
            existing.setContent(template.getContent());
        }
        if (template.getStatus() != null) {
            existing.setStatus(template.getStatus());
        }
        if (template.getIsDefault() != null) {
            existing.setIsDefault(template.getIsDefault());
        }
        
        existing.setUpdatedTime(LocalDateTime.now());
        promptTemplateMapper.updateById(existing);
        
        return existing;
    }
    
    @Override
    public void deleteTemplate(Long id) {
        PromptTemplate template = promptTemplateMapper.selectById(id);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        promptTemplateMapper.deleteById(id);
    }
    
    @Override
    public PromptTemplate getTemplateById(Long id) {
        return promptTemplateMapper.selectById(id);
    }
    
    @Override
    public List<PromptTemplate> getAllTemplates() {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getStatus, 1);
        wrapper.orderByDesc(PromptTemplate::getIsDefault);
        wrapper.orderByDesc(PromptTemplate::getCreatedTime);
        return promptTemplateMapper.selectList(wrapper);
    }
    
    @Override
    public PromptTemplate getDefaultTemplate(String templateType) {
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getTemplateType, templateType);
        wrapper.eq(PromptTemplate::getIsDefault, 1);
        wrapper.eq(PromptTemplate::getStatus, 1);
        wrapper.last("LIMIT 1");
        
        return promptTemplateMapper.selectOne(wrapper);
    }
    
    @Override
    public String renderTemplate(Long templateId, Object data) {
        PromptTemplate template = getTemplateById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }
        
        String content = template.getContent();
        JSONObject jsonData = JSONUtil.parseObj(data);
        
        // 使用正则表达式替换模板变量
        Pattern pattern = Pattern.compile("\\{\\{(\\w+)\\}\\}");
        Matcher matcher = pattern.matcher(content);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = jsonData.getStr(key, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
}
