package com.aiatg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiatg.common.PageResult;
import com.aiatg.dto.ApiInterfaceDTO;
import com.aiatg.dto.ApiInterfaceQueryDTO;
import com.aiatg.entity.ApiInterface;
import com.aiatg.entity.Project;
import com.aiatg.entity.User;
import com.aiatg.mapper.ApiInterfaceMapper;
import com.aiatg.mapper.ProjectMapper;
import com.aiatg.mapper.UserMapper;
import com.aiatg.service.ApiInterfaceService;
import com.aiatg.vo.ApiInterfaceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * API接口服务实现类
 */
@Slf4j
@Service
public class ApiInterfaceServiceImpl implements ApiInterfaceService {
    
    @Autowired
    private ApiInterfaceMapper apiInterfaceMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional
    public ApiInterfaceVO createInterface(ApiInterfaceDTO dto, Long userId) {
        // 验证项目是否存在
        Project project = projectMapper.selectById(dto.getProjectId());
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        ApiInterface apiInterface = new ApiInterface();
        // 排除 Map 类型字段，避免类型转换错误
        BeanUtil.copyProperties(dto, apiInterface, "headers", "params", "authConfig");
        
        // 手动转换 Map 为 JSON 字符串（如果为空或null，存储空对象"{}"）
        if (dto.getHeaders() != null && !dto.getHeaders().isEmpty()) {
            apiInterface.setHeaders(JSONUtil.toJsonStr(dto.getHeaders()));
        } else {
            apiInterface.setHeaders("{}");
        }
        if (dto.getParams() != null && !dto.getParams().isEmpty()) {
            apiInterface.setParams(JSONUtil.toJsonStr(dto.getParams()));
        } else {
            apiInterface.setParams("{}");
        }
        if (dto.getAuthConfig() != null && !dto.getAuthConfig().isEmpty()) {
            apiInterface.setAuthConfig(JSONUtil.toJsonStr(dto.getAuthConfig()));
        } else {
            apiInterface.setAuthConfig("{}");
        }
        
        apiInterface.setCreatedBy(userId);
        apiInterface.setCreatedTime(LocalDateTime.now());
        apiInterface.setUpdatedTime(LocalDateTime.now());
        
        apiInterfaceMapper.insert(apiInterface);
        return getInterfaceById(apiInterface.getId());
    }
    
    @Override
    @Transactional
    public ApiInterfaceVO updateInterface(Long id, ApiInterfaceDTO dto, Long userId) {
        ApiInterface apiInterface = apiInterfaceMapper.selectById(id);
        if (apiInterface == null) {
            throw new RuntimeException("接口不存在");
        }
        
        // 排除 id、审计字段和 Map 类型字段，避免类型转换错误
        BeanUtil.copyProperties(dto, apiInterface, "id", "createdBy", "createdTime", "headers", "params", "authConfig");
        
        // 手动转换 Map 为 JSON 字符串（如果为空或null，存储空对象"{}"）
        if (dto.getHeaders() != null && !dto.getHeaders().isEmpty()) {
            apiInterface.setHeaders(JSONUtil.toJsonStr(dto.getHeaders()));
        } else {
            apiInterface.setHeaders("{}");
        }
        if (dto.getParams() != null && !dto.getParams().isEmpty()) {
            apiInterface.setParams(JSONUtil.toJsonStr(dto.getParams()));
        } else {
            apiInterface.setParams("{}");
        }
        if (dto.getAuthConfig() != null && !dto.getAuthConfig().isEmpty()) {
            apiInterface.setAuthConfig(JSONUtil.toJsonStr(dto.getAuthConfig()));
        } else {
            apiInterface.setAuthConfig("{}");
        }
        
        apiInterface.setUpdatedBy(userId);
        apiInterface.setUpdatedTime(LocalDateTime.now());
        
        apiInterfaceMapper.updateById(apiInterface);
        return getInterfaceById(id);
    }
    
    @Override
    @Transactional
    public void deleteInterface(Long id) {
        apiInterfaceMapper.deleteById(id);
    }
    
    @Override
    public ApiInterfaceVO getInterfaceById(Long id) {
        ApiInterface apiInterface = apiInterfaceMapper.selectById(id);
        if (apiInterface == null) {
            throw new RuntimeException("接口不存在");
        }
        return convertToVO(apiInterface);
    }
    
    @Override
    public PageResult<ApiInterfaceVO> getInterfaceList(ApiInterfaceQueryDTO query) {
        LambdaQueryWrapper<ApiInterface> wrapper = new LambdaQueryWrapper<>();
        
        if (query.getProjectId() != null) {
            wrapper.eq(ApiInterface::getProjectId, query.getProjectId());
        }
        
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.and(w -> w
                .like(ApiInterface::getInterfaceName, query.getKeyword())
                .or()
                .like(ApiInterface::getUrl, query.getKeyword())
                .or()
                .like(ApiInterface::getDescription, query.getKeyword())
            );
        }
        
