package ru.efko.testtask.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultant {
    Long id;
    //дивизион
    String division;
    //направление
    String directing;
    //служба
    String service;
    //подразделение
    String subdivision;
    String fullName;
    Integer numOfTasks;
}
