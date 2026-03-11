package com.aiatg.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 套件用例顺序调整DTO
 */
@Data
public class SuiteCaseOrderDTO {
    
    @NotNull(message = "套件ID不能为空")
    private Long suiteId;
    
    @NotEmpty(message = "用例顺序不能为空")
    private List<Long> caseIds;
}
