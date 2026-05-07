package org.example.ai_educatin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.result.Result;
import org.example.ai_educatin.dto.student.StudentInfoDTO;
import org.example.ai_educatin.entity.StudentInfo;
import org.example.ai_educatin.service.StudentInfoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentInfoController {

    private final StudentInfoService studentInfoService;

    /**
     * 新增学生（家长添加孩子信息）
     * TODO: parentId 后续从JWT Token解析家长档案ID
     */
    @PostMapping
    public Result<StudentInfo> addStudent(@RequestParam Long parentId,
                                          @Valid @RequestBody StudentInfoDTO dto) {
        return Result.success(studentInfoService.addStudent(parentId, dto));
    }

    /**
     * 更新学生信息
     */
    @PutMapping("/{id}")
    public Result<StudentInfo> updateStudent(@PathVariable Long id,
                                             @RequestParam Long parentId,
                                             @Valid @RequestBody StudentInfoDTO dto) {
        return Result.success(studentInfoService.updateStudent(id, parentId, dto));
    }

    /**
     * 获取某家长下的所有学生
     */
    @GetMapping("/list")
    public Result<List<StudentInfo>> listStudents(@RequestParam Long parentId) {
        return Result.success(studentInfoService.listByParentId(parentId));
    }
}
