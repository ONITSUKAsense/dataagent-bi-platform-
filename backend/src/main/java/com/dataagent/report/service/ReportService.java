package com.dataagent.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dataagent.common.exception.BusinessException;
import com.dataagent.common.model.PageResult;
import com.dataagent.common.util.SecurityUtil;
import com.dataagent.report.entity.Report;
import com.dataagent.report.entity.ReportVersion;
import com.dataagent.report.mapper.ReportMapper;
import com.dataagent.report.mapper.ReportVersionMapper;
import com.dataagent.report.vo.ReportVO;
import com.dataagent.report.vo.ReportVersionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;
    private final ReportVersionMapper reportVersionMapper;

    public PageResult<ReportVO> page(int page, int pageSize, String title) {
        IPage<Report> reportPage = reportMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Report>()
                        .like(title != null, Report::getTitle, title)
                        .orderByDesc(Report::getCreatedAt));

        return PageResult.of(reportPage.convert(this::toVO));
    }

    public ReportVO getById(Long id) {
        Report report = reportMapper.selectById(id);
        if (report == null) throw new BusinessException("报告不存在");
        return toVO(report);
    }

    @Transactional
    public void create(Report report) {
        report.setCreatedBy(SecurityUtil.getCurrentUserId());
        report.setVersion(1);
        report.setStatus(1);
        reportMapper.insert(report);

        // Create initial version
        ReportVersion version = new ReportVersion();
        version.setReportId(report.getId());
        version.setVersion(1);
        version.setContentMd(report.getContentMd());
        version.setContentHtml(report.getContentHtml());
        version.setChartConfig(report.getChartConfig());
        version.setChangeNote("初始版本");
        version.setCreatedBy(report.getCreatedBy());
        reportVersionMapper.insert(version);
    }

    public void update(Long id, Report report) {
        Report existing = reportMapper.selectById(id);
        if (existing == null) throw new BusinessException("报告不存在");
        report.setId(id);
        reportMapper.updateById(report);
    }

    public void delete(Long id) {
        reportMapper.deleteById(id);
        reportVersionMapper.delete(
                new LambdaQueryWrapper<ReportVersion>().eq(ReportVersion::getReportId, id));
    }

    public List<ReportVersionVO> getVersions(Long reportId) {
        List<ReportVersion> versions = reportVersionMapper.selectList(
                new LambdaQueryWrapper<ReportVersion>()
                        .eq(ReportVersion::getReportId, reportId)
                        .orderByDesc(ReportVersion::getVersion));

        return versions.stream().map(this::toVersionVO).collect(Collectors.toList());
    }

    @Transactional
    public void createVersion(Long reportId, String changeNote) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) throw new BusinessException("报告不存在");

        int newVersion = report.getVersion() + 1;

        ReportVersion version = new ReportVersion();
        version.setReportId(reportId);
        version.setVersion(newVersion);
        version.setContentMd(report.getContentMd());
        version.setContentHtml(report.getContentHtml());
        version.setChartConfig(report.getChartConfig());
        version.setChangeNote(changeNote);
        version.setCreatedBy(SecurityUtil.getCurrentUserId());
        reportVersionMapper.insert(version);

        report.setVersion(newVersion);
        reportMapper.updateById(report);
    }

    @Transactional
    public void restoreVersion(Long reportId, Long versionId) {
        ReportVersion version = reportVersionMapper.selectById(versionId);
        if (version == null || !version.getReportId().equals(reportId)) {
            throw new BusinessException("版本不存在");
        }

        Report report = reportMapper.selectById(reportId);
        report.setContentMd(version.getContentMd());
        report.setContentHtml(version.getContentHtml());
        report.setChartConfig(version.getChartConfig());
        report.setVersion(version.getVersion());
        reportMapper.updateById(report);
    }

    public String share(Long reportId) {
        return "/reports/" + reportId + "?share=true";
    }

    private ReportVO toVO(Report report) {
        ReportVO vo = new ReportVO();
        vo.setId(report.getId());
        vo.setTitle(report.getTitle());
        vo.setDescription(report.getDescription());
        vo.setContentMd(report.getContentMd());
        vo.setContentHtml(report.getContentHtml());
        vo.setSessionId(report.getSessionId());
        vo.setStatus(report.getStatus());
        vo.setVersion(report.getVersion());
        vo.setCreatedBy(String.valueOf(report.getCreatedBy()));
        vo.setCreatedAt(report.getCreatedAt());
        vo.setUpdatedAt(report.getUpdatedAt());
        return vo;
    }

    private ReportVersionVO toVersionVO(ReportVersion version) {
        ReportVersionVO vo = new ReportVersionVO();
        vo.setId(version.getId());
        vo.setReportId(version.getReportId());
        vo.setVersion(version.getVersion());
        vo.setContentMd(version.getContentMd());
        vo.setChangeNote(version.getChangeNote());
        vo.setCreatedBy(String.valueOf(version.getCreatedBy()));
        vo.setCreatedAt(version.getCreatedAt());
        return vo;
    }
}
