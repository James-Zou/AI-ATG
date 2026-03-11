package com.aiatg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aiatg.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提示词模板Mapper
 */
@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {
}
