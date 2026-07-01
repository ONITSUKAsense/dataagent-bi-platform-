package com.dataagent.modules.file.controller;

import com.dataagent.common.model.PageResult;
import com.dataagent.common.model.R;
import com.dataagent.common.util.SecurityUtil;
import com.dataagent.modules.file.service.FileService;
import com.dataagent.modules.file.vo.FileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件中心")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @PreAuthorize("hasAuthority('file:upload')")
    public R<FileVO> upload(@RequestParam("file") MultipartFile file) {
        try {
            return R.ok(fileService.upload(file));
        } catch (Exception e) {
            return R.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "文件列表")
    @PreAuthorize("hasAuthority('file:list')")
    public R<PageResult<FileVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type) {
        return R.ok(fileService.page(page, pageSize, type));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文件")
    @PreAuthorize("hasAuthority('file:delete')")
    public R<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return R.ok();
    }

    @GetMapping("/{storedName}/preview")
    @Operation(summary = "文件预览")
    public ResponseEntity<Resource> preview(@PathVariable String storedName) {
        try {
            Path filePath = Paths.get("./uploads", storedName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                        .body(resource);
            }
        } catch (MalformedURLException e) {
            // ignore
        }
        return ResponseEntity.notFound().build();
    }
}
