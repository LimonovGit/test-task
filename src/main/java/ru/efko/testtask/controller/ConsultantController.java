package ru.efko.testtask.controller;


import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.efko.testtask.dto.ConsultantAggregateResponseDto;
import ru.efko.testtask.reposiroty.ConsultantRepository;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;


@RestController
@RequestMapping("/consultant")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ConsultantController {
    ConsultantRepository consultantRepository;


    /**
     * С базы необходимо выгрузить данные в JSON формате по консультантам.
     * Формат данных должен соответствовать стандарту REST в выгрузке должны быть
     * список консультантов со списком служб и суммой по количеству задач
     * @return список консультантов со списком служб и суммой по количеству задач
     */
    @GetMapping
    public List<ConsultantAggregateResponseDto> getAllConsultants(){
        return consultantRepository.getAll();
    }
}
