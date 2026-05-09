package org.example.ai_educatin.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.enums.ReviewStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.dto.student.StudentProfileDTO;
import org.example.ai_educatin.entity.dto.student.StudentQueryDTO;
import org.example.ai_educatin.entity.dto.student.SupplementDTO;
import org.example.ai_educatin.entity.StudentProfile;
import org.example.ai_educatin.mapper.StudentProfileMapper;
import org.example.ai_educatin.service.StudentProfileService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StudentProfileServiceImpl extends ServiceImpl<StudentProfileMapper, StudentProfile>
        implements StudentProfileService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public StudentProfile saveDraft(Long userId, StudentProfileDTO dto) {
        StudentProfile profile = getByUserId(userId);

        if (profile == null) {
            profile = new StudentProfile();
            profile.setUserId(userId);
            profile.setReviewStatus(ReviewStatus.DRAFT.getCode());
            profile.setStudentNo(""); // 草稿时暂无编号
            copyDtoToEntity(dto, profile);
            save(profile);
        } else {
            // 只有草稿和驳回状态可以编辑
            ReviewStatus currentStatus = ReviewStatus.fromCode(profile.getReviewStatus());
            if (currentStatus != ReviewStatus.DRAFT && currentStatus != ReviewStatus.REJECTED) {
                throw new BusinessException(400, "当前状态不允许编辑");
            }
            copyDtoToEntity(dto, profile);
            updateById(profile);
        }

        return profile;
    }

    @Override
    public StudentProfile submitForReview(Long userId, StudentProfileDTO dto) {
        StudentProfile profile = getByUserId(userId);

        if (profile == null) {
            // 没有草稿则直接创建并提交
            profile = new StudentProfile();
            profile.setUserId(userId);
            copyDtoToEntity(dto, profile);
        } else {
            ReviewStatus currentStatus = ReviewStatus.fromCode(profile.getReviewStatus());
            if (currentStatus != ReviewStatus.DRAFT && currentStatus != ReviewStatus.REJECTED) {
                throw new BusinessException(400, "当前状态不允许提交审核");
            }
            copyDtoToEntity(dto, profile);
        }

        // 生成学生编号
        if (!StringUtils.hasText(profile.getStudentNo()) || profile.getStudentNo().isEmpty()) {
            profile.setStudentNo(generateStudentNo());
        }
        profile.setReviewStatus(ReviewStatus.PENDING_REVIEW.getCode());

        if (profile.getId() == null) {
            save(profile);
        } else {
            updateById(profile);
        }

        return profile;
    }

    @Override
    public StudentProfile getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<StudentProfile>()
                .eq(StudentProfile::getUserId, userId));
    }

    @Override
    public StudentProfile supplement(Long userId, SupplementDTO dto) {
        StudentProfile profile = getByUserId(userId);
        if (profile == null) {
            throw new BusinessException(404, "学生档案不存在");
        }

        ReviewStatus currentStatus = ReviewStatus.fromCode(profile.getReviewStatus());
        if (currentStatus != ReviewStatus.NEED_SUPPLEMENT) {
            throw new BusinessException(400, "当前状态不允许补充材料");
        }

        // 更新补充材料
        if (StringUtils.hasText(dto.getSupplements())) {
            profile.setSupplements(dto.getSupplements());
        }
        if (StringUtils.hasText(dto.getStudentIdPhoto())) {
            profile.setStudentIdPhoto(dto.getStudentIdPhoto());
        }
        if (StringUtils.hasText(dto.getCertificates())) {
            profile.setCertificates(dto.getCertificates());
        }
        if (StringUtils.hasText(dto.getTranscripts())) {
            profile.setTranscripts(dto.getTranscripts());
        }

        // 状态流转: NEED_SUPPLEMENT → PENDING_REVIEW
        profile.setReviewStatus(ReviewStatus.PENDING_REVIEW.getCode());
        updateById(profile);

        return profile;
    }

    @Override
    public IPage<StudentProfile> pageQuery(StudentQueryDTO dto) {
        Page<StudentProfile> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<StudentProfile> wrapper = new LambdaQueryWrapper<>();

        // 后台列表过滤掉草稿
        wrapper.ne(StudentProfile::getReviewStatus, ReviewStatus.DRAFT.getCode());

        if (dto.getReviewStatus() != null) {
            wrapper.eq(StudentProfile::getReviewStatus, dto.getReviewStatus());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(StudentProfile::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getSchoolName())) {
            wrapper.like(StudentProfile::getSchoolName, dto.getSchoolName());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(StudentProfile::getRealName, dto.getKeyword())
                    .or().like(StudentProfile::getSchoolName, dto.getKeyword()));
        }
        if (dto.getStartTime() != null) {
            wrapper.ge(StudentProfile::getCreateTime, dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            wrapper.le(StudentProfile::getCreateTime, dto.getEndTime());
        }

        wrapper.orderByDesc(StudentProfile::getCreateTime);
        return page(page, wrapper);
    }

    /**
     * 生成学生编号: STU + yyyyMMdd + 4位流水号
     */
    private String generateStudentNo() {
        String today = DateUtil.format(DateUtil.date(), "yyyyMMdd");
        String key = "seq:STU:" + today;
        Long seq = redisTemplate.opsForValue().increment(key);
        if (seq != null && seq == 1L) {
            // 设置次日凌晨过期
            redisTemplate.expireAt(key, java.util.Date.from(
                    LocalDate.now().plusDays(1).atStartOfDay()
                            .atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        return String.format("STU%s%04d", today, seq);
    }

    private void copyDtoToEntity(StudentProfileDTO dto, StudentProfile entity) {
        entity.setRealName(dto.getRealName());
        entity.setGender(dto.getGender());
        if (StringUtils.hasText(dto.getBirthDate())) {
            entity.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        }
        entity.setCity(dto.getCity());
        entity.setSchoolName(dto.getSchoolName());
        entity.setGrade(dto.getGrade());
        entity.setAvatar(dto.getAvatar());
        entity.setIntroduction(dto.getIntroduction());
        entity.setSubjects(dto.getSubjects());
        entity.setTags(dto.getTags());
        entity.setStudentIdPhoto(dto.getStudentIdPhoto());
        entity.setCertificates(dto.getCertificates());
        entity.setTranscripts(dto.getTranscripts());
        entity.setSupplements(dto.getSupplements());
    }
}
