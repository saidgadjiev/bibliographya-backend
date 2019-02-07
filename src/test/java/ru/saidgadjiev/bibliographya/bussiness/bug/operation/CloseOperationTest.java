package ru.saidgadjiev.bibliographya.bussiness.bug.operation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.bussiness.common.BusinessOperation;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CloseOperationTest extends BaseBugOperationTest {

    @Override
    protected BusinessOperation<Bug> bugBusinessOperation() {
        return new CloseOperation(bugDao);
    }

    @Override
    protected Bug expected() {
        Bug expected = new Bug();

        expected.setId(1);
        expected.setFixerId(1);
        expected.setStatus(Bug.BugStatus.CLOSED);

        return expected;
    }

    @Override
    protected Map<String, Object> args() {
        Map<String, Object> args = new HashMap<>();

        args.put("bugId", 1);
        args.put("fixerId", 1);

        return args;
    }

    @Override
    protected void preExecute() {
        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id, status) VALUES('Тест', 'Тест', 1, 0)"
        );
    }
}