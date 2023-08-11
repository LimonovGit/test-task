package ru.efko.testtask.reposiroty;

import org.springframework.stereotype.Repository;
import ru.efko.testtask.dto.ConsultantAggregateResponseDto;
import ru.efko.testtask.entity.Consultant;

import java.util.List;

@Repository
public interface ConsultantRepository {
    int[] batchSave(List<Consultant> consultants);
    List<ConsultantAggregateResponseDto> getAll();
    void batchSave(String sql);
    void cleanData();
}
