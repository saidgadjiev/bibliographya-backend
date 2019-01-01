package ru.saidgadjiev.bibliography.controller;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyReportRequest;
import ru.saidgadjiev.bibliography.model.BiographyResponse;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.service.impl.BiographyReportService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 31.12.2018.
 */
public class BiographyReportController {

    private final ModelMapper modelMapper;

    private final BiographyReportService reportService;

    public BiographyReportController(ModelMapper modelMapper, BiographyReportService reportService) {
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
                        convertToDto(reportPage.getContent()),
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

    private List<BiographyResponse> convertToDto(List<Biography> biographies) {
        List<BiographyResponse> dto = new ArrayList<>();

        for (Biography biography : biographies) {
            BiographyResponse biographyResponse = modelMapper.map(biography, BiographyResponse.class);

            dto.add(biographyResponse);
        }

        return dto;
    }
}
