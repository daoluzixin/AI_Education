package org.example.ai_educatin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.enums.DemandStatus;
import org.example.ai_educatin.common.enums.ReviewStatus;
import org.example.ai_educatin.common.enums.TicketStatus;
import org.example.ai_educatin.common.enums.UserRole;
import org.example.ai_educatin.common.result.Result;
import org.example.ai_educatin.entity.dto.admin.ReviewDTO;
import org.example.ai_educatin.entity.dto.demand.DemandQueryDTO;
import org.example.ai_educatin.entity.dto.student.StudentQueryDTO;
import org.example.ai_educatin.entity.dto.ticket.TicketQueryDTO;
import org.example.ai_educatin.entity.dto.ticket.TicketReplyDTO;
import org.example.ai_educatin.entity.dto.user.AdminLoginDTO;
import org.example.ai_educatin.entity.*;
import org.example.ai_educatin.mapper.DemandMapper;
import org.example.ai_educatin.mapper.StudentProfileMapper;
import org.example.ai_educatin.mapper.TicketMapper;
import org.example.ai_educatin.mapper.UserMapper;
import org.example.ai_educatin.service.*;
import org.example.ai_educatin.vo.CandidateVO;
import org.example.ai_educatin.vo.DashboardVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台管理端 API
 */
@Tag(name = "后台管理端", description = "管理员PC Web接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final StudentProfileService studentProfileService;
    private final ReviewRecordService reviewRecordService;
    private final DemandService demandService;
    private final RecommendationService recommendationService;
    private final MatchingService matchingService;
    private final TicketService ticketService;
    private final UserMapper userMapper;
    private final DemandMapper demandMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final TicketMapper ticketMapper;

    // ==================== 认证相关 ====================

    @Operation(summary = "管理员登录")
    @PostMapping("/auth/login")
    public Result<User> login(@RequestBody @Valid AdminLoginDTO dto) {
        User user = userService.adminLogin(dto);
        return Result.success(user);
    }

    // ==================== 学生审核 ====================

    @Operation(summary = "学生列表（分页）")
    @GetMapping("/student/list")
    public Result<IPage<StudentProfile>> studentList(StudentQueryDTO dto) {
        IPage<StudentProfile> page = studentProfileService.pageQuery(dto);
        return Result.success(page);
    }

    @Operation(summary = "学生详情")
    @GetMapping("/student/{studentId}")
    public Result<StudentProfile> studentDetail(@PathVariable Long studentId) {
        StudentProfile profile = studentProfileService.getById(studentId);
        return Result.success(profile);
    }

    @Operation(summary = "执行审核")
    @PostMapping("/student/{studentId}/review")
    public Result<Void> review(@RequestHeader("X-User-Id") Long reviewerId,
                               @RequestHeader("X-User-Name") String reviewerName,
                               @PathVariable Long studentId,
                               @RequestBody @Valid ReviewDTO dto) {
        reviewRecordService.doReview(studentId, reviewerId, reviewerName, dto);
        return Result.success();
    }

    @Operation(summary = "查看审核记录")
    @GetMapping("/student/{studentId}/review-records")
    public Result<List<ReviewRecord>> reviewRecords(@PathVariable Long studentId) {
        List<ReviewRecord> records = reviewRecordService.listByStudentId(studentId);
        return Result.success(records);
    }

    // ==================== 需求管理 ====================

    @Operation(summary = "需求列表（分页）")
    @GetMapping("/demand/list")
    public Result<IPage<Demand>> demandList(DemandQueryDTO dto) {
        IPage<Demand> page = demandService.pageQuery(dto);
        return Result.success(page);
    }

    @Operation(summary = "需求详情")
    @GetMapping("/demand/{demandId}")
    public Result<Demand> demandDetail(@PathVariable Long demandId) {
        Demand demand = demandService.getById(demandId);
        return Result.success(demand);
    }

    @Operation(summary = "开始匹配")
    @PostMapping("/demand/{demandId}/start-matching")
    public Result<Void> startMatching(@PathVariable Long demandId) {
        demandService.startMatching(demandId);
        return Result.success();
    }

    @Operation(summary = "查看推荐列表")
    @GetMapping("/demand/{demandId}/recommendations")
    public Result<List<StudentProfile>> getRecommendations(@PathVariable Long demandId) {
        List<StudentProfile> students = recommendationService.getRecommendedStudents(demandId);
        return Result.success(students);
    }

    @Operation(summary = "智能匹配候选学生")
    @GetMapping("/demand/{demandId}/candidates")
    public Result<List<CandidateVO>> findCandidates(@PathVariable Long demandId,
                                                    @RequestParam(defaultValue = "10") int limit) {
        List<CandidateVO> candidates = matchingService.findCandidates(demandId, limit);
        return Result.success(candidates);
    }

    @Operation(summary = "关闭需求")
    @PostMapping("/demand/{demandId}/close")
    public Result<Void> closeDemand(@PathVariable Long demandId,
                                    @RequestParam(required = false) String reason) {
        demandService.closeDemand(demandId, reason);
        return Result.success();
    }

    // ==================== 工单管理 ====================

    @Operation(summary = "工单列表（分页）")
    @GetMapping("/ticket/list")
    public Result<IPage<Ticket>> ticketList(TicketQueryDTO dto) {
        IPage<Ticket> page = ticketService.pageQuery(dto);
        return Result.success(page);
    }

    @Operation(summary = "工单详情")
    @GetMapping("/ticket/{ticketId}")
    public Result<Ticket> ticketDetail(@PathVariable Long ticketId) {
        Ticket ticket = ticketService.getById(ticketId);
        return Result.success(ticket);
    }

    @Operation(summary = "接单")
    @PostMapping("/ticket/{ticketId}/accept")
    public Result<Void> acceptTicket(@RequestHeader("X-User-Id") Long handlerId,
                                     @PathVariable Long ticketId) {
        ticketService.acceptTicket(ticketId, handlerId);
        return Result.success();
    }

    @Operation(summary = "回复工单")
    @PostMapping("/ticket/{ticketId}/reply")
    public Result<Void> replyTicket(@RequestHeader("X-User-Id") Long replierId,
                                    @RequestHeader("X-User-Name") String replierName,
                                    @PathVariable Long ticketId,
                                    @RequestBody @Valid TicketReplyDTO dto) {
        ticketService.replyTicket(ticketId, replierId, replierName, dto);
        return Result.success();
    }

    @Operation(summary = "查看工单回复")
    @GetMapping("/ticket/{ticketId}/replies")
    public Result<List<TicketReply>> ticketReplies(@PathVariable Long ticketId) {
        List<TicketReply> replies = ticketService.listReplies(ticketId);
        return Result.success(replies);
    }

    @Operation(summary = "关闭工单")
    @PostMapping("/ticket/{ticketId}/close")
    public Result<Void> closeTicket(@PathVariable Long ticketId) {
        ticketService.closeTicket(ticketId);
        return Result.success();
    }

    // ==================== 数据概览 ====================

    @Operation(summary = "数据概览（Dashboard）")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        DashboardVO vo = new DashboardVO();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 今日新增需求数
        vo.setTodayNewDemands(demandMapper.selectCount(
                new LambdaQueryWrapper<Demand>().ge(Demand::getCreateTime, todayStart)));

        // 待处理需求数
        vo.setPendingDemands(demandMapper.selectCount(
                new LambdaQueryWrapper<Demand>().eq(Demand::getStatus, DemandStatus.PENDING.getCode())));

        // 今日新增报名数（学生提交审核）
        vo.setTodayNewStudents(studentProfileMapper.selectCount(
                new LambdaQueryWrapper<StudentProfile>()
                        .ge(StudentProfile::getCreateTime, todayStart)
                        .ne(StudentProfile::getReviewStatus, ReviewStatus.DRAFT.getCode())));

        // 待审核学生数
        vo.setPendingReviewStudents(studentProfileMapper.selectCount(
                new LambdaQueryWrapper<StudentProfile>()
                        .eq(StudentProfile::getReviewStatus, ReviewStatus.PENDING_REVIEW.getCode())));

        // 待处理工单数
        vo.setPendingTickets(ticketMapper.selectCount(
                new LambdaQueryWrapper<Ticket>().eq(Ticket::getStatus, TicketStatus.PENDING.getCode())));

        // 累计注册家长数
        vo.setTotalParents(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, UserRole.PARENT.getCode())));

        // 累计入驻学生数（审核通过）
        vo.setTotalApprovedStudents(studentProfileMapper.selectCount(
                new LambdaQueryWrapper<StudentProfile>()
                        .eq(StudentProfile::getReviewStatus, ReviewStatus.APPROVED.getCode())));

        return Result.success(vo);
    }
}
