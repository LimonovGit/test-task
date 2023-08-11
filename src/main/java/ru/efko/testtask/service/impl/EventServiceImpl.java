package ru.efko.testtask.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.efko.testtask.entity.Event;
import ru.efko.testtask.reposiroty.EventRepository;
import ru.efko.testtask.service.EventService;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {
    EventRepository eventRepository;

    /**
     * Сделайте рефакторинг
     * В результате рефакторинга перенес всю логику в запрос, а так же добавил индексы
     * на calendarId и dtStart, dtEnd(см. db/changelog/table/event.sql)
     * @param calendarId - id календаря
     * @param dtStart - дата начала
     * @param dtEnd - дата окончания
     * @return список событий
     */
    @Override
    public List<Event> getAllByCalendarIdAndBetweenDates(Long calendarId, Date dtStart, Date dtEnd) {
        return eventRepository.getAllByCalendarIdAndBetweenDates(calendarId, dtStart, dtEnd);
    }
}
