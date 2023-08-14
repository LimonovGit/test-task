package ru.efko.testtask.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.efko.testtask.reposiroty.ConsultantRepository;
import ru.efko.testtask.service.ParserService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ParserServiceImpl implements ParserService {
    @NonFinal
    @Value("classpath:data.xlsx")
    Resource resourceFile;
    ConsultantRepository consultantRepository;
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


    /**
     * Необходимо распарсить предоставленный файл структуры с сохранением
     * иерархии подразделений и записи ответственных к подразделению.
     * Результат парсинга должен собрать SQL запрос и записать результат в базу результат
     * @param workbook - файл структуры
     */
    @Override
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

    @PostConstruct
    public void initData() throws IOException {
        Workbook workbook = new XSSFWorkbook(resourceFile.getInputStream());
        convertToSqlQueryAndSave(workbook);
    }

    @PreDestroy
    public void cleanData(){
        consultantRepository.cleanData();
    }
}
