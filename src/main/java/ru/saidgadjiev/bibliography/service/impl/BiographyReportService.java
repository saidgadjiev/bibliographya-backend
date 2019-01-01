package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.BiographyReportDao;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyReport;
import ru.saidgadjiev.bibliography.domain.BiographyReportRequest;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.service.impl.auth.AuthService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by said on 31.12.2018.
 */
@Service
public class BiographyReportService {

    private final BiographyReportDao reportDao;

    private final AuthService authService;

    @Autowired
    public BiographyReportService(BiographyReportDao reportDao, AuthService authService) {
        this.reportDao = reportDao;
        this.authService = authService;
    }

    public Page<Biography> getReports(OffsetLimitPageRequest pageRequest) {
        List<Biography> reports = reportDao.getReports(pageRequest.getPageSize(), pageRequest.getOffset());

        long total = reportDao.countOff(Collections.emptyList());

        Collection<Integer> biographyIds = reports.stream().map(Biography::getId).collect(Collectors.toList());

        Map<Integer, Collection<BiographyReport>> complaintsMap = reportDao.getBiographyReports(biographyIds);

        for (Biography biography : reports) {
            Collection<BiographyReport> newComplaints =  complaintsMap.get(biography.getId())
                    .stream()
                    .filter(complaint -> complaint.getStatus().equals(BiographyReport.ReportStatus.PENDING))
                    .collect(Collectors.toList());

            Collection<BiographyReport> oldComplaints =  complaintsMap.get(biography.getId())
                    .stream()
                    .filter(complaint -> complaint.getStatus().equals(BiographyReport.ReportStatus.CONSIDERED))
                    .collect(Collectors.toList());

            biography.setNewComplaints(newComplaints);
            biography.setOldComplaints(oldComplaints);
        }

        return new PageImpl<>(reports, pageRequest, total);
    }

    public int consider(int reportId) {
        return 0;
    }

    public BiographyReport create(int biographyId, BiographyReportRequest reportRequest) {
        User userDetails = (User) authService.account();
        BiographyReport report = new BiographyReport();

        report.setBiographyId(biographyId);
        report.setReason(BiographyReport.ReportReason.fromCode(reportRequest.getReason()));
        report.setReasonText(reportRequest.getReasonText());
        report.setReporterId(userDetails.getId());

        return reportDao.create(report);
    }
}
