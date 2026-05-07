package org.example.ai_educatin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.ai_educatin.dto.parent.ParentProfileDTO;
import org.example.ai_educatin.entity.ParentProfile;

public interface ParentProfileService extends IService<ParentProfile> {

    /**
     * 提交/更新家长档案
     */
    ParentProfile saveOrUpdateProfile(Long userId, ParentProfileDTO dto);

    /**
     * 根据userId获取家长档案
     */
    ParentProfile getByUserId(Long userId);
}
