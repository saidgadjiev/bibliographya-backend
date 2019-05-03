package ru.saidgadjiev.bibliographya.bussiness.bug.operation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.dao.dialect.PostgresDialect;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;


@ExtendWith(SpringExtension.class)
@SpringBootTest
class AssignMeOperationTest {

    @MockBean
    private BugDao bugDao;

    @Test
    void execute() {
        Bug current = new Bug();

        current.setStatus(Bug.BugStatus.PENDING);
        current.setId(1);
        current.setCreatedAt(new Timestamp(new Date().getTime()));

        Mockito.when(bugDao.update(any(), any(), any())).thenAnswer(invocationOnMock -> {
            current.setFixerId(1);

            return current;
        });
        Mockito.when(bugDao.getDialect()).thenReturn(new PostgresDialect());
        AssignMeOperation assignMeOperation = new AssignMeOperation(bugDao);

        Bug result = assignMeOperation.execute(new HashMap<String, Object>() {{
            put("fixerId", 1);
            put("bugId", 1);
        }});

        Assertions.assertEquals(1, (int) current.getFixerId());
        Assertions.assertEquals(result.getFixerId(), current.getFixerId());
    }
}