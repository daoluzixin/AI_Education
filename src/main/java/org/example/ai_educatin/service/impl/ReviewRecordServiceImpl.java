package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.enums.ReviewStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.dto.admin.ReviewDTO;
import org.example.ai_educatin.entity.ReviewRecord;
import org.example.ai_educatin.entity.StudentProfile;
import org.example.ai_educatin.mapper.ReviewRecordMapper;
import org.example.ai_educatin.service.ReviewRecordService;
import org.example.ai_educatin.service.StudentProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewRecordServiceImpl extends ServiceImpl<ReviewRecordMapper, ReviewRecord>
        implements ReviewRecordService {

    private final StudentProfileService studentProfileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doReview(Long studentProfileId, Long reviewerId, String reviewerName, ReviewDTO dto) {
        // 校验审核结果合法性
        int result = dto.getReviewResult();
        if (result != ReviewStatus.APPROVED.getCode()
                && result != ReviewStatus.REJECTED.getCode()
                && result != ReviewStatus.NEED_SUPPLEMENT.getCode()) {
            throw new BusinessException(400, "无效的审核结果");
        }

        // 驳回或要求补充时必须填写备注
        if ((result == ReviewStatus.REJECTED.getCode() || result == ReviewStatus.NEED_SUPPLEMENT.getCode())
                && !StringUtils.hasText(dto.getReviewNote())) {
            throw new BusinessException(400, "驳回或要求补充时必须填写审核备注");
        }

        // 获取学生档案
        StudentProfile profile = studentProfileService.getById(studentProfileId);
        if (profile == null) {
            throw new BusinessException(404, "学生档案不存在");
        }

        // 校验当前状态是否允许审核（只有 PENDING_REVIEW 状态才能审核）
        if (!profile.getReviewStatus().equals(ReviewStatus.PENDING_REVIEW.getCode())) {
            throw new BusinessException(400, "当前状态不允许审核操作");
        }

        // 1. 写入审核记录（只允许INSERT）
        ReviewRecord record = new ReviewRecord();
        record.setStudentId(studentProfileId);
        record.setReviewerId(reviewerId);
        record.setReviewerName(reviewerName);
        record.setReviewResult(result);
        record.setReviewNote(dto.getReviewNote());
        save(record);

        // 2. 更新学生档案审核状态
        profile.setReviewStatus(result);
        if (result == ReviewStatus.REJECTED.getCode()) {
            profile.setRejectReason(dto.getReviewNote());
        } else if (result == ReviewStatus.NEED_SUPPLEMENT.getCode()) {
            profile.setSupplementNote(dto.getReviewNote());
        }
        studentProfileService.updateById(profile);
    }

    @Override
    public List<ReviewRecord> listByStudentId(Long studentProfileId) {
        return list(new LambdaQueryWrapper<ReviewRecord>()
                .eq(ReviewRecord::getStudentId, studentProfileId)
                .orderByDesc(ReviewRecord::getCreateTime));
    }
}
