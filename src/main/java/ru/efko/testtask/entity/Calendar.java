package ru.efko.testtask.entity;


import lombok.Data;

import java.util.List;


/**
 * Не будет использоваться, просто для демонстрации
 */
@Data
public class Calendar {
    Long id;
    List<Event> events;
}
