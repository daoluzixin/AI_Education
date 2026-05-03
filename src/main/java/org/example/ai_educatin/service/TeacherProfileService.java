package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.dto.teacher.TeacherProfileDTO;
import org.example.ai_educatin.entity.TeacherProfile;

public interface TeacherProfileService extends IService<TeacherProfile> {

    /**
     * 提交/更新教师档案
     */
    TeacherProfile saveOrUpdateProfile(Long userId, TeacherProfileDTO dto);

    /**
     * 根据userId获取教师档案
     */
    TeacherProfile getByUserId(Long userId);

    /**
     * 按条件分页查询已认证教师列表（家长端搜索老师）
     */
    IPage<TeacherProfile> pageTeachers(String district, String subject, Integer gender, int page, int size);

    /**
     * 分页查询待审核/全部教师列表（管理员端）
     */
    IPage<TeacherProfile> pageByAuthStatus(Integer authStatus, int page, int size);

    /**
     * 审核通过
     */
    void approve(Long teacherProfileId);

    /**
     * 审核拒绝
     */
    void reject(Long teacherProfileId, String reason);
}
