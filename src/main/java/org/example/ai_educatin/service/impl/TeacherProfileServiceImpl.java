package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ai_educatin.common.enums.AuthStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.dto.teacher.TeacherProfileDTO;
import org.example.ai_educatin.entity.TeacherProfile;
import org.example.ai_educatin.mapper.TeacherProfileMapper;
import org.example.ai_educatin.service.TeacherProfileService;
import org.springframework.stereotype.Service;

@Service
public class TeacherProfileServiceImpl extends ServiceImpl<TeacherProfileMapper, TeacherProfile>
        implements TeacherProfileService {

    @Override
    public TeacherProfile saveOrUpdateProfile(Long userId, TeacherProfileDTO dto) {
        TeacherProfile profile = getByUserId(userId);
        if (profile == null) {
            profile = new TeacherProfile();
            profile.setUserId(userId);
        }
        // 无论新建还是修改，都重置为待审核（被拒绝后修改资料需要重新审核）
        profile.setAuthStatus(AuthStatus.PENDING.getCode());
        profile.setRejectReason(null);
        // 填充字段
        profile.setRealName(dto.getRealName());
        profile.setGender(dto.getGender());
        profile.setUniversity(dto.getUniversity());
        profile.setMajor(dto.getMajor());
        profile.setEducationLevel(dto.getEducationLevel());
        profile.setGrade(dto.getGrade());
        profile.setSelfIntro(dto.getSelfIntro());
        profile.setTeachingExperience(dto.getTeachingExperience());
        profile.setSubjects(dto.getSubjects());
        profile.setGradeRange(dto.getGradeRange());
        profile.setDistrict(dto.getDistrict());
        profile.setDetailAddress(dto.getDetailAddress());
        profile.setTeachMode(dto.getTeachMode());
        profile.setPricePerHour(dto.getPricePerHour());

        saveOrUpdate(profile);
        return profile;
    }

    @Override
    public TeacherProfile getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<TeacherProfile>()
                .eq(TeacherProfile::getUserId, userId));
    }

    @Override
    public IPage<TeacherProfile> pageTeachers(String district, String subject,
                                               Integer gender, int page, int size) {
        LambdaQueryWrapper<TeacherProfile> wrapper = new LambdaQueryWrapper<>();
        // 只查已认证的老师
        wrapper.eq(TeacherProfile::getAuthStatus, AuthStatus.APPROVED.getCode());
        if (district != null && !district.isEmpty()) {
            wrapper.eq(TeacherProfile::getDistrict, district);
        }
        if (subject != null && !subject.isEmpty()) {
            wrapper.apply("FIND_IN_SET({0}, subjects) > 0", subject);
        }
        if (gender != null) {
            wrapper.eq(TeacherProfile::getGender, gender);
        }
        // 按评分降序,接单数降序
        wrapper.orderByDesc(TeacherProfile::getAvgRating)
               .orderByDesc(TeacherProfile::getTotalOrders);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<TeacherProfile> pageByAuthStatus(Integer authStatus, int page, int size) {
        LambdaQueryWrapper<TeacherProfile> wrapper = new LambdaQueryWrapper<>();
        if (authStatus != null) {
            wrapper.eq(TeacherProfile::getAuthStatus, authStatus);
        }
        wrapper.orderByDesc(TeacherProfile::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public void approve(Long teacherProfileId) {
        TeacherProfile profile = getById(teacherProfileId);
        if (profile == null) {
            throw new BusinessException(404, "教师档案不存在");
        }
        if (!profile.getAuthStatus().equals(AuthStatus.PENDING.getCode())) {
            throw new BusinessException("该档案当前状态不可审核，当前状态: "
                    + AuthStatus.values()[profile.getAuthStatus()].getDesc());
        }
        profile.setAuthStatus(AuthStatus.APPROVED.getCode());
        profile.setRejectReason(null);
        updateById(profile);
    }

    @Override
    public void reject(Long teacherProfileId, String reason) {
        TeacherProfile profile = getById(teacherProfileId);
        if (profile == null) {
            throw new BusinessException(404, "教师档案不存在");
        }
        if (!profile.getAuthStatus().equals(AuthStatus.PENDING.getCode())) {
            throw new BusinessException("该档案当前状态不可审核，当前状态: "
                    + AuthStatus.values()[profile.getAuthStatus()].getDesc());
        }
        profile.setAuthStatus(AuthStatus.REJECTED.getCode());
        profile.setRejectReason(reason);
        updateById(profile);
    }
}
