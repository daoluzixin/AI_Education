package org.example.ai_educatin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.result.Result;
import org.example.ai_educatin.dto.teacher.TeacherProfileDTO;
import org.example.ai_educatin.entity.TeacherProfile;
import org.example.ai_educatin.service.TeacherProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherProfileController {

    private final TeacherProfileService teacherProfileService;

    /**
     * 提交/更新教师档案
     * TODO: userId 后续从JWT Token中获取，当前先用参数传入
     */
    @PostMapping("/profile")
    public Result<TeacherProfile> saveProfile(@RequestParam Long userId,
                                              @Valid @RequestBody TeacherProfileDTO dto) {
        return Result.success(teacherProfileService.saveOrUpdateProfile(userId, dto));
    }

    /**
     * 获取自己的教师档案
     */
    @GetMapping("/profile")
    public Result<TeacherProfile> getMyProfile(@RequestParam Long userId) {
        return Result.success(teacherProfileService.getByUserId(userId));
    }

    /**
     * 查看某老师的详情（家长端查看）
     */
    @GetMapping("/profile/{id}")
    public Result<TeacherProfile> getTeacherDetail(@PathVariable Long id) {
        return Result.success(teacherProfileService.getById(id));
    }

    /**
     * 分页搜索老师列表（家长端）
     * 按区域、科目、性别筛选
     */
    @GetMapping("/list")
    public Result<IPage<TeacherProfile>> listTeachers(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Integer gender,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(teacherProfileService.pageTeachers(district, subject, gender, page, size));
    }
}
