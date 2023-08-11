package ru.efko.testtask.reposiroty.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.efko.testtask.entity.Event;
import ru.efko.testtask.reposiroty.EventRepository;

import java.util.Date;
import java.util.List;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {
    JdbcTemplate jdbcTemplate;
    static String GET_ALL_BY_CALENDAR_QUERY = "select e.id, e.calendar_id, e.dt_start, e.dt_end " +
            "from events e " +
            "where e.calendarId = ? and e.dtStart >= ? and " +
            "e.dtEnd <= ?";

    @Override
    public List<Event> getAllByCalendarIdAndBetweenDates(Long calendarId, Date dtStart, Date dtEnd) {
        return jdbcTemplate.query(GET_ALL_BY_CALENDAR_QUERY, (rs, i) -> Event.builder()
                .id(rs.getLong(1))
                .calendarId(rs.getLong(2))
                .dtStart(rs.getDate(3))
                .dtEnd(rs.getDate(4))
                .build(), calendarId, dtStart, dtEnd);
    }
}
