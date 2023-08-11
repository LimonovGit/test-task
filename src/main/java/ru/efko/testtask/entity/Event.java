package ru.efko.testtask.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    Long id;
    Long calendarId;
    Date dtStart;
    Date dtEnd;
}
