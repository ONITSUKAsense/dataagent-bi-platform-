package com.dataagent.modules.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dataagent.common.model.PageResult;
import com.dataagent.common.util.SecurityUtil;
import com.dataagent.modules.knowledge.entity.KnowledgeChunk;
import com.dataagent.modules.knowledge.entity.KnowledgeDoc;
import com.dataagent.modules.knowledge.mapper.KnowledgeChunkMapper;
import com.dataagent.modules.knowledge.mapper.KnowledgeDocMapper;
import com.dataagent.modules.knowledge.vo.KnowledgeDocVO;
import com.dataagent.modules.knowledge.vo.ChunkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeDocMapper knowledgeDocMapper;
    private final KnowledgeChunkMapper knowledgeChunkMapper;

    public PageResult<KnowledgeDocVO> page(int page, int pageSize) {
        IPage<KnowledgeDoc> docPage = knowledgeDocMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<KnowledgeDoc>().orderByDesc(KnowledgeDoc::getCreatedAt));

        return PageResult.of(docPage.convert(this::toDocVO));
    }

    public KnowledgeDocVO getById(Long id) {
        KnowledgeDoc doc = knowledgeDocMapper.selectById(id);
        return doc != null ? toDocVO(doc) : null;
    }

    @Transactional
    public Long create(KnowledgeDoc doc) {
        doc.setCreatedBy(SecurityUtil.getCurrentUserId());
        doc.setStatus(0); // Processing
        knowledgeDocMapper.insert(doc);
        return doc.getId();
    }

    @Transactional
    public void updateStatus(Long id, int status, int chunkCount) {
        KnowledgeDoc doc = knowledgeDocMapper.selectById(id);
        if (doc != null) {
            doc.setStatus(status);
            doc.setChunkCount(chunkCount);
            knowledgeDocMapper.updateById(doc);
        }
    }

    public void saveChunks(Long docId, List<KnowledgeChunk> chunks) {
        for (KnowledgeChunk chunk : chunks) {
            chunk.setDocId(docId);
            knowledgeChunkMapper.insert(chunk);
        }
    }

    @Transactional
    public void delete(Long id) {
        knowledgeDocMapper.deleteById(id);
        knowledgeChunkMapper.delete(
                new LambdaQueryWrapper<KnowledgeChunk>().eq(KnowledgeChunk::getDocId, id));
    }

    public List<ChunkVO> getChunks(Long docId) {
        List<KnowledgeChunk> chunks = knowledgeChunkMapper.selectList(
                new LambdaQueryWrapper<KnowledgeChunk>()
                        .eq(KnowledgeChunk::getDocId, docId)
                        .orderByAsc(KnowledgeChunk::getChunkIndex));

        return chunks.stream().map(c -> {
            ChunkVO vo = new ChunkVO();
            vo.setId(c.getId());
            vo.setDocId(c.getDocId());
            vo.setChunkIndex(c.getChunkIndex());
            vo.setContent(c.getContent());
            return vo;
        }).collect(Collectors.toList());
    }

    private KnowledgeDocVO toDocVO(KnowledgeDoc doc) {
        KnowledgeDocVO vo = new KnowledgeDocVO();
        vo.setId(doc.getId());
        vo.setTitle(doc.getTitle());
        vo.setDocType(doc.getDocType());
        vo.setStatus(doc.getStatus());
        vo.setChunkCount(doc.getChunkCount());
        vo.setCreatedBy(String.valueOf(doc.getCreatedBy()));
        vo.setCreatedAt(doc.getCreatedAt());
        vo.setUpdatedAt(doc.getUpdatedAt());
        return vo;
    }
}
