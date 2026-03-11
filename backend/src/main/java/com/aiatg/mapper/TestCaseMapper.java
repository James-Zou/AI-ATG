package com.aiatg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aiatg.entity.TestCase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试用例Mapper
 */
@Mapper
public interface TestCaseMapper extends BaseMapper<TestCase> {
}
