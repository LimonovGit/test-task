package ru.efko.testtask.reposiroty.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.efko.testtask.dto.ConsultantAggregateResponseDto;
import ru.efko.testtask.entity.Consultant;
import ru.efko.testtask.reposiroty.ConsultantRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultantRepositoryImpl implements ConsultantRepository {
    static final String GET_ALL_QUERY =
            "SELECT c.full_name as fullName, " +
            "sum(c.num_of_tasks) as sumOfTasks, " +
            "ARRAY_AGG(c.service) as services " +
            "FROM consultant c GROUP BY 1";

    static final String REMOVE_ALL_QUERY = "delete from consultant";

    JdbcTemplate jdbcTemplate;

    @Override
    public int[] batchSave(List<Consultant> consultants) {
        return jdbcTemplate.batchUpdate("INSERT INTO CONSULTANT VALUES (?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, consultants.get(i).getDivision());
                        ps.setString(2, consultants.get(i).getDirecting());
                        ps.setString(3, consultants.get(i).getService());
                        ps.setString(4, consultants.get(i).getSubdivision());
                        ps.setString(5, consultants.get(i).getFullName());
                        ps.setInt(6, consultants.get(i).getNumOfTasks());
                    }

                    @Override
                    public int getBatchSize() {
                        return consultants.size();
                    }
                });
    }

    @Override
    public List<ConsultantAggregateResponseDto> getAll() {
        return jdbcTemplate.query(GET_ALL_QUERY, (rs, rowNum) -> ConsultantAggregateResponseDto.builder()
                .fullName(rs.getString(1))
                .taskSumTime(rs.getInt(2))
                .services(Arrays.stream(((String[])rs.getArray(3).getArray())).collect(Collectors.toList()))
                .build());
    }

    @Override
    public void batchSave(String sql) {
        jdbcTemplate.batchUpdate(sql);
    }

    @Override
    public void cleanData() {
        jdbcTemplate.update(REMOVE_ALL_QUERY);
    }
}
