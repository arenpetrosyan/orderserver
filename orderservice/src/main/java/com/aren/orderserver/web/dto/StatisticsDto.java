package com.aren.orderserver.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsDto {

    private Long processed;
    private Long inProcess;
    private Long ready;
    private Long total;

}
