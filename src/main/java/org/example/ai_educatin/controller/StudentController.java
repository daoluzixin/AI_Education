package org.example.ai_educatin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.enums.UserRole;
import org.example.ai_educatin.common.result.Result;
import org.example.ai_educatin.entity.dto.student.StudentProfileDTO;
import org.example.ai_educatin.entity.dto.student.SupplementDTO;
import org.example.ai_educatin.entity.dto.ticket.TicketCreateDTO;
import org.example.ai_educatin.entity.dto.user.SendCodeDTO;
import org.example.ai_educatin.entity.dto.user.StudentLoginDTO;
import org.example.ai_educatin.entity.StudentProfile;
import org.example.ai_educatin.entity.Ticket;
import org.example.ai_educatin.entity.User;
import org.example.ai_educatin.service.StudentProfileService;
import org.example.ai_educatin.service.TicketService;
import org.example.ai_educatin.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端 API
 */
@Tag(name = "学生端", description = "学生端H5接口")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final TicketService ticketService;

    // ==================== 认证相关 ====================

    @Operation(summary = "发送验证码")
    @PostMapping("/auth/send-code")
    public Result<Void> sendCode(@RequestBody @Valid SendCodeDTO dto) {
        userService.sendVerifyCode(dto.getPhone());
        return Result.success();
    }

    @Operation(summary = "学生登录/注册")
    @PostMapping("/auth/login")
    public Result<User> login(@RequestBody @Valid StudentLoginDTO dto) {
        User user = userService.studentLogin(dto);
        return Result.success(user);
    }

    // ==================== 档案相关 ====================

    @Operation(summary = "保存档案草稿")
    @PostMapping("/profile/draft")
    public Result<StudentProfile> saveDraft(@RequestHeader("X-User-Id") Long userId,
                                            @RequestBody @Valid StudentProfileDTO dto) {
        StudentProfile profile = studentProfileService.saveDraft(userId, dto);
        return Result.success(profile);
    }

    @Operation(summary = "提交档案审核")
    @PostMapping("/profile/submit")
    public Result<StudentProfile> submitForReview(@RequestHeader("X-User-Id") Long userId,
                                                  @RequestBody @Valid StudentProfileDTO dto) {
        StudentProfile profile = studentProfileService.submitForReview(userId, dto);
        return Result.success(profile);
    }

    @Operation(summary = "查看我的档案")
    @GetMapping("/profile")
    public Result<StudentProfile> getMyProfile(@RequestHeader("X-User-Id") Long userId) {
        StudentProfile profile = studentProfileService.getByUserId(userId);
        return Result.success(profile);
    }

    @Operation(summary = "补充材料")
    @PostMapping("/profile/supplement")
    public Result<StudentProfile> supplement(@RequestHeader("X-User-Id") Long userId,
                                             @RequestBody SupplementDTO dto) {
        StudentProfile profile = studentProfileService.supplement(userId, dto);
        return Result.success(profile);
    }

    // ==================== 工单相关 ====================

    @Operation(summary = "提交咨询工单")
    @PostMapping("/ticket")
    public Result<Ticket> createTicket(@RequestHeader("X-User-Id") Long userId,
                                       @RequestBody @Valid TicketCreateDTO dto) {
        Ticket ticket = ticketService.createTicket(userId, UserRole.STUDENT.getCode(), dto);
        return Result.success(ticket);
    }

    @Operation(summary = "我的工单列表")
    @GetMapping("/ticket/list")
    public Result<List<Ticket>> myTickets(@RequestHeader("X-User-Id") Long userId) {
        List<Ticket> tickets = ticketService.listByUserId(userId);
        return Result.success(tickets);
    }
}