        if (query.getMethod() != null && !query.getMethod().isEmpty()) {
            wrapper.eq(ApiInterface::getMethod, query.getMethod());
        }
        
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(ApiInterface::getStatus, query.getStatus());
        }
        
        if (query.getCategory() != null && !query.getCategory().isEmpty()) {
            wrapper.eq(ApiInterface::getCategory, query.getCategory());
        }
        
        wrapper.orderByDesc(ApiInterface::getCreatedTime);
        
        Page<ApiInterface> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<ApiInterface> result = apiInterfaceMapper.selectPage(page, wrapper);
        
        List<ApiInterfaceVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResult<ApiInterfaceVO>(
            result.getTotal(), 
            voList, 
            query.getPageNum(), 
            query.getPageSize()
        );
    }
    
    @Override
    @Transactional
    public ApiInterfaceVO importFromCurl(String curl, Long projectId, Long userId) {
        try {
            // 解析cURL命令
            CurlParser parser = new CurlParser(curl);
            
            log.info("解析cURL结果 - Method: {}, URL: {}, Headers: {}, Body: {}", 
                parser.getMethod(), parser.getUrl(), parser.getHeaders(), parser.getBody());
            
            ApiInterfaceDTO dto = new ApiInterfaceDTO();
            dto.setProjectId(projectId);
            dto.setInterfaceName(parser.getUrl()); // 默认使用URL作为名称
            dto.setMethod(parser.getMethod());
            dto.setUrl(parser.getUrl());
            dto.setHeaders(parser.getHeaders());
            dto.setBody(parser.getBody());
            dto.setBodyType("raw");
            dto.setTimeout(30000);
            dto.setStatus("draft");
            
            return createInterface(dto, userId);
        } catch (Exception e) {
            log.error("解析cURL失败", e);
            throw new RuntimeException("cURL格式错误: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void publishInterface(Long id, Long userId) {
        ApiInterface apiInterface = apiInterfaceMapper.selectById(id);
        if (apiInterface == null) {
            throw new RuntimeException("接口不存在");
        }
        
        apiInterface.setStatus("published");
        apiInterface.setUpdatedBy(userId);
        apiInterface.setUpdatedTime(LocalDateTime.now());
        
        apiInterfaceMapper.updateById(apiInterface);
    }
    
    @Override
    public List<ApiInterfaceVO> getPublishedInterfaces(Long projectId) {
        LambdaQueryWrapper<ApiInterface> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiInterface::getStatus, "published");
        
        if (projectId != null) {
            wrapper.eq(ApiInterface::getProjectId, projectId);
        }
        
        wrapper.orderByDesc(ApiInterface::getCreatedTime);
        
        List<ApiInterface> interfaces = apiInterfaceMapper.selectList(wrapper);
        return interfaces.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    /**
     * 转换为VO
     */
    private ApiInterfaceVO convertToVO(ApiInterface apiInterface) {
        ApiInterfaceVO vo = new ApiInterfaceVO();
        
        // 使用CopyOptions忽略无法自动转换的Map类型字段
        CopyOptions options = CopyOptions.create()
            .setIgnoreProperties("headers", "params", "authConfig");
        BeanUtil.copyProperties(apiInterface, vo, options);
        
        // 手动解析JSON字符串为Map（添加try-catch避免解析失败）
        try {
            if (apiInterface.getHeaders() != null && !apiInterface.getHeaders().isEmpty() 
                && !apiInterface.getHeaders().equals("{}")) {
                Map<String, String> headersMap = JSONUtil.toBean(
                    apiInterface.getHeaders(), 
                    new TypeReference<Map<String, String>>() {}, 
                    false
                );
                vo.setHeaders(headersMap);
            } else {
                vo.setHeaders(new HashMap<>());
            }
        } catch (Exception e) {
            log.warn("解析headers失败: {}", e.getMessage());
            vo.setHeaders(new HashMap<>());
        }
        
        try {
            if (apiInterface.getParams() != null && !apiInterface.getParams().isEmpty() 
                && !apiInterface.getParams().equals("{}")) {
                Map<String, String> paramsMap = JSONUtil.toBean(
                    apiInterface.getParams(), 
                    new TypeReference<Map<String, String>>() {}, 
                    false
                );
                vo.setParams(paramsMap);
            } else {
                vo.setParams(new HashMap<>());
            }
        } catch (Exception e) {
            log.warn("解析params失败: {}", e.getMessage());
            vo.setParams(new HashMap<>());
        }
        
        try {
            if (apiInterface.getAuthConfig() != null && !apiInterface.getAuthConfig().isEmpty() 
                && !apiInterface.getAuthConfig().equals("{}")) {
                Map<String, String> authConfigMap = JSONUtil.toBean(
                    apiInterface.getAuthConfig(), 
                    new TypeReference<Map<String, String>>() {}, 
                    false
                );
                vo.setAuthConfig(authConfigMap);
            } else {
                vo.setAuthConfig(new HashMap<>());
            }
        } catch (Exception e) {
            log.warn("解析authConfig失败: {}", e.getMessage());
            vo.setAuthConfig(new HashMap<>());
        }
        
        // 关联查询项目名称
        if (apiInterface.getProjectId() != null) {
            Project project = projectMapper.selectById(apiInterface.getProjectId());
            if (project != null) {
                vo.setProjectName(project.getName());
            }
        }
        
        // 关联查询创建人和更新人
        if (apiInterface.getCreatedBy() != null) {
            User user = userMapper.selectById(apiInterface.getCreatedBy());
            if (user != null) {
                vo.setCreatedByName(user.getUsername());
            }
        }
        
        if (apiInterface.getUpdatedBy() != null) {
            User user = userMapper.selectById(apiInterface.getUpdatedBy());
            if (user != null) {
                vo.setUpdatedByName(user.getUsername());
            }
        }
        
        return vo;
    }
    
    /**
     * cURL解析器（简化版）
     */
    private static class CurlParser {
        private String method = "GET";
        private String url;
        private Map<String, String> headers = new HashMap<>();
        private String body;
        
        public CurlParser(String curl) {
            // 预处理：移除行尾的反斜杠和换行符，将多行 cURL 命令合并为一行
            curl = curl.replaceAll("\\\\\\s*\\n\\s*", " ");
            
            log.info("预处理后的cURL: {}", curl);
            
            // 移除 curl 命令
            curl = curl.replaceFirst("^curl\\s+", "");
            
            // 提取 URL（支持多种格式）
            Pattern urlPattern = Pattern.compile("(?:--location\\s+)?(?:--request\\s+\\w+\\s+)?'([^']+)'|(?:--location\\s+)?(?:--request\\s+\\w+\\s+)?\"([^\"]+)\"|(?:--location\\s+)?(?:--request\\s+\\w+\\s+)?([^\\s\\\\]+)");
            Matcher urlMatcher = urlPattern.matcher(curl);
            if (urlMatcher.find()) {
                url = urlMatcher.group(1) != null ? urlMatcher.group(1) :
                      urlMatcher.group(2) != null ? urlMatcher.group(2) : urlMatcher.group(3);
                log.info("解析URL: {}", url);
            }
            
            // 提取方法（支持 -X 和 --request 两种格式）
            Pattern methodPattern = Pattern.compile("(?:-X|--request)\\s+(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)");
            Matcher methodMatcher = methodPattern.matcher(curl);
            if (methodMatcher.find()) {
                method = methodMatcher.group(1);
                log.info("解析Method: {}", method);
            }
            
            // 提取 Headers（支持 -H 和 --header 两种格式）
            Pattern headerPattern = Pattern.compile("(?:-H|--header)\\s+'([^:]+):\\s*([^']+)'|(?:-H|--header)\\s+\"([^:]+):\\s*([^\"]+)\"");
            Matcher headerMatcher = headerPattern.matcher(curl);
            while (headerMatcher.find()) {
                String key = headerMatcher.group(1) != null ? headerMatcher.group(1) : headerMatcher.group(3);
                String value = headerMatcher.group(2) != null ? headerMatcher.group(2) : headerMatcher.group(4);
                if (key != null && value != null) {
                    headers.put(key.trim(), value.trim());
                    log.info("解析Header - {}: {}", key.trim(), value.trim());
                }
            }
            log.info("总共解析到 {} 个headers", headers.size());
            
            // 提取 Body
            Pattern bodyPattern = Pattern.compile("--data-raw\\s+'([^']+)'|--data-raw\\s+\"([^\"]+)\"|--data\\s+'([^']+)'|--data\\s+\"([^\"]+)\"|-d\\s+'([^']+)'|-d\\s+\"([^\"]+)\"");
            Matcher bodyMatcher = bodyPattern.matcher(curl);
            if (bodyMatcher.find()) {
                for (int i = 1; i <= 6; i++) {
                    if (bodyMatcher.group(i) != null) {
                        body = bodyMatcher.group(i);
                        log.info("解析Body: {}", body);
                        break;
                    }
                }
            }
        }
        
        public String getMethod() {
            return method;
        }
        
        public String getUrl() {
            return url;
        }
        
        public Map<String, String> getHeaders() {
            return headers;
        }
        
        public String getBody() {
            return body;
        }
    }
}
