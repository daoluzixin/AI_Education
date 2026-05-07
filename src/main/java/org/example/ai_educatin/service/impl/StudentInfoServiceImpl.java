package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.dto.student.StudentInfoDTO;
import org.example.ai_educatin.entity.StudentInfo;
import org.example.ai_educatin.mapper.StudentInfoMapper;
import org.example.ai_educatin.service.StudentInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentInfoServiceImpl extends ServiceImpl<StudentInfoMapper, StudentInfo>
        implements StudentInfoService {

    @Override
    public StudentInfo addStudent(Long parentId, StudentInfoDTO dto) {
        StudentInfo student = new StudentInfo();
        student.setParentId(parentId);
        student.setName(dto.getName());
        student.setGender(dto.getGender());
        student.setGrade(dto.getGrade());
        student.setSchool(dto.getSchool());
        student.setRemark(dto.getRemark());
        save(student);
        return student;
    }

    @Override
    public StudentInfo updateStudent(Long studentId, Long parentId, StudentInfoDTO dto) {
        StudentInfo student = getById(studentId);
        if (student == null || !student.getParentId().equals(parentId)) {
            throw new BusinessException(404, "学生信息不存在");
        }
        student.setName(dto.getName());
        student.setGender(dto.getGender());
        student.setGrade(dto.getGrade());
        student.setSchool(dto.getSchool());
        student.setRemark(dto.getRemark());
        updateById(student);
        return student;
    }

    @Override
    public List<StudentInfo> listByParentId(Long parentId) {
        return list(new LambdaQueryWrapper<StudentInfo>()
                .eq(StudentInfo::getParentId, parentId)
                .orderByDesc(StudentInfo::getCreateTime));
    }
}
