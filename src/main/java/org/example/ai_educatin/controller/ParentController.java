package org.example.ai_educatin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.enums.UserRole;
import org.example.ai_educatin.common.result.Result;
import org.example.ai_educatin.entity.dto.demand.DemandCreateDTO;
import org.example.ai_educatin.entity.dto.ticket.TicketCreateDTO;
import org.example.ai_educatin.entity.dto.user.ParentLoginDTO;
import org.example.ai_educatin.entity.dto.user.SendCodeDTO;
import org.example.ai_educatin.entity.Demand;
import org.example.ai_educatin.entity.StudentProfile;
import org.example.ai_educatin.entity.Ticket;
import org.example.ai_educatin.entity.User;
import org.example.ai_educatin.service.DemandService;
import org.example.ai_educatin.service.RecommendationService;
import org.example.ai_educatin.service.TicketService;
import org.example.ai_educatin.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 家长端 API
 */
@Tag(name = "家长端", description = "家长端H5接口")
@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentController {

    private final UserService userService;
    private final DemandService demandService;
    private final RecommendationService recommendationService;
    private final TicketService ticketService;

    // ==================== 认证相关 ====================

    @Operation(summary = "发送验证码")
    @PostMapping("/auth/send-code")
    public Result<Void> sendCode(@RequestBody @Valid SendCodeDTO dto) {
        userService.sendVerifyCode(dto.getPhone());
        return Result.success();
    }

    @Operation(summary = "家长登录/注册")
    @PostMapping("/auth/login")
    public Result<User> login(@RequestBody @Valid ParentLoginDTO dto) {
        User user = userService.parentLogin(dto);
        return Result.success(user);
    }

    // ==================== 需求相关 ====================

    @Operation(summary = "提交家教需求")
    @PostMapping("/demand")
    public Result<Demand> createDemand(@RequestHeader("X-User-Id") Long userId,
                                       @RequestBody @Valid DemandCreateDTO dto) {
        Demand demand = demandService.createDemand(userId, dto);
        return Result.success(demand);
    }

    @Operation(summary = "我的需求列表")
    @GetMapping("/demand/list")
    public Result<List<Demand>> myDemands(@RequestHeader("X-User-Id") Long userId) {
        List<Demand> demands = demandService.listByUserId(userId);
        return Result.success(demands);
    }

    @Operation(summary = "查看需求详情")
    @GetMapping("/demand/{demandId}")
    public Result<Demand> getDemand(@PathVariable Long demandId) {
        Demand demand = demandService.getById(demandId);
        return Result.success(demand);
    }

    @Operation(summary = "查看推荐的学生列表")
    @GetMapping("/demand/{demandId}/recommendations")
    public Result<List<StudentProfile>> getRecommendations(@PathVariable Long demandId) {
        List<StudentProfile> students = recommendationService.getRecommendedStudents(demandId);
        return Result.success(students);
    }

    @Operation(summary = "关闭需求")
    @PostMapping("/demand/{demandId}/close")
    public Result<Void> closeDemand(@PathVariable Long demandId,
                                    @RequestParam(required = false) String reason) {
        demandService.closeDemand(demandId, reason);
        return Result.success();
    }

    // ==================== 工单相关 ====================

    @Operation(summary = "提交咨询工单")
    @PostMapping("/ticket")
    public Result<Ticket> createTicket(@RequestHeader("X-User-Id") Long userId,
                                       @RequestBody @Valid TicketCreateDTO dto) {
        Ticket ticket = ticketService.createTicket(userId, UserRole.PARENT.getCode(), dto);
        return Result.success(ticket);
    }

    @Operation(summary = "我的工单列表")
    @GetMapping("/ticket/list")
    public Result<List<Ticket>> myTickets(@RequestHeader("X-User-Id") Long userId) {
        List<Ticket> tickets = ticketService.listByUserId(userId);
        return Result.success(tickets);
    }
}
