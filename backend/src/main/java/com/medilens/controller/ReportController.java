package com.medilens.controller;

import com.medilens.dto.report.MeasurementResponseDto;
import com.medilens.dto.report.ReportDetailResponseDto;
import com.medilens.dto.report.ReportExplanationResponseDto;
import com.medilens.dto.report.ReportResponseDto;
import com.medilens.exception.ResourceNotFoundException;
import com.medilens.exception.UnauthorizedException;
import com.medilens.model.User;
import com.medilens.repository.UserRepository;
import com.medilens.security.UserPrincipal;
import com.medilens.service.ReportProcessingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportProcessingService reportProcessingService;
    private final UserRepository userRepository;
    private final com.medilens.service.PdfExportService pdfExportService;

    public ReportController(ReportProcessingService reportProcessingService,
                            UserRepository userRepository,
                            com.medilens.service.PdfExportService pdfExportService) {
        this.reportProcessingService = reportProcessingService;
        this.userRepository = userRepository;
        this.pdfExportService = pdfExportService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponseDto> uploadReport(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to upload reports");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        ReportResponseDto response = reportProcessingService.uploadReport(user, file, ipAddress, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ReportResponseDto>> getUserReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }

        Page<ReportResponseDto> reports = reportProcessingService.getUserReports(
                principal.getId(), PageRequest.of(page, size)
        );
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportDetailResponseDto> getReportDetail(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }

        ReportDetailResponseDto detail = reportProcessingService.getReportDetail(id, principal.getId());
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{id}/measurements")
    public ResponseEntity<List<MeasurementResponseDto>> getReportMeasurements(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }

        List<MeasurementResponseDto> measurements = reportProcessingService.getReportMeasurements(id, principal.getId());
        return ResponseEntity.ok(measurements);
    }

    @GetMapping("/{id}/pages/{pageNumber}/image")
    public ResponseEntity<Resource> getPageImage(
            @PathVariable UUID id,
            @PathVariable int pageNumber,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }

        Resource imageResource = reportProcessingService.getPageImageResource(id, pageNumber, principal.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageResource);
    }

    @GetMapping("/{id}/explanation")
    public ResponseEntity<ReportExplanationResponseDto> getReportExplanation(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        ReportExplanationResponseDto explanation = reportProcessingService.getReportExplanation(id, user);
        return ResponseEntity.ok(explanation);
    }

    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportReportPdf(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Authentication required to export report");
        }

        byte[] pdfBytes = pdfExportService.generateReportPdf(principal.getId(), id);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"medilens-report-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
