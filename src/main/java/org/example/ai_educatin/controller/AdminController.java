package org.example.ai_educatin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.result.Result;
import org.example.ai_educatin.dto.admin.AuditRejectDTO;
import org.example.ai_educatin.entity.TeacherProfile;
import org.example.ai_educatin.service.TeacherProfileService;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员接口
 * TODO: 后续加 JWT 鉴权时，需校验当前用户 role=3(管理员)
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TeacherProfileService teacherProfileService;

    /**
     * 查看教师审核列表
     * authStatus: 0-待审核 1-已通过 2-已拒绝，不传则查全部
     */
    @GetMapping("/teacher/audit/list")
    public Result<IPage<TeacherProfile>> auditList(
            @RequestParam(required = false) Integer authStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(teacherProfileService.pageByAuthStatus(authStatus, page, size));
    }

    /**
     * 查看某教师档案详情（审核时查看）
     */
    @GetMapping("/teacher/audit/{id}")
    public Result<TeacherProfile> auditDetail(@PathVariable Long id) {
        return Result.success(teacherProfileService.getById(id));
    }

    /**
     * 审核通过
     */
    @PutMapping("/teacher/audit/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        teacherProfileService.approve(id);
        return Result.success();
    }

    /**
     * 审核拒绝
     */
    @PutMapping("/teacher/audit/{id}/reject")
    public Result<Void> reject(@PathVariable Long id,
                               @Valid @RequestBody AuditRejectDTO dto) {
        teacherProfileService.reject(id, dto.getReason());
        return Result.success();
    }
}
