package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.entity.Recommendation;
import org.example.ai_educatin.entity.StudentProfile;
import org.example.ai_educatin.mapper.RecommendationMapper;
import org.example.ai_educatin.service.RecommendationService;
import org.example.ai_educatin.service.StudentProfileService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl extends ServiceImpl<RecommendationMapper, Recommendation>
        implements RecommendationService {

    private final StudentProfileService studentProfileService;

    @Override
    public List<StudentProfile> getRecommendedStudents(Long demandId) {
        List<Recommendation> recommendations = list(new LambdaQueryWrapper<Recommendation>()
                .eq(Recommendation::getDemandId, demandId)
                .orderByAsc(Recommendation::getSortOrder));

        List<StudentProfile> students = new ArrayList<>();
        for (Recommendation rec : recommendations) {
            StudentProfile profile = studentProfileService.getById(rec.getStudentId());
            if (profile != null) {
                students.add(profile);
            }
        }
        return students;
    }
}
