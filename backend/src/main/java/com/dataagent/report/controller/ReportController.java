package com.dataagent.report.controller;

import com.dataagent.common.model.PageResult;
import com.dataagent.common.model.R;
import com.dataagent.report.entity.Report;
import com.dataagent.report.service.ReportService;
import com.dataagent.report.vo.ReportVO;
import com.dataagent.report.vo.ReportVersionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "报告中心")
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @Operation(summary = "报告分页列表")
    @PreAuthorize("hasAuthority('report:list')")
    public R<PageResult<ReportVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String title) {
        return R.ok(reportService.page(page, pageSize, title));
    }

    @GetMapping("/{id}")
    @Operation(summary = "报告详情")
    @PreAuthorize("hasAuthority('report:detail')")
    public R<ReportVO> getById(@PathVariable Long id) {
        return R.ok(reportService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增报告")
    @PreAuthorize("hasAuthority('report:list')")
    public R<Void> create(@RequestBody Report report) {
        reportService.create(report);
        return R.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新报告")
    @PreAuthorize("hasAuthority('report:edit')")
    public R<Void> update(@PathVariable Long id, @RequestBody Report report) {
        reportService.update(id, report);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除报告")
    @PreAuthorize("hasAuthority('report:delete')")
    public R<Void> delete(@PathVariable Long id) {
        reportService.delete(id);
        return R.ok();
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "版本列表")
    @PreAuthorize("hasAuthority('report:version')")
    public R<List<ReportVersionVO>> versions(@PathVariable Long id) {
        return R.ok(reportService.getVersions(id));
    }

    @PostMapping("/{id}/versions")
    @Operation(summary = "创建版本")
    @PreAuthorize("hasAuthority('report:version')")
    public R<Void> createVersion(@PathVariable Long id, @RequestBody Map<String, String> body) {
        reportService.createVersion(id, body.getOrDefault("changeNote", "新版本"));
        return R.ok();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "恢复版本")
    @PreAuthorize("hasAuthority('report:version')")
    public R<Void> restoreVersion(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        reportService.restoreVersion(id, body.get("versionId"));
        return R.ok();
    }

    @PostMapping("/{id}/share")
    @Operation(summary = "生成分享链接")
    @PreAuthorize("hasAuthority('report:share')")
    public R<Map<String, String>> share(@PathVariable Long id) {
        String url = reportService.share(id);
        return R.ok(Map.of("url", url));
    }
}
