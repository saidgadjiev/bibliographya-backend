package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.saidgadjiev.bibliographya.bussiness.bug.BugAction;
import ru.saidgadjiev.bibliographya.bussiness.bug.Handler;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.BugRequest;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.service.impl.BugService;
import ru.saidgadjiev.bibliographya.service.impl.auth.AuthService;

import javax.servlet.http.Cookie;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class BugTrackingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BugService bugService;

    @MockBean
    private AuthService authService;

    @Test
    void create() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        BugRequest bugRequest = new BugRequest();

        bugRequest.setBugCase("Тест");
        bugRequest.setTheme("Тест");
        Bug bug = new Bug();

        bug.setId(1);
        bug.setTheme("Тест");
        bug.setBugCase("Тест");
        bug.setCreatedAt(new Timestamp(new Date().getTime()));
        bug.setStatus(Bug.BugStatus.PENDING);
/*
        Mockito.doAnswer(invocationOnMock -> {
            authenticate();

            return null;
        }).when(authService).tokenAuth("TestToken");*/

        Mockito.when(bugService.create(any(), any())).thenReturn(bug);
        Mockito.when(bugService.getActions(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/bugs/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(bugRequest))
                .cookie(new Cookie("X-TOKEN", "TestToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.theme", Matchers.is("Тест")))
                .andExpect(jsonPath("$.bugCase", Matchers.is("Тест")))
                .andExpect(jsonPath("$.actions", Matchers.nullValue()))
                .andExpect(jsonPath("$.createdAt", Matchers.notNullValue()))
                .andExpect(jsonPath("$.status", Matchers.is(Bug.BugStatus.PENDING.getCode())));

        logout();
    }

    @Test
    void getBugs() throws Exception {
        Bug bug = new Bug();

        bug.setId(1);
        bug.setTheme("Test");
        bug.setBugCase("Test");
        bug.setCreatedAt(new Timestamp(new Date().getTime()));
        bug.setStatus(Bug.BugStatus.PENDING);

        Bug bug1 = new Bug();

        bug1.setId(2);
        bug1.setTheme("Test2");
        bug1.setBugCase("Test2");
        bug1.setCreatedAt(new Timestamp(new Date().getTime()));
        bug1.setStatus(Bug.BugStatus.CLOSED);

        List<Bug> bugs = new ArrayList<>();

        bugs.add(bug);
        bugs.add(bug1);
/*
        Mockito.doAnswer(invocationOnMock -> {
            authenticate();

            return null;
        }).when(authService).tokenAuth("TestToken");*/

        OffsetLimitPageRequest pageRequest = new OffsetLimitPageRequest.Builder()
                .setLimit(10)
                .setOffset(0)
                .setSort(Sort.unsorted())
                .build();

        Page<Bug> page = new PageImpl<>(bugs, pageRequest, 2);

        Mockito.when(bugService.getBugs(any(), eq(pageRequest), isNull())).thenReturn(page);
        Mockito.when(bugService.getActions(any()))
                .thenReturn(Arrays.asList(BugAction.assignMe(), BugAction.close(), BugAction.ignore()))
                .thenReturn(Arrays.asList(BugAction.assignMe(), BugAction.close(), BugAction.ignore()));

        mockMvc.perform(get("/api/bugs?limit=10&offset=0")
                .cookie(new Cookie("X-TOKEN", "TestToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", Matchers.is(1)))
                .andExpect(jsonPath("$.content[0].theme", Matchers.is("Test")))
                .andExpect(jsonPath("$.content[0].bugCase", Matchers.is("Test")))
                .andExpect(jsonPath("$.content[0].actions", hasSize(0)))
                .andExpect(jsonPath("$.content[0].createdAt", Matchers.notNullValue()))
                .andExpect(jsonPath("$.content[0].status", Matchers.is(Bug.BugStatus.PENDING.getCode())))
                .andExpect(jsonPath("$.content[0].fixerId", nullValue()))
                .andExpect(jsonPath("$.content[0].fixer", nullValue()))
                .andExpect(jsonPath("$.content[1].id", Matchers.is(2)))
                .andExpect(jsonPath("$.content[1].theme", Matchers.is("Test2")))
                .andExpect(jsonPath("$.content[1].bugCase", Matchers.is("Test2")))
                .andExpect(jsonPath("$.content[1].actions", hasSize(0)))
                .andExpect(jsonPath("$.content[1].createdAt", Matchers.notNullValue()))
                .andExpect(jsonPath("$.content[1].status", Matchers.is(Bug.BugStatus.CLOSED.getCode())))
                .andExpect(jsonPath("$.content[1].fixerId", nullValue()))
                .andExpect(jsonPath("$.content[1].fixer", nullValue()));

        logout();
    }

    @Test
    void getBugsTracks() throws Exception {
        Bug bug = new Bug();

        bug.setId(1);
        bug.setTheme("Test");
        bug.setBugCase("Test");
        bug.setCreatedAt(new Timestamp(new Date().getTime()));
        bug.setStatus(Bug.BugStatus.PENDING);
        bug.setFixerId(1);

        Biography fixer = new Biography();

        fixer.setId(1);
        fixer.setFirstName("Test");
        fixer.setLastName("Test");

        bug.setFixer(fixer);

        Bug bug1 = new Bug();

        bug1.setId(2);
        bug1.setTheme("Test2");
        bug1.setBugCase("Test2");
        bug1.setCreatedAt(new Timestamp(new Date().getTime()));
        bug1.setStatus(Bug.BugStatus.IGNORED);
        bug1.setFixerId(1);

        bug1.setFixer(fixer);

        List<Bug> bugs = new ArrayList<>();

        bugs.add(bug);
        bugs.add(bug1);
/*
        Mockito.doAnswer(invocationOnMock -> {
            authenticate();

            return null;
        }).when(authService).tokenAuth("TestToken");*/

        OffsetLimitPageRequest pageRequest = new OffsetLimitPageRequest.Builder()
                .setLimit(10)
                .setOffset(0)
                .setSort(Sort.unsorted())
                .build();

        Page<Bug> page = new PageImpl<>(bugs, pageRequest, 2);

        Mockito.when(bugService.getBugsTracks(any(), eq(pageRequest), isNull())).thenReturn(page);
        Mockito.when(bugService.getActions(any()))
                .thenReturn(Arrays.asList(BugAction.ignore(), BugAction.close(), BugAction.release()))
                .thenReturn(Arrays.asList(BugAction.pending(), BugAction.release()));

        mockMvc.perform(get("/api/bugs/tracking?limit=10&offset=0")
                .cookie(new Cookie("X-TOKEN", "TestToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", Matchers.is(1)))
                .andExpect(jsonPath("$.content[0].theme", Matchers.is("Test")))
                .andExpect(jsonPath("$.content[0].bugCase", Matchers.is("Test")))
                .andExpect(jsonPath("$.content[0].createdAt", Matchers.notNullValue()))
                .andExpect(jsonPath("$.content[0].status", Matchers.is(Bug.BugStatus.PENDING.getCode())))
                .andExpect(jsonPath("$.content[0].fixerId", is(1)))
                .andExpect(jsonPath("$.content[0].fixer.id", is(1)))
                .andExpect(jsonPath("$.content[0].fixer.firstName", is("Test")))
                .andExpect(jsonPath("$.content[0].fixer.lastName", is("Test")))
                .andExpect(jsonPath("$.content[0].actions", hasSize(3)))
                .andExpect(jsonPath("$.content[0].actions[0].name", is("Ignore")))
                .andExpect(jsonPath("$.content[0].actions[0].caption", is("Закрыть без исправления")))
                .andExpect(jsonPath("$.content[0].actions[0].signal", is("ignore")))
                .andExpect(jsonPath("$.content[0].actions[1].name", is("Close")))
                .andExpect(jsonPath("$.content[0].actions[1].caption", is("Закрыть")))
                .andExpect(jsonPath("$.content[0].actions[1].signal", is("close")))
                .andExpect(jsonPath("$.content[0].actions[2].name", is("Release")))
                .andExpect(jsonPath("$.content[0].actions[2].caption", is("Вернуть")))
                .andExpect(jsonPath("$.content[0].actions[2].signal", is("release")))
                .andExpect(jsonPath("$.content[1].id", Matchers.is(2)))
                .andExpect(jsonPath("$.content[1].theme", Matchers.is("Test2")))
                .andExpect(jsonPath("$.content[1].bugCase", Matchers.is("Test2")))
                .andExpect(jsonPath("$.content[1].createdAt", Matchers.notNullValue()))
                .andExpect(jsonPath("$.content[1].status", Matchers.is(Bug.BugStatus.IGNORED.getCode())))
                .andExpect(jsonPath("$.content[1].fixerId", is(1)))
                .andExpect(jsonPath("$.content[1].fixer.id", is(1)))
                .andExpect(jsonPath("$.content[1].fixer.firstName", is("Test")))
                .andExpect(jsonPath("$.content[1].fixer.lastName", is("Test")))
                .andExpect(jsonPath("$.content[1].actions", hasSize(2)))
                .andExpect(jsonPath("$.content[1].actions[0].name", is("Pending")))
                .andExpect(jsonPath("$.content[1].actions[0].caption", is("Открыть")))
                .andExpect(jsonPath("$.content[1].actions[0].signal", is("pending")))
                .andExpect(jsonPath("$.content[1].actions[1].name", is("Release")))
                .andExpect(jsonPath("$.content[1].actions[1].caption", is("Вернуть")))
                .andExpect(jsonPath("$.content[1].actions[1].signal", is("release")));

        logout();
    }

    @Test
    void getNotAssignedBugsTracks() throws Exception {
        Bug bug = new Bug();

        bug.setId(1);
        bug.setTheme("Test");
        bug.setBugCase("Test");
        bug.setCreatedAt(new Timestamp(new Date().getTime()));
        bug.setStatus(Bug.BugStatus.PENDING);

        Bug bug1 = new Bug();

        bug1.setId(2);
        bug1.setTheme("Test2");
        bug1.setBugCase("Test2");
        bug1.setCreatedAt(new Timestamp(new Date().getTime()));
        bug1.setStatus(Bug.BugStatus.IGNORED);

        List<Bug> bugs = new ArrayList<>();

        bugs.add(bug);
        bugs.add(bug1);
/*
        Mockito.doAnswer(invocationOnMock -> {
            authenticate();

            return null;
        }).when(authService).tokenAuth("TestToken");*/

        OffsetLimitPageRequest pageRequest = new OffsetLimitPageRequest.Builder()
                .setLimit(10)
                .setOffset(0)
                .setSort(Sort.unsorted())
                .build();

        Page<Bug> page = new PageImpl<>(bugs, pageRequest, 2);

        Mockito.when(bugService.getBugsTracks(any(), eq(pageRequest), isNull())).thenReturn(page);
        Mockito.when(bugService.getActions(any()))
                .thenReturn(Arrays.asList(BugAction.ignore(), BugAction.close(), BugAction.release()))
                .thenReturn(Arrays.asList(BugAction.pending(), BugAction.release()));

        mockMvc.perform(get("/api/bugs/tracking?limit=10&offset=0")
                .cookie(new Cookie("X-TOKEN", "TestToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", Matchers.is(1)))
                .andExpect(jsonPath("$.content[0].theme", Matchers.is("Test")))
                .andExpect(jsonPath("$.content[0].bugCase", Matchers.is("Test")))
                .andExpect(jsonPath("$.content[0].createdAt", Matchers.notNullValue()))
                .andExpect(jsonPath("$.content[0].status", Matchers.is(Bug.BugStatus.PENDING.getCode())))
                .andExpect(jsonPath("$.content[0].fixerId", nullValue()))
                .andExpect(jsonPath("$.content[0].fixer", nullValue()))
                .andExpect(jsonPath("$.content[0].actions", hasSize(3)))
                .andExpect(jsonPath("$.content[0].actions[0].name", is("Ignore")))
                .andExpect(jsonPath("$.content[0].actions[0].caption", is("Закрыть без исправления")))
                .andExpect(jsonPath("$.content[0].actions[0].signal", is("ignore")))
                .andExpect(jsonPath("$.content[0].actions[1].name", is("Close")))
                .andExpect(jsonPath("$.content[0].actions[1].caption", is("Закрыть")))
                .andExpect(jsonPath("$.content[0].actions[1].signal", is("close")))
                .andExpect(jsonPath("$.content[0].actions[2].name", is("Release")))
                .andExpect(jsonPath("$.content[0].actions[2].caption", is("Вернуть")))
                .andExpect(jsonPath("$.content[0].actions[2].signal", is("release")))
                .andExpect(jsonPath("$.content[1].id", Matchers.is(2)))
                .andExpect(jsonPath("$.content[1].theme", Matchers.is("Test2")))
                .andExpect(jsonPath("$.content[1].bugCase", Matchers.is("Test2")))
                .andExpect(jsonPath("$.content[1].createdAt", Matchers.notNullValue()))
                .andExpect(jsonPath("$.content[1].status", Matchers.is(Bug.BugStatus.IGNORED.getCode())))
                .andExpect(jsonPath("$.content[0].fixerId", nullValue()))
                .andExpect(jsonPath("$.content[0].fixer", nullValue()))
                .andExpect(jsonPath("$.content[1].actions", hasSize(2)))
                .andExpect(jsonPath("$.content[1].actions[0].name", is("Pending")))
                .andExpect(jsonPath("$.content[1].actions[0].caption", is("Открыть")))
                .andExpect(jsonPath("$.content[1].actions[0].signal", is("pending")))
                .andExpect(jsonPath("$.content[1].actions[1].name", is("Release")))
                .andExpect(jsonPath("$.content[1].actions[1].caption", is("Вернуть")))
                .andExpect(jsonPath("$.content[1].actions[1].signal", is("release")));

        logout();
    }

    @Test
    void assignMe() throws Exception {
        CompleteRequest completeRequest = new CompleteRequest();

        completeRequest.setSignal(Handler.Signal.ASSIGN_ME.getDesc());
        completeRequest.setStatus(Bug.BugStatus.PENDING.getCode());

        Bug bug = new Bug();

        bug.setId(1);
        bug.setStatus(Bug.BugStatus.PENDING);

        Bug fixerInfo = new Bug();

        fixerInfo.setId(1);
        fixerInfo.setFixerId(1);
        fixerInfo.setStatus(Bug.BugStatus.PENDING);

        Biography fixer = new Biography();

        fixer.setId(1);
        fixer.setFirstName("Test");
        fixer.setLastName("Test");

        fixerInfo.setFixer(fixer);

        CompleteResult<Bug> completeResult = new CompleteResult<>(1, bug);

        Mockito.when(bugService.complete(any(), eq(1), eq(completeRequest))).thenReturn(completeResult);
        Mockito.when(bugService.getFixerInfo(eq(1))).thenReturn(fixerInfo);
/*
        Mockito.doAnswer(invocationOnMock -> {
            authenticate();

            return null;
        }).when(authService).tokenAuth("TestToken");*/

        Mockito.when(bugService.getActions(any())).thenReturn(Arrays.asList(BugAction.ignore(), BugAction.close(), BugAction.release()));

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(patch("/api/bugs/1/assign-me")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(completeRequest))
                .cookie(new Cookie("X-TOKEN", "TestToken"))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is(Bug.BugStatus.PENDING.getCode())))
                .andExpect(jsonPath("$.fixerId", is(1)))
                .andExpect(jsonPath("$.fixer.id", is(1)))
                .andExpect(jsonPath("$.fixer.firstName", is("Test")))
                .andExpect(jsonPath("$.fixer.lastName", is("Test")))
                .andExpect(jsonPath("$.actions", hasSize(3)))
                .andExpect(jsonPath("$.actions[0].name", is("Ignore")))
                .andExpect(jsonPath("$.actions[0].caption", is("Закрыть без исправления")))
                .andExpect(jsonPath("$.actions[0].signal", is("ignore")))
                .andExpect(jsonPath("$.actions[1].name", is("Close")))
                .andExpect(jsonPath("$.actions[1].caption", is("Закрыть")))
                .andExpect(jsonPath("$.actions[1].signal", is("close")))
                .andExpect(jsonPath("$.actions[2].name", is("Release")))
                .andExpect(jsonPath("$.actions[2].caption", is("Вернуть")))
                .andExpect(jsonPath("$.actions[2].signal", is("release")));
    }

    @Test
    void assignMeConflict() throws Exception {
        CompleteRequest completeRequest = new CompleteRequest();

        completeRequest.setSignal(Handler.Signal.ASSIGN_ME.getDesc());
        completeRequest.setStatus(Bug.BugStatus.PENDING.getCode());

        Bug bug = new Bug();

        bug.setId(1);
        bug.setStatus(Bug.BugStatus.PENDING);
        bug.setFixerId(1);

        Bug fixerInfo = new Bug();

        fixerInfo.setId(1);
        fixerInfo.setFixerId(1);
        fixerInfo.setStatus(Bug.BugStatus.PENDING);

        Biography fixer = new Biography();

        fixer.setId(1);
        fixer.setFirstName("Test");
        fixer.setLastName("Test");

        fixerInfo.setFixer(fixer);

        CompleteResult<Bug> completeResult = new CompleteResult<>(0, bug);

        Mockito.when(bugService.complete(any(), eq(1), eq(completeRequest))).thenReturn(completeResult);
        Mockito.when(bugService.getFixerInfo(eq(1))).thenReturn(fixerInfo);
/*
        Mockito.doAnswer(invocationOnMock -> {
            authenticate();

            return null;
        }).when(authService).tokenAuth("TestToken");*/

        Mockito.when(bugService.getActions(any())).thenReturn(Arrays.asList(BugAction.ignore(), BugAction.close(), BugAction.release()));

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(patch("/api/bugs/1/assign-me")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(completeRequest))
                .cookie(new Cookie("X-TOKEN", "TestToken"))
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is(Bug.BugStatus.PENDING.getCode())))
                .andExpect(jsonPath("$.fixerId", is(1)))
                .andExpect(jsonPath("$.fixer.id", is(1)))
                .andExpect(jsonPath("$.fixer.firstName", is("Test")))
                .andExpect(jsonPath("$.fixer.lastName", is("Test")))
                .andExpect(jsonPath("$.actions", hasSize(0)));
    }

    @Test
    void complete() throws Exception {
        CompleteRequest completeRequest = new CompleteRequest();

        completeRequest.setSignal(Handler.Signal.IGNORE.getDesc());
        completeRequest.setStatus(Bug.BugStatus.PENDING.getCode());

        Bug bug = new Bug();

        bug.setId(1);
        bug.setStatus(Bug.BugStatus.PENDING);
        bug.setFixerId(1);

        CompleteResult<Bug> completeResult = new CompleteResult<>(1, bug);

        Mockito.when(bugService.complete(any(), eq(1), eq(completeRequest))).thenReturn(completeResult);
        Mockito.when(bugService.getActions(any())).thenReturn(Arrays.asList(BugAction.ignore(), BugAction.close(), BugAction.release()));
/*

        Mockito.doAnswer(invocationOnMock -> {
            authenticate();

            return null;
        }).when(authService).tokenAuth("TestToken");
*/

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(patch("/api/bugs/1/complete")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(completeRequest))
                .cookie(new Cookie("X-TOKEN", "TestToken"))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is(Bug.BugStatus.PENDING.getCode())))
                .andExpect(jsonPath("$.fixerId", is(1)))
                .andExpect(jsonPath("$.actions", hasSize(3)))
                .andExpect(jsonPath("$.actions[0].name", is("Ignore")))
                .andExpect(jsonPath("$.actions[0].caption", is("Закрыть без исправления")))
                .andExpect(jsonPath("$.actions[0].signal", is("ignore")))
                .andExpect(jsonPath("$.actions[1].name", is("Close")))
                .andExpect(jsonPath("$.actions[1].caption", is("Закрыть")))
                .andExpect(jsonPath("$.actions[1].signal", is("close")))
                .andExpect(jsonPath("$.actions[2].name", is("Release")))
                .andExpect(jsonPath("$.actions[2].caption", is("Вернуть")))
                .andExpect(jsonPath("$.actions[2].signal", is("release")));
    }

    private void authenticate() {
        User user = new User();

        user.setId(1);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, Stream
                        .of("ROLE_DEVELOPER")
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}