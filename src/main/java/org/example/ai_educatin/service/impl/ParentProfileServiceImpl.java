package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.ai_educatin.dto.parent.ParentProfileDTO;
import org.example.ai_educatin.entity.ParentProfile;
import org.example.ai_educatin.mapper.ParentProfileMapper;
import org.example.ai_educatin.service.ParentProfileService;
import org.springframework.stereotype.Service;

@Service
public class ParentProfileServiceImpl extends ServiceImpl<ParentProfileMapper, ParentProfile>
        implements ParentProfileService {

    @Override
    public ParentProfile saveOrUpdateProfile(Long userId, ParentProfileDTO dto) {
        ParentProfile profile = getByUserId(userId);
        if (profile == null) {
            profile = new ParentProfile();
            profile.setUserId(userId);
        }
        profile.setRealName(dto.getRealName());
        profile.setPhone(dto.getPhone());
        profile.setDistrict(dto.getDistrict());
        profile.setAddress(dto.getAddress());

        saveOrUpdate(profile);
        return profile;
    }

    @Override
    public ParentProfile getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<ParentProfile>()
                .eq(ParentProfile::getUserId, userId));
    }
}
