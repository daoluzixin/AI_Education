package org.example.ai_educatin.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_educatin.common.enums.DemandStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.Recommendation;
import org.example.ai_educatin.entity.dto.demand.DemandCreateDTO;
import org.example.ai_educatin.entity.dto.demand.DemandQueryDTO;
import org.example.ai_educatin.entity.Demand;
import org.example.ai_educatin.mapper.DemandMapper;
import org.example.ai_educatin.mapper.RecommendationMapper;
import org.example.ai_educatin.service.DemandService;
import org.example.ai_educatin.service.MatchingService;
import org.example.ai_educatin.vo.CandidateVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemandServiceImpl extends ServiceImpl<DemandMapper, Demand> implements DemandService {

    private final StringRedisTemplate redisTemplate;
    private final MatchingService matchingService;
    private final RecommendationMapper recommendationMapper;

    /** 自动推荐的最大学生数 */
    private static final int AUTO_RECOMMEND_LIMIT = 5;

    @Override
    public Demand createDemand(Long userId, DemandCreateDTO dto) {
        Demand demand = new Demand();
        demand.setDemandNo(generateDemandNo());
        demand.setUserId(userId);
        demand.setChildGrade(dto.getChildGrade());
        demand.setDemandType(dto.getDemandType());
        demand.setCity(dto.getCity());
        demand.setExpectations(dto.getExpectations());
        demand.setBudget(dto.getBudget());
        demand.setRemark(dto.getRemark());
        demand.setStatus(DemandStatus.PENDING.getCode());
        save(demand);
        return demand;
    }

    @Override
    public List<Demand> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<Demand>()
                .eq(Demand::getUserId, userId)
                .orderByDesc(Demand::getCreateTime));
    }

    @Override
    public IPage<Demand> pageQuery(DemandQueryDTO dto) {
        Page<Demand> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Demand> wrapper = new LambdaQueryWrapper<>();

        if (dto.getStatus() != null) {
            wrapper.eq(Demand::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getDemandType())) {
            wrapper.eq(Demand::getDemandType, dto.getDemandType());
        }
        if (StringUtils.hasText(dto.getCity())) {
            wrapper.eq(Demand::getCity, dto.getCity());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Demand::getDemandNo, dto.getKeyword())
                    .or().like(Demand::getExpectations, dto.getKeyword()));
        }
        if (dto.getStartTime() != null) {
            wrapper.ge(Demand::getCreateTime, dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            wrapper.le(Demand::getCreateTime, dto.getEndTime());
        }

        wrapper.orderByDesc(Demand::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startMatching(Long demandId) {
        Demand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException(404, "需求不存在");
        }

        // 状态校验: PENDING 或 MATCHING（无候选时重试）可以开始匹配
        DemandStatus current = DemandStatus.fromCode(demand.getStatus());
        if (current != DemandStatus.PENDING && current != DemandStatus.MATCHING) {
            throw new BusinessException(400, "当前状态不允许开始匹配，当前状态: " + current.getDesc());
        }

        // 先置为 MATCHING（瞬时态）
        demand.setStatus(DemandStatus.MATCHING.getCode());
        updateById(demand);

        // 自动调用匹配引擎，取 Top N 候选
        List<CandidateVO> candidates = matchingService.findCandidates(demandId, AUTO_RECOMMEND_LIMIT);

        if (candidates.isEmpty()) {
            log.info("需求[{}]自动匹配无候选学生，状态保持 MATCHING 等待后续重试", demand.getDemandNo());
            return;
        }

        // 清除旧推荐记录（幂等）
        recommendationMapper.delete(
                new LambdaQueryWrapper<Recommendation>()
                        .eq(Recommendation::getDemandId, demandId));

        // 写入推荐记录
        for (int i = 0; i < candidates.size(); i++) {
            Recommendation rec = new Recommendation();
            rec.setDemandId(demandId);
            rec.setStudentId(candidates.get(i).getStudentId());
            rec.setSortOrder(i + 1);
            rec.setOperatorId(0L); // 0 表示系统自动推荐
            recommendationMapper.insert(rec);
        }

        // 状态推进到 RECOMMENDED
        demand.setStatus(DemandStatus.RECOMMENDED.getCode());
        updateById(demand);

        log.info("需求[{}]自动匹配完成，推荐{}位学生", demand.getDemandNo(), candidates.size());
    }

    @Override
    public void closeDemand(Long demandId, String closeReason) {
        Demand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException(404, "需求不存在");
        }

        // 任何状态都可以关闭（状态机规则：任意状态 → CLOSED）
        DemandStatus current = DemandStatus.fromCode(demand.getStatus());
        if (current == DemandStatus.CLOSED) {
            throw new BusinessException(400, "需求已关闭");
        }

        demand.setStatus(DemandStatus.CLOSED.getCode());
        demand.setCloseReason(closeReason);
        updateById(demand);
    }

    /**
     * 生成需求编号: REQ + yyyyMMdd + 4位流水号
     */
    private String generateDemandNo() {
        String today = DateUtil.format(DateUtil.date(), "yyyyMMdd");
        String key = "seq:REQ:" + today;
        Long seq = redisTemplate.opsForValue().increment(key);
        if (seq != null && seq == 1L) {
            redisTemplate.expireAt(key, java.util.Date.from(
                    LocalDate.now().plusDays(1).atStartOfDay()
                            .atZone(java.time.ZoneId.systemDefault()).toInstant()));
        }
        return String.format("REQ%s%04d", today, seq);
    }
}
