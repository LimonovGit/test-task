package ru.efko.testtask.service;

import ru.efko.testtask.entity.Event;

import java.util.Date;
import java.util.List;

public interface EventService {
    List<Event> getAllByCalendarIdAndBetweenDates(Long calendarId, Date dtStart, Date dtEnd);
}
