package com.dataagent.report.controller;

import com.dataagent.report.service.ReportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/{id}/export")
@RequiredArgsConstructor
@Tag(name = "报告导出")
public class ReportExportController {

    private final ReportExportService reportExportService;

    @PostMapping("/pdf")
    @Operation(summary = "导出 PDF")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        byte[] data = reportExportService.exportPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_" + id + ".html")
                .contentType(MediaType.TEXT_HTML)
                .body(data);
    }

    @PostMapping("/excel")
    @Operation(summary = "导出 Excel (CSV)")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long id) {
        byte[] data = reportExportService.exportExcel(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_" + id + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
