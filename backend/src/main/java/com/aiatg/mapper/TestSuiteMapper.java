package com.aiatg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aiatg.entity.TestSuite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 测试套件Mapper
 */
@Mapper
public interface TestSuiteMapper extends BaseMapper<TestSuite> {
}
