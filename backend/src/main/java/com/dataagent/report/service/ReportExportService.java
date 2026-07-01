package com.dataagent.report.service;

import com.dataagent.report.entity.Report;
import com.dataagent.report.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final ReportMapper reportMapper;

    public byte[] exportPdf(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) return new byte[0];

        String html = buildHtml(report);
        // Simplified: return HTML wrapped as PDF placeholder
        // In production, use wkhtmltopdf or iText
        return html.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportExcel(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) return new byte[0];

        // Simplified CSV export
        StringBuilder csv = new StringBuilder();
        csv.append("报告ID,标题,版本,创建时间\n");
        csv.append(report.getId()).append(",")
           .append(report.getTitle()).append(",")
           .append(report.getVersion()).append(",")
           .append(report.getCreatedAt()).append("\n");

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String buildHtml(Report report) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'>" +
               "<title>" + escapeHtml(report.getTitle()) + "</title>" +
               "<style>body{font-family:sans-serif;padding:40px;line-height:1.7}" +
               "h1{color:#303133}</style></head><body>" +
               "<h1>" + escapeHtml(report.getTitle()) + "</h1>" +
               "<div>" + (report.getContentHtml() != null ? report.getContentHtml() :
                          toHtml(report.getContentMd())) + "</div>" +
               "</body></html>";
    }

    private String toHtml(String md) {
        if (md == null) return "";
        // Basic Markdown to HTML conversion (for export)
        return md.replace("## ", "<h2>")
                 .replace("\n", "<br>");
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
