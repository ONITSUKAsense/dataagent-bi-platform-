package com.dataagent.modules.prompt.controller;

import com.dataagent.common.model.PageResult;
import com.dataagent.common.model.R;
import com.dataagent.modules.prompt.service.PromptService;
import com.dataagent.modules.prompt.vo.PromptVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
@Tag(name = "Prompt 管理")
public class PromptController {

    private final PromptService promptService;

    @GetMapping
    @Operation(summary = "Prompt 列表")
    @PreAuthorize("hasAuthority('prompt:list')")
    public R<PageResult<PromptVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return R.ok(promptService.page(page, pageSize, keyword));
    }

    @GetMapping("/published")
    @Operation(summary = "已发布的 Prompt")
    public R<List<PromptVO>> getPublished() {
        return R.ok(promptService.getPublished());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Prompt 详情")
    @PreAuthorize("hasAuthority('prompt:list')")
    public R<PromptVO> detail(@PathVariable Long id) {
        PromptVO vo = promptService.getById(id);
        return vo != null ? R.ok(vo) : R.error("不存在");
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "按编码获取")
    public R<PromptVO> getByCode(@PathVariable String code) {
        PromptVO vo = promptService.getByCode(code);
        return vo != null ? R.ok(vo) : R.error("不存在");
    }

    @PostMapping
    @Operation(summary = "新增 Prompt")
    @PreAuthorize("hasAuthority('prompt:add')")
    public R<Void> add(@RequestBody PromptVO vo) {
        promptService.add(vo);
        return R.ok();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新 Prompt")
    @PreAuthorize("hasAuthority('prompt:edit')")
    public R<Void> update(@PathVariable Long id, @RequestBody PromptVO vo) {
        promptService.update(id, vo);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除 Prompt")
    @PreAuthorize("hasAuthority('prompt:delete')")
    public R<Void> delete(@PathVariable Long id) {
        promptService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布 Prompt")
    @PreAuthorize("hasAuthority('prompt:edit')")
    public R<Void> publish(@PathVariable Long id) {
        promptService.publish(id);
        return R.ok();
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "测试渲染 Prompt")
    @PreAuthorize("hasAuthority('prompt:list')")
    public R<String> test(@PathVariable Long id, @RequestBody Map<String, String> variables) {
        String result = promptService.testRender(id, variables);
        return result != null ? R.ok(result) : R.error("Prompt 不存在");
    }
}
