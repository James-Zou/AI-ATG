package com.aiatg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aiatg.entity.WebhookRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * Webhook记录Mapper
 */
@Mapper
public interface WebhookRecordMapper extends BaseMapper<WebhookRecord> {
}
