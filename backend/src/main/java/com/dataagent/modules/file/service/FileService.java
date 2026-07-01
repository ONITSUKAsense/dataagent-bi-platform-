package com.dataagent.modules.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dataagent.common.model.PageResult;
import com.dataagent.common.util.SecurityUtil;
import com.dataagent.modules.file.entity.FileEntity;
import com.dataagent.modules.file.mapper.FileMapper;
import com.dataagent.modules.file.vo.FileVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMapper fileMapper;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadPath));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public FileVO upload(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + ext;
        Path targetPath = Paths.get(uploadPath, storedName);
        Files.copy(file.getInputStream(), targetPath);

        FileEntity entity = new FileEntity();
        entity.setOriginalName(originalName);
        entity.setStoredName(storedName);
        entity.setPath(targetPath.toString());
        entity.setSize(file.getSize());
        entity.setType(file.getContentType());
        entity.setExt(ext);
        entity.setStorageType("local");
        entity.setUrl("/api/files/" + storedName + "/preview");
        entity.setCreatedBy(SecurityUtil.getCurrentUserId());
        fileMapper.insert(entity);

        return toVO(entity);
    }

    public PageResult<FileVO> page(int page, int pageSize, String type) {
        IPage<FileEntity> filePage = fileMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<FileEntity>()
                        .eq(type != null, FileEntity::getType, type)
                        .orderByDesc(FileEntity::getCreatedAt));

        return PageResult.of(filePage.convert(this::toVO));
    }

    public void delete(Long id) {
        FileEntity entity = fileMapper.selectById(id);
        if (entity != null) {
            try {
                Files.deleteIfExists(Paths.get(entity.getPath()));
            } catch (IOException e) {
                // ignore
            }
            fileMapper.deleteById(id);
        }
    }

    public FileEntity getById(Long id) {
        return fileMapper.selectById(id);
    }

    private FileVO toVO(FileEntity entity) {
        FileVO vo = new FileVO();
        vo.setId(entity.getId());
        vo.setOriginalName(entity.getOriginalName());
        vo.setSize(entity.getSize());
        vo.setType(entity.getType());
        vo.setExt(entity.getExt());
        vo.setUrl(entity.getUrl());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
