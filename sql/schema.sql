-- ============================================================
-- AI_Educatin 家教平台 - 数据库建表脚本 (5张核心表)
-- 家长端 + 教师端
-- ============================================================

CREATE DATABASE IF NOT EXISTS ai_education
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ai_education;

-- -----------------------------------------------------------
-- 1. 用户表 (家长和老师共用，通过 role 区分)
-- -----------------------------------------------------------
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL                COMMENT '用户名(登录用)',
    `password`    VARCHAR(128) NOT NULL                COMMENT '密码(BCrypt加密)',
    `openid`      VARCHAR(64)  DEFAULT NULL             COMMENT '微信小程序openid',
    `phone`       VARCHAR(20)  DEFAULT NULL             COMMENT '手机号',
    `nickname`    VARCHAR(64)  DEFAULT NULL             COMMENT '昵称',
    `avatar_url`  VARCHAR(512) DEFAULT NULL             COMMENT '头像地址',
    `role`        TINYINT      NOT NULL                 COMMENT '角色: 1-家长 2-老师 3-管理员',
    `status`      TINYINT      NOT NULL DEFAULT 1       COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB COMMENT='用户表';


-- -----------------------------------------------------------
-- 2. 教师档案表
-- -----------------------------------------------------------
CREATE TABLE `teacher_profile` (
    `id`                  BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`             BIGINT        NOT NULL                 COMMENT '关联 user.id',
    `real_name`           VARCHAR(32)   NOT NULL                 COMMENT '真实姓名',
    `gender`              TINYINT       NOT NULL                 COMMENT '性别: 1-男 2-女',
    `id_card_encrypted`   VARCHAR(256)  DEFAULT NULL             COMMENT '身份证号(AES加密存储)',
    `id_card_front_url`   VARCHAR(512)  DEFAULT NULL             COMMENT '身份证正面照片',
    `id_card_back_url`    VARCHAR(512)  DEFAULT NULL             COMMENT '身份证背面照片',
    `university`          VARCHAR(64)   NOT NULL                 COMMENT '就读大学',
    `major`               VARCHAR(64)   DEFAULT NULL             COMMENT '专业',
    `education_level`     VARCHAR(16)   NOT NULL                 COMMENT '学历层次: 本科/硕士/博士',
    `grade`               VARCHAR(16)   NOT NULL                 COMMENT '在读年级: 大一~大四/研一~研三',
    `student_id_url`      VARCHAR(512)  DEFAULT NULL             COMMENT '学生证照片',
    `self_intro`          TEXT          DEFAULT NULL             COMMENT '个人简介/教学风格',
    `teaching_experience` TEXT          DEFAULT NULL             COMMENT '家教经验描述',
    `subjects`            VARCHAR(256)  NOT NULL                 COMMENT '擅长科目,逗号分隔: 数学,物理,英语',
    `grade_range`         VARCHAR(256)  NOT NULL                 COMMENT '可教年级范围,逗号分隔: 高一,高二,高三',
    `district`            VARCHAR(32)   NOT NULL                 COMMENT '服务区域(区): 雁塔区/碑林区/莲湖区...',
    `detail_address`      VARCHAR(256)  DEFAULT NULL             COMMENT '常驻地址(用于就近匹配)',
    `teach_mode`          TINYINT       NOT NULL DEFAULT 1       COMMENT '授课方式: 1-上门 2-线上 3-均可',
    `price_per_hour`      DECIMAL(8,2)  NOT NULL                 COMMENT '期望时薪(元)',
    `auth_status`         TINYINT       NOT NULL DEFAULT 0       COMMENT '认证状态: 0-待审核 1-已通过 2-已拒绝',
    `reject_reason`       VARCHAR(256)  DEFAULT NULL             COMMENT '审核拒绝原因',
    `avg_rating`          DECIMAL(3,2)  NOT NULL DEFAULT 0.00    COMMENT '平均评分(1~5)',
    `total_orders`        INT           NOT NULL DEFAULT 0       COMMENT '累计完成订单数',
    `create_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_district_auth` (`district`, `auth_status`),
    KEY `idx_auth_status` (`auth_status`)
) ENGINE=InnoDB COMMENT='教师档案表';


-- -----------------------------------------------------------
-- 3. 家长档案表
-- -----------------------------------------------------------
CREATE TABLE `parent_profile` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       NOT NULL                 COMMENT '关联 user.id',
    `real_name`   VARCHAR(32)  DEFAULT NULL              COMMENT '家长姓名',
    `phone`       VARCHAR(20)  NOT NULL                  COMMENT '联系电话',
    `district`    VARCHAR(32)  NOT NULL                  COMMENT '所在区: 雁塔区/碑林区...',
    `address`     VARCHAR(256) DEFAULT NULL              COMMENT '详细地址',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='家长档案表';


-- -----------------------------------------------------------
-- 4. 学生信息表 (一个家长可以有多个孩子)
-- -----------------------------------------------------------
CREATE TABLE `student_info` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `parent_id`   BIGINT       NOT NULL                  COMMENT '关联 parent_profile.id',
    `name`        VARCHAR(32)  NOT NULL                   COMMENT '孩子姓名',
    `gender`      TINYINT      NOT NULL                   COMMENT '性别: 1-男 2-女',
    `grade`       VARCHAR(16)  NOT NULL                   COMMENT '当前年级: 高一/初三/六年级...',
    `school`      VARCHAR(64)  DEFAULT NULL               COMMENT '就读学校',
    `remark`      VARCHAR(256) DEFAULT NULL               COMMENT '备注(学习情况等)',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB COMMENT='学生信息表';


-- -----------------------------------------------------------
-- 5. 辅导需求表
--    字段映射 (以043043号家教为例):
--    demand_no=043043  subject=数学,物理  teacher_count=1
--    current_level=补基础  frequency=每周1次  duration_hours=2.0
--    prefer_weekday=周日  price_per_hour=130.00
--    district=雁塔区  address=雁环路龙湖紫宸一期
--    teacher_gender_req=1(男)  teacher_requirement=高中理科经验丰富,思路清晰有方法
-- -----------------------------------------------------------
CREATE TABLE `tutoring_demand` (
    `id`                  BIGINT        NOT NULL AUTO_INCREMENT,
    `demand_no`           VARCHAR(32)   NOT NULL                 COMMENT '需求编号',
    `parent_id`           BIGINT        NOT NULL                 COMMENT '关联 parent_profile.id',
    `student_id`          BIGINT        NOT NULL                 COMMENT '关联 student_info.id',
    `subject`             VARCHAR(64)   NOT NULL                 COMMENT '补习科目(逗号分隔)',
    `teacher_count`       TINYINT       NOT NULL DEFAULT 1       COMMENT '需要几位老师',
    `current_level`       VARCHAR(32)   DEFAULT NULL             COMMENT '现阶段水平: 补基础/中等提升/拔尖冲刺',
    `frequency`           VARCHAR(32)   NOT NULL                 COMMENT '频次: 每周1次/每周2次',
    `duration_hours`      DECIMAL(4,1)  NOT NULL                 COMMENT '每次时长(小时)',
    `prefer_weekday`      VARCHAR(64)   NOT NULL                 COMMENT '偏好上课日: 周日/周六,周日',
    `prefer_time_slot`    VARCHAR(64)   DEFAULT NULL             COMMENT '偏好时间段: 上午/下午/晚上/不限',
    `price_per_hour`      DECIMAL(8,2)  NOT NULL                 COMMENT '报价(元/小时)',
    `district`            VARCHAR(32)   NOT NULL                 COMMENT '区: 雁塔区',
    `address`             VARCHAR(256)  NOT NULL                 COMMENT '详细地址',
    `teach_mode`          TINYINT       NOT NULL DEFAULT 1       COMMENT '授课方式: 1-上门 2-线上 3-均可',
    `teacher_gender_req`  TINYINT       DEFAULT NULL             COMMENT '老师性别要求: 1-男 2-女 NULL-不限',
    `teacher_requirement` VARCHAR(512)  DEFAULT NULL             COMMENT '其他要求',
    `status`              TINYINT       NOT NULL DEFAULT 0       COMMENT '0-发布中 1-已匹配 2-上课中 3-已完成 4-已关闭',
    `create_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_demand_no` (`demand_no`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status_district` (`status`, `district`),
    KEY `idx_subject` (`subject`)
) ENGINE=InnoDB COMMENT='辅导需求表';
