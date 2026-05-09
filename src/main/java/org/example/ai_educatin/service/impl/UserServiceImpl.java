package org.example.ai_educatin.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ai_educatin.common.enums.UserRole;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.dto.user.AdminLoginDTO;
import org.example.ai_educatin.entity.dto.user.ParentLoginDTO;
import org.example.ai_educatin.entity.dto.user.StudentLoginDTO;
import org.example.ai_educatin.entity.User;
import org.example.ai_educatin.mapper.UserMapper;
import org.example.ai_educatin.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String VERIFY_CODE_PREFIX = "verify:code:";
    private static final String VERIFY_LIMIT_PREFIX = "verify:limit:";
    private static final String VERIFY_ERROR_COUNT_PREFIX = "verify:error:";
    private static final String VERIFY_LOCK_PREFIX = "verify:lock:";
    private static final String ADMIN_ERROR_COUNT_PREFIX = "admin:error:";
    private static final String ADMIN_LOCK_PREFIX = "admin:lock:";
    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final long SEND_INTERVAL_SECONDS = 60;
    private static final int MAX_VERIFY_ERROR_COUNT = 5;
    private static final long VERIFY_LOCK_MINUTES = 15;
    private static final int MAX_ADMIN_ERROR_COUNT = 5;
    private static final long ADMIN_LOCK_MINUTES = 30;

    /** 用户状态: 正常 */
    private static final int USER_STATUS_ACTIVE = 1;
    /** 用户状态: 禁用 */
    private static final int USER_STATUS_DISABLED = 0;

    @Override
    public User parentLogin(ParentLoginDTO dto) {
        // 校验验证码
        verifyCode(dto.getPhone(), dto.getVerifyCode());

        // 查找或创建用户
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone())
                .eq(User::getRole, UserRole.PARENT.getCode()));

        if (user == null) {
            // 首次登录自动注册
            user = new User();
            user.setPhone(dto.getPhone());
            user.setRole(UserRole.PARENT.getCode());
            user.setNickname(dto.getNickname() != null ? dto.getNickname() : "家长" + dto.getPhone().substring(7));
            user.setStatus(USER_STATUS_ACTIVE);
            save(user);
        }

        checkUserStatus(user);
        return user;
    }

    @Override
    public User studentLogin(StudentLoginDTO dto) {
        // 校验教育邮箱后缀
        if (!dto.getEmail().endsWith(".edu.cn")) {
            throw new BusinessException(400, "教育邮箱必须以 .edu.cn 结尾");
        }

        // 校验验证码
        verifyCode(dto.getPhone(), dto.getVerifyCode());

        // 查找或创建用户
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone())
                .eq(User::getRole, UserRole.STUDENT.getCode()));

        if (user == null) {
            // 检查该邮箱是否已被其他手机号绑定
            User existEmail = getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, dto.getEmail()));
            if (existEmail != null) {
                throw new BusinessException(400, "该教育邮箱已被其他账号绑定");
            }

            // 首次登录自动注册
            user = new User();
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            user.setRole(UserRole.STUDENT.getCode());
            user.setStatus(USER_STATUS_ACTIVE);
            save(user);
        }

        checkUserStatus(user);
        return user;
    }

    @Override
    public User adminLogin(AdminLoginDTO dto) {
        // 检查是否被锁定
        String lockKey = ADMIN_LOCK_PREFIX + dto.getPhone();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new BusinessException(403, "账号已被锁定，请" + ADMIN_LOCK_MINUTES + "分钟后重试");
        }

        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone())
                .eq(User::getRole, UserRole.ADMIN.getCode()));

        if (user == null) {
            incrementAdminErrorCount(dto.getPhone());
            throw new BusinessException(401, "账号或密码错误");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            incrementAdminErrorCount(dto.getPhone());
            throw new BusinessException(401, "账号或密码错误");
        }

        // 登录成功，清除错误计数
        redisTemplate.delete(ADMIN_ERROR_COUNT_PREFIX + dto.getPhone());

        checkUserStatus(user);
        return user;
    }

    @Override
    public void sendVerifyCode(String phone) {
        // 检查发送频率：60秒内不可重发
        String limitKey = VERIFY_LIMIT_PREFIX + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw new BusinessException(400, "验证码发送过于频繁，请60秒后重试");
        }

        // 生成6位验证码
        String code = RandomUtil.randomNumbers(6);

        // 存入 Redis，5分钟过期
        String codeKey = VERIFY_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置发送间隔限制
        redisTemplate.opsForValue().set(limitKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // TODO: 接入真实短信发送服务（当前仅日志输出）
        log.info("【博文学堂】验证码: {}，手机号: {}，有效期{}分钟", code, phone, CODE_EXPIRE_MINUTES);
    }

    /**
     * 校验验证码（含错误次数限制：连续输错5次锁定15分钟）
     */
    private void verifyCode(String phone, String inputCode) {
        // 检查是否被锁定
        String lockKey = VERIFY_LOCK_PREFIX + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new BusinessException(403, "验证码错误次数过多，请" + VERIFY_LOCK_MINUTES + "分钟后重试");
        }

        String codeKey = VERIFY_CODE_PREFIX + phone;
        String cachedCode = redisTemplate.opsForValue().get(codeKey);

        if (cachedCode == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }

        if (!cachedCode.equals(inputCode)) {
            // 递增错误计数
            String errorKey = VERIFY_ERROR_COUNT_PREFIX + phone;
            Long errorCount = redisTemplate.opsForValue().increment(errorKey);
            if (errorCount != null && errorCount == 1L) {
                // 首次错误，设置过期时间与验证码有效期一致
                redisTemplate.expire(errorKey, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }
            if (errorCount != null && errorCount >= MAX_VERIFY_ERROR_COUNT) {
                // 达到上限，锁定并清除错误计数和验证码
                redisTemplate.opsForValue().set(lockKey, "1", VERIFY_LOCK_MINUTES, TimeUnit.MINUTES);
                redisTemplate.delete(errorKey);
                redisTemplate.delete(codeKey);
                throw new BusinessException(403, "验证码错误次数过多，请" + VERIFY_LOCK_MINUTES + "分钟后重试");
            }
            throw new BusinessException(400, "验证码错误，还剩" + (MAX_VERIFY_ERROR_COUNT - errorCount) + "次机会");
        }

        // 验证成功，清除错误计数和验证码
        redisTemplate.delete(VERIFY_ERROR_COUNT_PREFIX + phone);
        redisTemplate.delete(codeKey);
    }

    /**
     * 递增管理员登录错误次数（达到上限则锁定30分钟）
     */
    private void incrementAdminErrorCount(String phone) {
        String errorKey = ADMIN_ERROR_COUNT_PREFIX + phone;
        Long errorCount = redisTemplate.opsForValue().increment(errorKey);
        if (errorCount != null && errorCount == 1L) {
            // 首次错误，设置30分钟窗口期
            redisTemplate.expire(errorKey, ADMIN_LOCK_MINUTES, TimeUnit.MINUTES);
        }
        if (errorCount != null && errorCount >= MAX_ADMIN_ERROR_COUNT) {
            // 达到上限，锁定账号
            String lockKey = ADMIN_LOCK_PREFIX + phone;
            redisTemplate.opsForValue().set(lockKey, "1", ADMIN_LOCK_MINUTES, TimeUnit.MINUTES);
            redisTemplate.delete(errorKey);
            throw new BusinessException(403, "连续输错密码" + MAX_ADMIN_ERROR_COUNT + "次，账号已锁定" + ADMIN_LOCK_MINUTES + "分钟");
        }
    }

    /**
     * 检查用户状态
     */
    private void checkUserStatus(User user) {
        if (user.getStatus() != null && user.getStatus() == USER_STATUS_DISABLED) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }
    }
}
