# Тестовое задание
- [Задача 1.1](#задача-11)
- [Задача 1.2](#задача-12)
- [Задача 2](#задача-2)


## Задача 1.1
### Условие:

> Необходимо распарсить предоставленный файл структуры с сохранением иерархии подразделений и записи ответственных к подразделению.
    Результат парсинга должен собрать SQL запрос и записать результат в базу результат.

## Решение: 

Для реализации парсинга использовался модернизированный DFS с запоминанием самого глубокого состояния



```java
ru/efko/testtask/service/impl/ParserServiceImpl.java
======================================================================

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
    //очищаем текущее состояние
    currState.put(cellName, null);

    int rowDiff = maxRow - currRowNum;
    int nextRow = currRowNum + rowDiff + 1;
    //исходя из максимальной глубины обхода вычисляем на сколько нужно спуститься вниз
    Cell nextCell = nullSafeGetCell(nextRow, currCellNum, sheet);
    if(nextCell != null){
        //переход на след. элемент текущей вложенности
        return recursiveConvert(sheet, nextRow, currCellNum, currState, result);
    }else{
        //текущая вложенность закончилась, идем назад
        return maxRow;
    }
}

private void addConsultantToAnswer(Map<String, String> currState, StringBuilder result, Sheet sheet, int row){
    Row sheetRow = sheet.getRow(row);
    //небезопасно, лучше использовать prepareStatement, но по условию нужно было собрать запрос
    String newAppend = String.format("('%s', '%s', '%s', '%s', '%s', %d), ",
            currState.get("Дивизион"),
            currState.get("Направление"),
            currState.get("Служба"),
            currState.get("Подразделение"),
            sheetRow.getCell(CONSULTANT_CELL).getStringCellValue(),
            (int)(sheetRow.getCell(NUM_OF_TASK_CELL).getNumericCellValue() * 100));
    result.append(newAppend);
}

private Cell nullSafeGetCell(int row, int cell, Sheet sheet){
    if(row > sheet.getLastRowNum())
        return null;
    if(cell > sheet.getRow(row).getLastCellNum())
        return null;
    return sheet.getRow(row).getCell(cell);
}
```

## Задача 1.2

### Условие:

> С базы необходимо выгрузить данные в JSON формате по консультантам.
> Формат данных должен соответствовать стандарту REST в выгрузке должны быть список консультантов со списком служб и суммой по количеству задач.
 
 ### Решение: 
 
Для запроса всех консультантов использовал такой запрос.

```sql
SELECT c.full_name as fullName,
sum(c.num_of_tasks) as sumOfTasks,
ARRAY_AGG(c.service) as services
FROM consultant c GROUP BY 1

```

Данный запрос вызывался из контроллера спринг бута.

```java
ru/efko/testtask/controller/ConsultantController.java
======================================================================

@RestController
@RequestMapping("/consultant")
public class ConsultantController {
    ConsultantRepository consultantRepository;

    @GetMapping
    public List<ConsultantAggregateResponseDto> getAllConsultants(){
        return consultantRepository.getAll();
    }
}

```
## Задача 2
### Условие:

> Сделайте рефакторинг
   
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

### Решение:

Вся логика из сервиса ушла в запрос.

```java
ru/efko/testtask/service/impl/EventServiceImpl.java
======================================================================

public List<Event> getAllByCalendarIdAndBetweenDates(Long calendarId, Date dtStart, Date dtEnd) {
    return eventRepository.getAllByCalendarIdAndBetweenDates(calendarId, dtStart, dtEnd);
}

======================================================================
ru/efko/testtask/reposiroty/impl/EventRepositoryImpl.java
======================================================================

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

Создание таблицы:

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
