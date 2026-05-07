package org.example.ai_educatin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.result.Result;
import org.example.ai_educatin.dto.demand.TutoringDemandDTO;
import org.example.ai_educatin.entity.TutoringDemand;
import org.example.ai_educatin.service.TutoringDemandService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demand")
@RequiredArgsConstructor
public class TutoringDemandController {

    private final TutoringDemandService tutoringDemandService;

    /**
     * 发布辅导需求（家长端）
     * TODO: parentId 后续从JWT Token获取
     */
    @PostMapping
    public Result<TutoringDemand> publish(@RequestParam Long parentId,
                                          @Valid @RequestBody TutoringDemandDTO dto) {
        return Result.success(tutoringDemandService.publish(parentId, dto));
    }

    /**
     * 家长查看自己的需求列表
     */
    @GetMapping("/my")
    public Result<IPage<TutoringDemand>> myDemands(@RequestParam Long parentId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return Result.success(tutoringDemandService.pageByParent(parentId, page, size));
    }

    /**
     * 浏览需求列表（老师端，按区域/科目筛选）
     */
    @GetMapping("/list")
    public Result<IPage<TutoringDemand>> listPublished(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(tutoringDemandService.pagePublished(district, subject, page, size));
    }

    /**
     * 查看需求详情
     */
    @GetMapping("/{id}")
    public Result<TutoringDemand> getDetail(@PathVariable Long id) {
        return Result.success(tutoringDemandService.getById(id));
    }

    /**
     * 关闭需求（家长端）
     */
    @PutMapping("/{id}/close")
    public Result<Void> closeDemand(@PathVariable Long id, @RequestParam Long parentId) {
        tutoringDemandService.closeDemand(id, parentId);
        return Result.success();
    }
}
