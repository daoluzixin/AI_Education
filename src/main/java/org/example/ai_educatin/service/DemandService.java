package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.entity.dto.demand.DemandCreateDTO;
import org.example.ai_educatin.entity.dto.demand.DemandQueryDTO;
import org.example.ai_educatin.entity.Demand;

import java.util.List;

public interface DemandService extends IService<Demand> {

    /**
     * 家长提交需求
     */
    Demand createDemand(Long userId, DemandCreateDTO dto);

    /**
     * 家长查看自己的需求列表
     */
    List<Demand> listByUserId(Long userId);

    /**
     * 后台分页查询需求列表
     */
    IPage<Demand> pageQuery(DemandQueryDTO dto);

    /**
     * 修改需求状态为 MATCHING
     */
    void startMatching(Long demandId);

    /**
     * 关闭需求
     */
    void closeDemand(Long demandId, String closeReason);
}
