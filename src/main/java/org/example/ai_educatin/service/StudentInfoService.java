package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.dto.student.StudentInfoDTO;
import org.example.ai_educatin.entity.StudentInfo;

import java.util.List;

public interface StudentInfoService extends IService<StudentInfo> {

    /**
     * 新增学生信息
     */
    StudentInfo addStudent(Long parentId, StudentInfoDTO dto);

    /**
     * 更新学生信息
     */
    StudentInfo updateStudent(Long studentId, Long parentId, StudentInfoDTO dto);

    /**
     * 获取某家长下的所有学生
     */
    List<StudentInfo> listByParentId(Long parentId);
}
