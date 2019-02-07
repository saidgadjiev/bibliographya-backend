package ru.saidgadjiev.bibliographya.bussiness.bug.operation;

import ru.saidgadjiev.bibliographya.bussiness.common.BusinessOperation;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.util.HashMap;
import java.util.Map;

class ReleaseOperationTest extends BaseBugOperationTest {

    @Override
    protected BusinessOperation<Bug> bugBusinessOperation() {
        return new ReleaseOperation(bugDao);
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
                "INSERT INTO bug(theme, bug_case, fixer_id, status) VALUES('Тест', 'Тест', 1, 2)"
        );
    }

    @Override
    protected Bug expected() {
        Bug expected = new Bug();

        expected.setId(1);
        expected.setFixerId(null);
        expected.setStatus(Bug.BugStatus.PENDING);

        return expected;
    }
}