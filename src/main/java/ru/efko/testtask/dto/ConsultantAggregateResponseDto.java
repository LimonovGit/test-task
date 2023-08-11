package ru.efko.testtask.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultantAggregateResponseDto {
    String fullName;
    Integer taskSumTime;
    List<String> services;
}
