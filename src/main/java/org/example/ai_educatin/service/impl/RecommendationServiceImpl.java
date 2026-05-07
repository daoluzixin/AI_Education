package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.enums.DemandStatus;
import org.example.ai_educatin.common.enums.ReviewStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.dto.admin.RecommendDTO;
import org.example.ai_educatin.entity.Demand;
import org.example.ai_educatin.entity.Recommendation;
import org.example.ai_educatin.entity.StudentProfile;
import org.example.ai_educatin.mapper.RecommendationMapper;
import org.example.ai_educatin.service.DemandService;
import org.example.ai_educatin.service.RecommendationService;
import org.example.ai_educatin.service.StudentProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl extends ServiceImpl<RecommendationMapper, Recommendation>
        implements RecommendationService {

    private final DemandService demandService;
    private final StudentProfileService studentProfileService;

    /** 每条需求最多推荐5位学生 */
    private static final int MAX_RECOMMEND_COUNT = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configureRecommendation(RecommendDTO dto, Long operatorId) {
        Long demandId = dto.getDemandId();

        // 校验需求存在且状态为 MATCHING
        Demand demand = demandService.getById(demandId);
        if (demand == null) {
            throw new BusinessException(404, "需求不存在");
        }
        DemandStatus demandStatus = DemandStatus.fromCode(demand.getStatus());
        if (demandStatus != DemandStatus.MATCHING && demandStatus != DemandStatus.RECOMMENDED) {
            throw new BusinessException(400, "需求状态不允许配置推荐，当前状态: " + demandStatus.getDesc());
        }

        // 校验推荐数量上限
        List<Long> studentIds = dto.getStudentIds();
        if (studentIds.size() > MAX_RECOMMEND_COUNT) {
            throw new BusinessException(400, "每条需求最多推荐" + MAX_RECOMMEND_COUNT + "位学生");
        }

        // 校验所有学生都已审核通过
        for (Long studentId : studentIds) {
            StudentProfile profile = studentProfileService.getById(studentId);
            if (profile == null) {
                throw new BusinessException(404, "学生档案不存在, id=" + studentId);
            }
            if (!profile.getReviewStatus().equals(ReviewStatus.APPROVED.getCode())) {
                throw new BusinessException(400, "学生 " + profile.getRealName() + " 未审核通过，不可推荐");
            }
        }

        // 删除原有推荐记录
        remove(new LambdaQueryWrapper<Recommendation>()
                .eq(Recommendation::getDemandId, demandId));

        // 写入新推荐记录
        for (int i = 0; i < studentIds.size(); i++) {
            Recommendation rec = new Recommendation();
            rec.setDemandId(demandId);
            rec.setStudentId(studentIds.get(i));
            rec.setSortOrder(i + 1);
            rec.setOperatorId(operatorId);
            save(rec);
        }

        // 更新需求状态为 RECOMMENDED
        demand.setStatus(DemandStatus.RECOMMENDED.getCode());
        demandService.updateById(demand);
    }

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

    @Override
    public void removeRecommendation(Long demandId, Long studentId) {
        remove(new LambdaQueryWrapper<Recommendation>()
                .eq(Recommendation::getDemandId, demandId)
                .eq(Recommendation::getStudentId, studentId));
    }
}
