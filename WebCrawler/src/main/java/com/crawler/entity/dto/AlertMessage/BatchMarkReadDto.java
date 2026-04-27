package com.crawler.entity.dto.AlertMessage;

import lombok.Data;
import java.util.List;

@Data
public class BatchMarkReadDto {
    private List<Long> messageIds;
}