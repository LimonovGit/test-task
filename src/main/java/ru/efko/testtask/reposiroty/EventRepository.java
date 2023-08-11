package ru.efko.testtask.reposiroty;

import ru.efko.testtask.entity.Event;

import java.util.Date;
import java.util.List;

public interface EventRepository {
    List<Event> getAllByCalendarIdAndBetweenDates(Long calendarId, Date dtStart, Date dtEnd);
}
