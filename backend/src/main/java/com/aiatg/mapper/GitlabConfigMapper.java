package com.aiatg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aiatg.entity.GitlabConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * GitLab配置Mapper
 */
@Mapper
public interface GitlabConfigMapper extends BaseMapper<GitlabConfig> {
}
