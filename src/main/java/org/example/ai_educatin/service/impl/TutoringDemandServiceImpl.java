package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ai_educatin.common.enums.DemandStatus;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.dto.demand.TutoringDemandDTO;
import org.example.ai_educatin.entity.TutoringDemand;
import org.example.ai_educatin.mapper.TutoringDemandMapper;
import org.example.ai_educatin.service.TutoringDemandService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TutoringDemandServiceImpl extends ServiceImpl<TutoringDemandMapper, TutoringDemand>
        implements TutoringDemandService {

    @Override
    public TutoringDemand publish(Long parentId, TutoringDemandDTO dto) {
        TutoringDemand demand = new TutoringDemand();
        demand.setDemandNo(generateDemandNo());
        demand.setParentId(parentId);
        demand.setStudentId(dto.getStudentId());
        demand.setSubject(dto.getSubject());
        demand.setTeacherCount(dto.getTeacherCount());
        demand.setCurrentLevel(dto.getCurrentLevel());
        demand.setFrequency(dto.getFrequency());
        demand.setDurationHours(dto.getDurationHours());
        demand.setPreferWeekday(dto.getPreferWeekday());
        demand.setPreferTimeSlot(dto.getPreferTimeSlot());
        demand.setPricePerHour(dto.getPricePerHour());
        demand.setDistrict(dto.getDistrict());
        demand.setAddress(dto.getAddress());
        demand.setTeachMode(dto.getTeachMode());
        demand.setTeacherGenderReq(dto.getTeacherGenderReq());
        demand.setTeacherRequirement(dto.getTeacherRequirement());
        demand.setStatus(DemandStatus.PUBLISHED.getCode());
        save(demand);
        return demand;
    }

    @Override
    public IPage<TutoringDemand> pageByParent(Long parentId, int page, int size) {
        LambdaQueryWrapper<TutoringDemand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TutoringDemand::getParentId, parentId)
               .orderByDesc(TutoringDemand::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<TutoringDemand> pagePublished(String district, String subject,
                                                int page, int size) {
        LambdaQueryWrapper<TutoringDemand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TutoringDemand::getStatus, DemandStatus.PUBLISHED.getCode());
        if (district != null && !district.isEmpty()) {
            wrapper.eq(TutoringDemand::getDistrict, district);
        }
        if (subject != null && !subject.isEmpty()) {
            wrapper.apply("FIND_IN_SET({0}, subject) > 0", subject);
        }
        wrapper.orderByDesc(TutoringDemand::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public void closeDemand(Long demandId, Long parentId) {
        TutoringDemand demand = getById(demandId);
        if (demand == null || !demand.getParentId().equals(parentId)) {
            throw new BusinessException(404, "需求不存在");
        }
        if (demand.getStatus().equals(DemandStatus.CLOSED.getCode())) {
            throw new BusinessException("需求已关闭");
        }
        demand.setStatus(DemandStatus.CLOSED.getCode());
        updateById(demand);
    }

    /**
     * 生成需求编号: 年月日 + 4位随机数
     */
    private String generateDemandNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return date + random;
    }
}
