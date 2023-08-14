# test-task
Выполнение тестового задания:
## 1) Условие:
  ```
•	Необходимо распарсить предоставленный файл структуры с сохранением иерархии подразделений и записи ответственных к подразделению.
    Результат парсинга должен собрать SQL запрос и записать результат в базу результат.
•	С базы необходимо выгрузить данные в JSON формате по консультантам.
    Формат данных должен соответствовать стандарту REST в выгрузке должны быть список консультантов со списком служб и суммой по количеству задач.

  ```
## Решение: 

### 1.1) Для реализации парсинга был реализован рекурсивный алгоритм обхода.
  
```java
static final Map<Integer, String> NAME_MAPPING = new HashMap<>();
static final Integer CONSULTANT_CELL = 4;
static final Integer NUM_OF_TASK_CELL = 5;
static final Integer SUBDIVISION_CELL = 3;

static {
    NAME_MAPPING.put(0, "Дивизион");
    NAME_MAPPING.put(1, "Направление");
    NAME_MAPPING.put(2, "Служба");
    NAME_MAPPING.put(3, "Подразделение");
}
public void convertToSqlQueryAndSave(Workbook workbook) {
      StringBuilder builderQuery = new StringBuilder("INSERT INTO CONSULTANT (division, directing, service, subdivision, full_name, num_of_tasks) values ");
      Sheet sheet = workbook.getSheetAt(0);
      Map<String, String> currState = new HashMap<>();
      recursiveConvert(sheet, 2, 0, currState, builderQuery);
      builderQuery.replace(builderQuery.length()-2, builderQuery.length()-1, ";");
      consultantRepository.batchSave(builderQuery.toString());
  }

private int recursiveConvert(Sheet sheet,
                             int currRowNum,
                             int currCellNum,
                             Map<String, String> currState,
                             StringBuilder result){
    Cell currCell = sheet.getRow(currRowNum).getCell(currCellNum);
    String cellName = NAME_MAPPING.get(currCellNum);
    currState.put(cellName, currCell.getStringCellValue());
    int maxRow = currRowNum;
    //если дошли до подразделения, то готовы собрать консультанта
    if(currCellNum >= SUBDIVISION_CELL){
        addConsultantToAnswer(currState, result, sheet, currRowNum);
    }else{
        //иначе пробуем спускаться по дереву
        Cell nextCell = nullSafeGetCell(currRowNum+1, currCellNum+1, sheet);
        if(nextCell == null){
            //на входных данных, это значит что есть служба, но нет подразделения
            addConsultantToAnswer(currState, result, sheet, currRowNum);
        }else{
            //переход на след. уровень вложенности
            maxRow = recursiveConvert(sheet, currRowNum+1, currCellNum+1, currState, result);
        }
    }

    int rowDiff = maxRow - currRowNum;
    int nextRow = currRowNum + rowDiff + 1;
    //исходя из максимальной глубины обхода вычисляем на сколько нужно спуститься вниз
    Cell nextCellObj = nullSafeGetCell(nextRow, currCellNum, sheet);
    if(nextCellObj != null){
        //переход на след. элемент текущей вложенности
        return recursiveConvert(sheet, nextRow, currCellNum, currState, result);
    }else{
        //текущая вложенность закончилась, очищаем состояние и идем назад
        currState.put(cellName, null);
        return maxRow;
    }
}
```
После сбора запроса, результат сохраняется в базу.

### 1.2) Для запроса всех консультантов использовал такой запрос

```sql
SELECT c.full_name as fullName,
  sum(c.num_of_tasks) as sumOfTasks,
  ARRAY_AGG(c.service) as services
  FROM consultant c GROUP BY 1

```

## 2) Условие:

Сделайте рефакторинг
   
```java
public List<Event> getEventByData(int id, Date data1, Date date2) {
    List<Event> eventFinal = new ArrayList<>();
    List<Event> event = calendarRepository.findById(id).get().getEvents();
    event.forEach(event1 -> {
        if(event1.getDt_start().compareTo(data1) >= 0 && event1.getDt_end().compareTo(date2) <= 0){
            eventFinal.add(event1);
        }
    });
  return eventFinal;
}
```

## Решение:

Вся логика из сервиса ушла в запрос.

```java
public List<Event> getAllByCalendarIdAndBetweenDates(Long calendarId, Date dtStart, Date dtEnd) {
        return eventRepository.getAllByCalendarIdAndBetweenDates(calendarId, dtStart, dtEnd);
}

-----------------------------------------------

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
```
и создание таблицы:

```sql

CREATE TABLE event (
    id bigserial not null primary key,
    calendar_id bigint not null references calendar(id),
    dt_start timestamp,
    dt_end timestamp
);

CREATE INDEX calendar_id_idx ON event USING btree (calendar_id);
CREATE INDEX between_idx on event USING btree(dt_start, dt_end)

```
