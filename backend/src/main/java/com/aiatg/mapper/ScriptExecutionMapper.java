package com.aiatg.mapper;

import com.aiatg.entity.ScriptExecution;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 脚本执行记录Mapper接口
 */
@Mapper
public interface ScriptExecutionMapper extends BaseMapper<ScriptExecution> {
}
