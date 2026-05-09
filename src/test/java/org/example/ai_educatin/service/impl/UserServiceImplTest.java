package org.example.ai_educatin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.ai_educatin.common.enums.UserRole;
import org.example.ai_educatin.common.exception.BusinessException;
import org.example.ai_educatin.entity.User;
import org.example.ai_educatin.entity.dto.user.AdminLoginDTO;
import org.example.ai_educatin.entity.dto.user.ParentLoginDTO;
import org.example.ai_educatin.entity.dto.user.StudentLoginDTO;
import org.example.ai_educatin.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl 单元测试
 * 覆盖 parentLogin / studentLogin / adminLogin / sendVerifyCode 全部对抗性用例
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 单元测试")
class UserServiceImplTest {

    @Spy
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    @SuppressWarnings("unused")
    private UserMapper userMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private static final String VERIFY_CODE_PREFIX = "verify:code:";
    private static final String VERIFY_LIMIT_PREFIX = "verify:limit:";
    private static final String VERIFY_ERROR_COUNT_PREFIX = "verify:error:";
    private static final String VERIFY_LOCK_PREFIX = "verify:lock:";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 默认：验证码锁定 key 不存在
        lenient().when(redisTemplate.hasKey(anyString())).thenReturn(false);
        // 默认：错误计数递增返回 1（首次错误）
        lenient().when(valueOperations.increment(anyString())).thenReturn(1L);
        lenient().when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
    }

    // ==================== Helper Methods ====================

    private void mockVerifyCodeSuccess(String phone, String code) {
        when(valueOperations.get(VERIFY_CODE_PREFIX + phone)).thenReturn(code);
    }

    private ParentLoginDTO buildParentLoginDTO(String phone, String code, String nickname) {
        ParentLoginDTO dto = new ParentLoginDTO();
        dto.setPhone(phone);
        dto.setVerifyCode(code);
        dto.setNickname(nickname);
        return dto;
    }

    private StudentLoginDTO buildStudentLoginDTO(String phone, String email, String code) {
        StudentLoginDTO dto = new StudentLoginDTO();
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setVerifyCode(code);
        return dto;
    }

    private AdminLoginDTO buildAdminLoginDTO(String phone, String password) {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setPhone(phone);
        dto.setPassword(password);
        return dto;
    }

    private User buildUser(String phone, Integer role, Integer status) {
        User user = new User();
        user.setId(1L);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    // ==================== parentLogin Tests ====================

    @Nested
    @DisplayName("parentLogin - 家长登录/注册")
    class ParentLoginTests {

        @Test
        @DisplayName("U-P-001: 首次登录自动创建 User(role=PARENT, status=1)")
        void shouldCreateNewParentOnFirstLogin() {
            String phone = "13800001111";
            String code = "123456";
            mockVerifyCodeSuccess(phone, code);
            doReturn(null).when(userService).getOne(any(LambdaQueryWrapper.class));
            doReturn(true).when(userService).save(any(User.class));

            ParentLoginDTO dto = buildParentLoginDTO(phone, code, "张妈妈");
            User result = userService.parentLogin(dto);

            assertThat(result).isNotNull();
            assertThat(result.getRole()).isEqualTo(UserRole.PARENT.getCode());
            assertThat(result.getStatus()).isEqualTo(1);
            assertThat(result.getPhone()).isEqualTo(phone);
            verify(userService).save(any(User.class));
        }

        @Test
        @DisplayName("U-P-002: 已存在家长直接返回，不重新创建")
        void shouldReturnExistingParent() {
            String phone = "13800002222";
            String code = "654321";
            mockVerifyCodeSuccess(phone, code);
            User existing = buildUser(phone, UserRole.PARENT.getCode(), 1);
            existing.setNickname("已有家长");
            doReturn(existing).when(userService).getOne(any(LambdaQueryWrapper.class));

            ParentLoginDTO dto = buildParentLoginDTO(phone, code, null);
            User result = userService.parentLogin(dto);

            assertThat(result).isSameAs(existing);
            assertThat(result.getNickname()).isEqualTo("已有家长");
        }

        @Test
        @DisplayName("U-P-003: nickname='张妈妈' 正确设置")
        void shouldSetNicknameCorrectly() {
            String phone = "13800003333";
            String code = "111111";
            mockVerifyCodeSuccess(phone, code);
            doReturn(null).when(userService).getOne(any(LambdaQueryWrapper.class));
            doReturn(true).when(userService).save(any(User.class));

            ParentLoginDTO dto = buildParentLoginDTO(phone, code, "张妈妈");
            User result = userService.parentLogin(dto);

            assertThat(result.getNickname()).isEqualTo("张妈妈");
        }

        @Test
        @DisplayName("U-P-004: nickname=null 时自动生成 '家长' + 手机号后4位")
        void shouldAutoGenerateNicknameWhenNull() {
            String phone = "13800004444";
            String code = "222222";
            mockVerifyCodeSuccess(phone, code);
            doReturn(null).when(userService).getOne(any(LambdaQueryWrapper.class));
            doReturn(true).when(userService).save(any(User.class));

            ParentLoginDTO dto = buildParentLoginDTO(phone, code, null);
            User result = userService.parentLogin(dto);

            // phone.substring(7) = "4444"
            assertThat(result.getNickname()).isEqualTo("家长4444");
        }

        @Test
        @DisplayName("U-P-005: 验证码错误 → 400 '验证码错误，还剩N次机会'")
        void shouldThrowWhenWrongVerifyCode() {
            String phone = "13800005555";
            when(valueOperations.get(VERIFY_CODE_PREFIX + phone)).thenReturn("999999");
            when(valueOperations.increment(VERIFY_ERROR_COUNT_PREFIX + phone)).thenReturn(1L);

            ParentLoginDTO dto = buildParentLoginDTO(phone, "000000", null);

            assertThatThrownBy(() -> userService.parentLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).contains("验证码错误");
                        assertThat(bex.getMessage()).contains("次机会");
                    });
        }

        @Test
        @DisplayName("U-P-006: Redis 中无验证码 → 400 '验证码已过期'")
        void shouldThrowWhenCodeExpired() {
            String phone = "13800006666";
            when(valueOperations.get(VERIFY_CODE_PREFIX + phone)).thenReturn(null);

            ParentLoginDTO dto = buildParentLoginDTO(phone, "123456", null);

            assertThatThrownBy(() -> userService.parentLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).contains("验证码已过期");
                    });
        }

        @Test
        @DisplayName("U-P-007: 用户 status=0 → 403 '账号已被禁用'")
        void shouldThrowWhenUserDisabled() {
            String phone = "13800007777";
            String code = "333333";
            mockVerifyCodeSuccess(phone, code);
            User disabled = buildUser(phone, UserRole.PARENT.getCode(), 0);
            doReturn(disabled).when(userService).getOne(any(LambdaQueryWrapper.class));

            ParentLoginDTO dto = buildParentLoginDTO(phone, code, null);

            assertThatThrownBy(() -> userService.parentLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(403);
                        assertThat(bex.getMessage()).contains("账号已被禁用");
                    });
        }

        @Test
        @DisplayName("U-P-008: 同手机号 STUDENT 角色不干扰 PARENT 查询")
        void shouldNotInterfereWithStudentRole() {
            String phone = "13800008888";
            String code = "444444";
            mockVerifyCodeSuccess(phone, code);
            // getOne with PARENT role returns null (no parent exists)
            doReturn(null).when(userService).getOne(any(LambdaQueryWrapper.class));
            doReturn(true).when(userService).save(any(User.class));

            ParentLoginDTO dto = buildParentLoginDTO(phone, code, "新家长");
            User result = userService.parentLogin(dto);

            // Should create a new PARENT user even though a STUDENT with same phone may exist
            assertThat(result.getRole()).isEqualTo(UserRole.PARENT.getCode());
            assertThat(result.getPhone()).isEqualTo(phone);
        }
    }

    // ==================== studentLogin Tests ====================

    @Nested
    @DisplayName("studentLogin - 学生登录/注册")
    class StudentLoginTests {

        @Test
        @DisplayName("U-S-001: 正常 .edu.cn 邮箱 → 创建 User(role=STUDENT)")
        void shouldCreateStudentWithEduEmail() {
            String phone = "13900001111";
            String email = "zhangsan@pku.edu.cn";
            String code = "123456";
            mockVerifyCodeSuccess(phone, code);
            // First getOne (by phone+STUDENT) returns null
            // Second getOne (by email) returns null
            doReturn(null).when(userService).getOne(any(LambdaQueryWrapper.class));
            doReturn(true).when(userService).save(any(User.class));

            StudentLoginDTO dto = buildStudentLoginDTO(phone, email, code);
            User result = userService.studentLogin(dto);

            assertThat(result).isNotNull();
            assertThat(result.getRole()).isEqualTo(UserRole.STUDENT.getCode());
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("U-S-002: 已存在学生直接返回")
        void shouldReturnExistingStudent() {
            String phone = "13900002222";
            String email = "lisi@tsinghua.edu.cn";
            String code = "654321";
            mockVerifyCodeSuccess(phone, code);
            User existing = buildUser(phone, UserRole.STUDENT.getCode(), 1);
            existing.setEmail(email);
            doReturn(existing).when(userService).getOne(any(LambdaQueryWrapper.class));

            StudentLoginDTO dto = buildStudentLoginDTO(phone, email, code);
            User result = userService.studentLogin(dto);

            assertThat(result).isSameAs(existing);
        }

        @Test
        @DisplayName("U-S-003: 非 .edu.cn 邮箱 → 400 '教育邮箱必须以 .edu.cn 结尾'")
        void shouldRejectNonEduEmail() {
            String phone = "13900003333";
            String email = "test@gmail.com";
            String code = "111111";

            StudentLoginDTO dto = buildStudentLoginDTO(phone, email, code);

            assertThatThrownBy(() -> userService.studentLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).isEqualTo("教育邮箱必须以 .edu.cn 结尾");
                    });
        }

        @Test
        @DisplayName("U-S-004: 邮箱已被其他手机号绑定 → 400 '该教育邮箱已被其他账号绑定'")
        void shouldRejectEmailBoundToAnotherPhone() {
            String phone = "13900004444";
            String email = "bound@pku.edu.cn";
            String code = "222222";
            mockVerifyCodeSuccess(phone, code);

            // First getOne (by phone+STUDENT) returns null → new user flow
            // Second getOne (by email) returns existing user with different phone
            User emailOwner = buildUser("13900009999", UserRole.STUDENT.getCode(), 1);
            emailOwner.setEmail(email);
            doReturn(null).doReturn(emailOwner).when(userService).getOne(any(LambdaQueryWrapper.class));

            StudentLoginDTO dto = buildStudentLoginDTO(phone, email, code);

            assertThatThrownBy(() -> userService.studentLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).isEqualTo("该教育邮箱已被其他账号绑定");
                    });
        }

        @Test
        @DisplayName("U-S-005: 验证码错误 → 400 '验证码错误，还剩N次机会'")
        void shouldThrowWhenWrongVerifyCode() {
            String phone = "13900005555";
            String email = "test@pku.edu.cn";
            when(valueOperations.get(VERIFY_CODE_PREFIX + phone)).thenReturn("999999");
            when(valueOperations.increment(VERIFY_ERROR_COUNT_PREFIX + phone)).thenReturn(1L);

            StudentLoginDTO dto = buildStudentLoginDTO(phone, email, "000000");

            assertThatThrownBy(() -> userService.studentLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).contains("验证码错误");
                        assertThat(bex.getMessage()).contains("次机会");
                    });
        }

        @Test
        @DisplayName("U-S-006: 用户 status=0 → 403 '账号已被禁用'")
        void shouldThrowWhenStudentDisabled() {
            String phone = "13900006666";
            String email = "disabled@pku.edu.cn";
            String code = "333333";
            mockVerifyCodeSuccess(phone, code);
            User disabled = buildUser(phone, UserRole.STUDENT.getCode(), 0);
            disabled.setEmail(email);
            doReturn(disabled).when(userService).getOne(any(LambdaQueryWrapper.class));

            StudentLoginDTO dto = buildStudentLoginDTO(phone, email, code);

            assertThatThrownBy(() -> userService.studentLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(403);
                        assertThat(bex.getMessage()).contains("账号已被禁用");
                    });
        }

        @Test
        @DisplayName("U-S-007: 短邮箱 'a@b.edu.cn' 通过校验")
        void shouldAcceptShortEduEmail() {
            String phone = "13900007777";
            String email = "a@b.edu.cn";
            String code = "444444";
            mockVerifyCodeSuccess(phone, code);
            doReturn(null).when(userService).getOne(any(LambdaQueryWrapper.class));
            doReturn(true).when(userService).save(any(User.class));

            StudentLoginDTO dto = buildStudentLoginDTO(phone, email, code);
            User result = userService.studentLogin(dto);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getRole()).isEqualTo(UserRole.STUDENT.getCode());
        }

        @Test
        @DisplayName("U-S-008: 特殊字符邮箱 'test+1@pku.edu.cn' 通过校验")
        void shouldAcceptEmailWithSpecialChars() {
            String phone = "13900008888";
            String email = "test+1@pku.edu.cn";
            String code = "555555";
            mockVerifyCodeSuccess(phone, code);
            doReturn(null).when(userService).getOne(any(LambdaQueryWrapper.class));
            doReturn(true).when(userService).save(any(User.class));

            StudentLoginDTO dto = buildStudentLoginDTO(phone, email, code);
            User result = userService.studentLogin(dto);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getRole()).isEqualTo(UserRole.STUDENT.getCode());
        }
    }

    // ==================== adminLogin Tests ====================

    @Nested
    @DisplayName("adminLogin - 管理员登录")
    class AdminLoginTests {

        @Test
        @DisplayName("U-A-001: 正确密码 → 返回 User(role=ADMIN)")
        void shouldReturnAdminOnCorrectPassword() {
            String phone = "13700001111";
            String rawPassword = "Admin@123";
            String encodedPassword = "$2a$10$encodedHash";
            User admin = buildUser(phone, UserRole.ADMIN.getCode(), 1);
            admin.setPassword(encodedPassword);
            doReturn(admin).when(userService).getOne(any(LambdaQueryWrapper.class));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

            AdminLoginDTO dto = buildAdminLoginDTO(phone, rawPassword);
            User result = userService.adminLogin(dto);

            assertThat(result).isSameAs(admin);
            assertThat(result.getRole()).isEqualTo(UserRole.ADMIN.getCode());
        }

        @Test
        @DisplayName("U-A-002: 账号不存在 → 401 '账号或密码错误'")
        void shouldThrowWhenAccountNotFound() {
            String phone = "13700002222";
            doReturn(null).when(userService).getOne(any(LambdaQueryWrapper.class));

            AdminLoginDTO dto = buildAdminLoginDTO(phone, "anyPassword");

            assertThatThrownBy(() -> userService.adminLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(401);
                        assertThat(bex.getMessage()).isEqualTo("账号或密码错误");
                    });
        }

        @Test
        @DisplayName("U-A-003: 密码错误 → 401 '账号或密码错误'")
        void shouldThrowWhenWrongPassword() {
            String phone = "13700003333";
            String encodedPassword = "$2a$10$encodedHash";
            User admin = buildUser(phone, UserRole.ADMIN.getCode(), 1);
            admin.setPassword(encodedPassword);
            doReturn(admin).when(userService).getOne(any(LambdaQueryWrapper.class));
            when(passwordEncoder.matches("wrongPassword", encodedPassword)).thenReturn(false);

            AdminLoginDTO dto = buildAdminLoginDTO(phone, "wrongPassword");

            assertThatThrownBy(() -> userService.adminLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(401);
                        assertThat(bex.getMessage()).isEqualTo("账号或密码错误");
                    });
        }

        @Test
        @DisplayName("U-A-004: 用户 status=0 → 403 '账号已被禁用'")
        void shouldThrowWhenAdminDisabled() {
            String phone = "13700004444";
            String rawPassword = "Admin@123";
            String encodedPassword = "$2a$10$encodedHash";
            User admin = buildUser(phone, UserRole.ADMIN.getCode(), 0);
            admin.setPassword(encodedPassword);
            doReturn(admin).when(userService).getOne(any(LambdaQueryWrapper.class));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

            AdminLoginDTO dto = buildAdminLoginDTO(phone, rawPassword);

            assertThatThrownBy(() -> userService.adminLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(403);
                        assertThat(bex.getMessage()).contains("账号已被禁用");
                    });
        }

        @Test
        @DisplayName("U-A-005: 手机号存在 PARENT 角色但无 ADMIN → 401")
        void shouldThrowWhenPhoneExistsAsParentOnly() {
            String phone = "13700005555";
            // getOne with ADMIN role returns null
            doReturn(null).when(userService).getOne(any(LambdaQueryWrapper.class));

            AdminLoginDTO dto = buildAdminLoginDTO(phone, "anyPassword");

            assertThatThrownBy(() -> userService.adminLogin(dto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(401);
                        assertThat(bex.getMessage()).isEqualTo("账号或密码错误");
                    });
        }
    }

    // ==================== sendVerifyCode Tests ====================

    @Nested
    @DisplayName("sendVerifyCode - 发送验证码")
    class SendVerifyCodeTests {

        @Test
        @DisplayName("U-V-001: 正常发送（无频率限制 key）→ 写入 Redis")
        void shouldSendCodeWhenNoRateLimit() {
            String phone = "13600001111";
            when(redisTemplate.hasKey(VERIFY_LIMIT_PREFIX + phone)).thenReturn(false);

            userService.sendVerifyCode(phone);

            // Verify code was written to Redis
            verify(valueOperations).set(eq(VERIFY_CODE_PREFIX + phone), anyString(), eq(5L), eq(java.util.concurrent.TimeUnit.MINUTES));
            // Verify rate limit key was set
            verify(valueOperations).set(eq(VERIFY_LIMIT_PREFIX + phone), eq("1"), eq(60L), eq(java.util.concurrent.TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("U-V-002: 频率限制 key 存在 → 400 '验证码发送过于频繁'")
        void shouldThrowWhenRateLimited() {
            String phone = "13600002222";
            when(redisTemplate.hasKey(VERIFY_LIMIT_PREFIX + phone)).thenReturn(true);

            assertThatThrownBy(() -> userService.sendVerifyCode(phone))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException bex = (BusinessException) ex;
                        assertThat(bex.getCode()).isEqualTo(400);
                        assertThat(bex.getMessage()).contains("验证码发送过于频繁");
                    });
        }

        @Test
        @DisplayName("U-V-003: 频率限制过期后 → 正常发送")
        void shouldSendCodeAfterRateLimitExpires() {
            String phone = "13600003333";
            // Rate limit key no longer exists (expired)
            when(redisTemplate.hasKey(VERIFY_LIMIT_PREFIX + phone)).thenReturn(false);

            userService.sendVerifyCode(phone);

            verify(valueOperations).set(eq(VERIFY_CODE_PREFIX + phone), anyString(), eq(5L), eq(java.util.concurrent.TimeUnit.MINUTES));
            verify(valueOperations).set(eq(VERIFY_LIMIT_PREFIX + phone), eq("1"), eq(60L), eq(java.util.concurrent.TimeUnit.SECONDS));
        }
    }
}
