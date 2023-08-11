package ru.efko.testtask.service;

import org.apache.poi.ss.usermodel.Workbook;

public interface ParserService {
    void convertToSqlQueryAndSave(Workbook workbook);
}
