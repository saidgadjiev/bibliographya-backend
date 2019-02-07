package ru.saidgadjiev.bibliographya.bussiness.bug.operation;

import ru.saidgadjiev.bibliographya.bussiness.common.BusinessOperation;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.util.HashMap;
import java.util.Map;


class AssignMeOperationTest extends BaseBugOperationTest {

    @Override
    protected void preExecute() {
        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case) VALUES('Тест', 'Тест')"
        );
    }

    @Override
    protected Bug expected() {
        Bug expected = new Bug();

        expected.setId(1);
        expected.setStatus(Bug.BugStatus.PENDING);
        expected.setFixerId(1);

        return expected;
    }

    @Override
    protected BusinessOperation<Bug> bugBusinessOperation() {
        return new AssignMeOperation(bugDao);
    }

    @Override
    protected Map<String, Object> args() {
        Map<String, Object> args = new HashMap<>();

        args.put("fixerId", 1);
        args.put("bugId", 1);

        return args;
    }
}