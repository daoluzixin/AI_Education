package org.example.ai_educatin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.result.Result;
import org.example.ai_educatin.dto.parent.ParentProfileDTO;
import org.example.ai_educatin.entity.ParentProfile;
import org.example.ai_educatin.service.ParentProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentProfileController {

    private final ParentProfileService parentProfileService;

    /**
     * 提交/更新家长档案
     * TODO: userId 后续从JWT Token中获取
     */
    @PostMapping("/profile")
    public Result<ParentProfile> saveProfile(@RequestParam Long userId,
                                             @Valid @RequestBody ParentProfileDTO dto) {
        return Result.success(parentProfileService.saveOrUpdateProfile(userId, dto));
    }

    /**
     * 获取自己的家长档案
     */
    @GetMapping("/profile")
    public Result<ParentProfile> getMyProfile(@RequestParam Long userId) {
        return Result.success(parentProfileService.getByUserId(userId));
    }
}
