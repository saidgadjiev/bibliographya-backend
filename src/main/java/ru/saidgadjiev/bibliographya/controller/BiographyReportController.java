package ru.saidgadjiev.bibliographya.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyReportRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.service.impl.BiographyReportService;

/**
 * Created by said on 31.12.2018.
 */
public class BiographyReportController {

    private final BibliographyaMapper modelMapper;

    private final BiographyReportService reportService;

    public BiographyReportController(BibliographyaMapper modelMapper, BiographyReportService reportService) {
        this.modelMapper = modelMapper;
        this.reportService = reportService;
    }

    @GetMapping("")
    public ResponseEntity<?> getReports(OffsetLimitPageRequest pageRequest) {
        Page<Biography> reportPage = reportService.getReports(pageRequest);

        if (reportPage.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                new PageImpl<>(
                        null,
                        pageRequest,
                        reportPage.getTotalElements()
                )
        );
    }

    @PostMapping("/create/{biographyId}")
    public ResponseEntity<?> createReport(
            @PathVariable("biographyId") int biographyId,
            @RequestBody BiographyReportRequest reportRequest
    ) {
        reportService.create(biographyId, reportRequest);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/complete")
    public ResponseEntity<?> complete() {
        int updated = reportService.consider(0);

        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
