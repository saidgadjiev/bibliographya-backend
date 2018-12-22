package ru.saidgadjiev.bibliography.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.bussiness.fix.ClosedHandler;
import ru.saidgadjiev.bibliography.bussiness.fix.FixAction;
import ru.saidgadjiev.bibliography.bussiness.fix.Handler;
import ru.saidgadjiev.bibliography.bussiness.fix.PendingHandler;
import ru.saidgadjiev.bibliography.dao.BiographyFixDao;
import ru.saidgadjiev.bibliography.data.FilterArgumentResolver;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.PreparedSetter;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyFix;
import ru.saidgadjiev.bibliography.domain.CompleteResult;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.BiographyFixSuggestRequest;
import ru.saidgadjiev.bibliography.model.CompleteRequest;
import ru.saidgadjiev.bibliography.model.ModerationStatus;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.security.service.SecurityService;
import ru.saidgadjiev.bibliography.service.impl.BiographyCategoryBiographyService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by said on 15.12.2018.
 */
@Service
public class BiographyFixService {

    private final BiographyFixDao biographyFixDao;

    private final SecurityService securityService;

    private final BiographyCategoryBiographyService biographyCategoryBiographyService;

    private final FilterArgumentResolver argumentResolver;

    private Map<BiographyFix.FixStatus, Handler> handlerMap = new HashMap<>();

    @Autowired
    public BiographyFixService(BiographyFixDao biographyFixDao,
                               SecurityService securityService,
                               BiographyCategoryBiographyService biographyCategoryBiographyService,
                               FilterArgumentResolver argumentResolver) {
        this.biographyFixDao = biographyFixDao;
        this.securityService = securityService;
        this.biographyCategoryBiographyService = biographyCategoryBiographyService;
        this.argumentResolver = argumentResolver;

        initHandlers();
    }

    public Page<BiographyFix> getFixesList(OffsetLimitPageRequest pageRequest,
                                           String fixerNameFilter,
                                           String statusFilter) {
        List<FilterCriteria> criteria = new ArrayList<>();

        if (fixerNameFilter != null) {
            criteria.add(
                    argumentResolver.resolve(
                            "fixer_name",
                            String::valueOf,
                            PreparedStatement::setString,
                            fixerNameFilter
                    )
            );
        }
        if (statusFilter != null) {
            criteria.add(
                    argumentResolver.resolve(
                            "status",
                            object -> Integer.valueOf(String.valueOf(object)),
                            PreparedStatement::setInt,
                            statusFilter
                    )
            );
        }
        List<BiographyFix> biographyFixes = biographyFixDao.getFixesList(
                pageRequest.getPageSize(),
                pageRequest.getOffset(),
                criteria
        );

        if (biographyFixes.isEmpty()) {
            return new PageImpl<>(biographyFixes, pageRequest, 0);
        }
        long total = biographyFixDao.countOff();
        Collection<Integer> ids = biographyFixes.stream().map(BiographyFix::getBiographyId).collect(Collectors.toList());

        Map<Integer, Collection<String>> categories = biographyCategoryBiographyService.getBiographiesCategories(ids);

        for (BiographyFix fix: biographyFixes) {
            fix.getBiography().setCategories(categories.get(fix.getBiographyId()));
        }

        return new PageImpl<>(biographyFixes, pageRequest, total);
    }

    public void suggest(int biographyId, BiographyFixSuggestRequest suggestRequest) {
        UserDetails details = securityService.findLoggedInUser();
        BiographyFix biographyFix = new BiographyFix();

        biographyFix.setFixText(suggestRequest.getFixText());
        biographyFix.setCreatorName(details.getUsername());
        biographyFix.setBiographyId(biographyId);

        biographyFixDao.create(biographyFix);
    }

    public CompleteResult<BiographyFix, FixAction> complete(int fixId, CompleteRequest completeRequest) throws SQLException {
        BiographyFix fix = doComplete(fixId, completeRequest);

        return new CompleteResult<>(fix == null ? 0 : 1, fix, getActions(fix));
    }

    public Collection<FixAction> getActions(BiographyFix fix) {
        return handlerMap.get(fix.getStatus()).getActions(
                new HashMap<String, Object>() {{
                    put("fixerName", fix.getFixerName());
                    put("status", fix.getStatus());
                }}
        );
    }

    public BiographyFix getFixerInfo(int fixId) {
        return biographyFixDao.getFixerInfo(fixId);
    }

    private void initHandlers() {
        handlerMap.put(BiographyFix.FixStatus.CLOSED, new ClosedHandler());
        handlerMap.put(BiographyFix.FixStatus.PENDING, new PendingHandler(biographyFixDao));
    }

    private BiographyFix doComplete(int fixId, CompleteRequest completeRequest) throws SQLException {
        User userDetails = (User) securityService.findLoggedInUser();

        Map<String, Object> processValues = new HashMap<>();

        processValues.put("fixId", fixId);
        processValues.put("fixerName", userDetails.getUsername());

        Handler handler = handlerMap.get(
                BiographyFix.FixStatus.fromCode(completeRequest.getStatus())
        );

        BiographyFix updated = handler.handle(Handler.Signal.fromDesc(completeRequest.getSignal()), processValues);

        if (updated == null) {
            return null;
        }

        if (StringUtils.isNotBlank(updated.getFixerName())) {
            updated.setFixerBiography(userDetails.getBiography());
        }

        return updated;
    }
}
