package org.example.ai_educatin.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.ai_educatin.common.enums.DemandStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.dto.demand.DemandCreateDTO;
import org.example.ai_educatin.entity.dto.demand.DemandQueryDTO;
import org.example.ai_educatin.entity.Demand;
import org.example.ai_educatin.mapper.DemandMapper;
import org.example.ai_educatin.service.DemandService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemandServiceImpl extends ServiceImpl<DemandMapper, Demand> implements DemandService {

    private final StringRedisTemplate redisTemplate;

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

        wrapper.orderByDesc(Demand::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public void startMatching(Long demandId) {
        Demand demand = getById(demandId);
        if (demand == null) {
            throw new BusinessException(404, "需求不存在");
        }

        // 状态校验: 只有 PENDING 可以转为 MATCHING
        DemandStatus current = DemandStatus.fromCode(demand.getStatus());
        if (current != DemandStatus.PENDING) {
            throw new BusinessException(400, "当前状态不允许开始匹配，当前状态: " + current.getDesc());
        }

        demand.setStatus(DemandStatus.MATCHING.getCode());
        updateById(demand);
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
