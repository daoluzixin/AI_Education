package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.dto.demand.TutoringDemandDTO;
import org.example.ai_educatin.entity.TutoringDemand;

public interface TutoringDemandService extends IService<TutoringDemand> {

    /**
     * 发布辅导需求
     */
    TutoringDemand publish(Long parentId, TutoringDemandDTO dto);

    /**
     * 家长查看自己的需求列表
     */
    IPage<TutoringDemand> pageByParent(Long parentId, int page, int size);

    /**
     * 老师端浏览需求列表(按区域/科目筛选)
     */
    IPage<TutoringDemand> pagePublished(String district, String subject, int page, int size);

    /**
     * 关闭需求
     */
    void closeDemand(Long demandId, Long parentId);
}
