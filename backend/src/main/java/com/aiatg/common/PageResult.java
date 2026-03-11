package com.aiatg.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> {
    
    private Long total;
    
    private List<T> records;
    
    private Integer pageNum;
    
    private Integer pageSize;
    
    private Integer totalPages;
    
    public PageResult(Long total, List<T> records, Integer pageNum, Integer pageSize) {
        this.total = total;
        this.records = records;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
}
