package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.entity.dto.student.StudentProfileDTO;
import org.example.ai_educatin.entity.dto.student.StudentQueryDTO;
import org.example.ai_educatin.entity.dto.student.SupplementDTO;
import org.example.ai_educatin.entity.StudentProfile;

public interface StudentProfileService extends IService<StudentProfile> {

    /**
     * 保存草稿
     */
    StudentProfile saveDraft(Long userId, StudentProfileDTO dto);

    /**
     * 提交审核
     */
    StudentProfile submitForReview(Long userId, StudentProfileDTO dto);

    /**
     * 获取当前用户的档案
     */
    StudentProfile getByUserId(Long userId);

    /**
     * 补充材料后重新提交
     */
    StudentProfile supplement(Long userId, SupplementDTO dto);

    /**
     * 后台分页查询学生列表
     */
    IPage<StudentProfile> pageQuery(StudentQueryDTO dto);
}
