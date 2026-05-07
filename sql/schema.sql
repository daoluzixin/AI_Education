-- ============================================================
-- 博文学堂 V1 - 数据库建表脚本 (7张核心表)
-- 家长端 + 学生端(大学生家教) + 后台管理端
-- 参考原有schema风格: utf8mb4, InnoDB, 统一时间字段, 索引命名规范
-- ============================================================

CREATE DATABASE IF NOT EXISTS ai_education
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ai_education;

-- -----------------------------------------------------------
-- 1. 用户表 (家长、学生、管理员共用，通过 role 区分)
-- -----------------------------------------------------------
CREATE TABLE `user` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '用户ID(主键)',
    `phone`         VARCHAR(11)   NOT NULL                COMMENT '手机号(登录凭证,唯一)',
    `password`      VARCHAR(128)  DEFAULT NULL            COMMENT '密码(BCrypt加密,仅管理员使用)',
    `email`         VARCHAR(128)  DEFAULT NULL            COMMENT '教育邮箱(.edu.cn,仅学生)',
    `nickname`      VARCHAR(64)   DEFAULT NULL            COMMENT '昵称(家长注册时填写)',
    `avatar_url`    VARCHAR(512)  DEFAULT NULL            COMMENT '头像地址',
    `role`          TINYINT       NOT NULL                COMMENT '角色: 1-家长 2-学生(大学生家教) 3-管理员',
    `status`        TINYINT       NOT NULL DEFAULT 1      COMMENT '账号状态: 0-禁用 1-正常',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_role_status` (`role`, `status`)
) ENGINE=InnoDB COMMENT='用户表(三端共用)';


-- -----------------------------------------------------------
-- 2. 学生档案表 (大学生家教入驻资料)
-- -----------------------------------------------------------
CREATE TABLE `student_profile` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '档案ID(主键)',
    `user_id`           BIGINT        NOT NULL                COMMENT '关联 user.id',
    `student_no`        VARCHAR(32)   NOT NULL                COMMENT '学生编号(STU前缀,提交审核时生成)',
    `real_name`         VARCHAR(32)   NOT NULL                COMMENT '真实姓名',
    `gender`            TINYINT       NOT NULL                COMMENT '性别: 1-男 2-女',
    `birth_date`        DATE          NOT NULL                COMMENT '出生日期(系统计算年龄)',
    `city`              VARCHAR(64)   NOT NULL                COMMENT '所在城市(省-市-区)',
    `school_name`       VARCHAR(64)   NOT NULL                COMMENT '学校名称',
    `grade`             VARCHAR(16)   NOT NULL                COMMENT '年级: FRESHMAN/SOPHOMORE/JUNIOR/SENIOR/MASTER_1/MASTER_2/MASTER_3',
    `avatar`            VARCHAR(512)  NOT NULL                COMMENT '个人照片URL',
    `introduction`      VARCHAR(500)  DEFAULT NULL            COMMENT '自我介绍(限300字)',
    `subjects`          VARCHAR(256)  NOT NULL                COMMENT '擅长方向(逗号分隔): MATH,PHYSICS,ENGLISH',
    `tags`              VARCHAR(256)  NOT NULL                COMMENT '个人标签(逗号分隔,最多5个)',
    `student_id_photo`  VARCHAR(1024) DEFAULT NULL            COMMENT '学生证照片URL(正反面,逗号分隔)',
    `certificates`      VARCHAR(2048) DEFAULT NULL            COMMENT '获奖证书URL(逗号分隔,最多5张)',
    `transcripts`       VARCHAR(1024) DEFAULT NULL            COMMENT '成绩证明URL(逗号分隔,最多3张)',
    `supplements`       VARCHAR(1024) DEFAULT NULL            COMMENT '其他补充材料URL(逗号分隔,最多3个)',
    `review_status`     TINYINT       NOT NULL DEFAULT 0      COMMENT '审核状态: 0-草稿(DRAFT) 1-待审核(PENDING_REVIEW) 2-通过(APPROVED) 3-驳回(REJECTED) 4-待补充(NEED_SUPPLEMENT)',
    `reject_reason`     VARCHAR(512)  DEFAULT NULL            COMMENT '驳回原因(REJECTED时填写)',
    `supplement_note`   VARCHAR(512)  DEFAULT NULL            COMMENT '需补充内容说明(NEED_SUPPLEMENT时填写)',
    `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    KEY `idx_review_status` (`review_status`),
    KEY `idx_city_status` (`city`, `review_status`),
    KEY `idx_school` (`school_name`)
) ENGINE=InnoDB COMMENT='学生档案表(大学生家教入驻资料)';


-- -----------------------------------------------------------
-- 3. 审核记录表 (每次审核操作一条记录,形成审计轨迹)
-- -----------------------------------------------------------
CREATE TABLE `review_record` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID(主键)',
    `student_id`      BIGINT        NOT NULL                COMMENT '关联 student_profile.id',
    `reviewer_id`     BIGINT        NOT NULL                COMMENT '审核人 user.id(管理员)',
    `reviewer_name`   VARCHAR(64)   NOT NULL                COMMENT '审核人账号名',
    `review_result`   TINYINT       NOT NULL                COMMENT '审核结果: 2-通过(APPROVED) 3-驳回(REJECTED) 4-待补充(NEED_SUPPLEMENT)',
    `review_note`     VARCHAR(512)  DEFAULT NULL            COMMENT '审核备注(驳回原因或补充说明)',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB COMMENT='审核记录表(不可删除,审计轨迹)';


-- -----------------------------------------------------------
-- 4. 家长需求表
-- -----------------------------------------------------------
CREATE TABLE `demand` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '需求ID(主键)',
    `demand_no`       VARCHAR(32)   NOT NULL                COMMENT '需求编号(REQ前缀)',
    `user_id`         BIGINT        NOT NULL                COMMENT '关联 user.id(家长)',
    `child_grade`     VARCHAR(16)   NOT NULL                COMMENT '孩子年级: PRIMARY_1~SENIOR_3',
    `demand_type`     VARCHAR(32)   NOT NULL                COMMENT '需求类型: SUBJECT_TUTOR/INTEREST/COMPETITION/ADMISSION/OTHER',
    `city`            VARCHAR(64)   NOT NULL                COMMENT '城市(省-市-区)',
    `expectations`    VARCHAR(500)  NOT NULL                COMMENT '期望条件(限200字)',
    `budget`          VARCHAR(32)   DEFAULT NULL            COMMENT '预算范围: 50-100/100-150/150-200/200+',
    `remark`          VARCHAR(1000) DEFAULT NULL            COMMENT '补充说明(限500字)',
    `status`          TINYINT       NOT NULL DEFAULT 0      COMMENT '需求状态: 0-待处理(PENDING) 1-推荐中(MATCHING) 2-已推荐(RECOMMENDED) 3-已关闭(CLOSED)',
    `close_reason`    VARCHAR(256)  DEFAULT NULL            COMMENT '关闭原因(CLOSED时填写)',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_demand_no` (`demand_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_city_status` (`city`, `status`),
    KEY `idx_demand_type` (`demand_type`)
) ENGINE=InnoDB COMMENT='家长需求表';


-- -----------------------------------------------------------
-- 5. 推荐关系表 (需求与学生的多对多关系)
-- -----------------------------------------------------------
CREATE TABLE `recommendation` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '推荐ID(主键)',
    `demand_id`       BIGINT        NOT NULL                COMMENT '关联 demand.id',
    `student_id`      BIGINT        NOT NULL                COMMENT '关联 student_profile.id',
    `sort_order`      INT           NOT NULL DEFAULT 0      COMMENT '推荐顺序(越小越靠前)',
    `operator_id`     BIGINT        NOT NULL                COMMENT '操作人 user.id(运营)',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_demand_student` (`demand_id`, `student_id`),
    KEY `idx_demand_id` (`demand_id`),
    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB COMMENT='推荐关系表(需求-学生多对多)';


-- -----------------------------------------------------------
-- 6. 咨询工单表
-- -----------------------------------------------------------
CREATE TABLE `ticket` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '工单ID(主键)',
    `ticket_no`         VARCHAR(32)   NOT NULL                COMMENT '工单编号(TKT前缀)',
    `user_id`           BIGINT        NOT NULL                COMMENT '提交人 user.id',
    `user_type`         TINYINT       NOT NULL                COMMENT '用户类型: 1-家长(PARENT) 2-学生(STUDENT)',
    `related_demand_id` BIGINT        DEFAULT NULL            COMMENT '关联需求 demand.id(可选)',
    `ticket_type`       VARCHAR(32)   NOT NULL                COMMENT '咨询类型: RECOMMEND_UNSATISFIED/SERVICE_ISSUE/COMPLAINT/REGISTRATION_ISSUE/REVIEW_ISSUE/PROFILE_MODIFY/OTHER',
    `description`       VARCHAR(1000) NOT NULL                COMMENT '问题描述(限500字)',
    `contact_phone`     VARCHAR(11)   NOT NULL                COMMENT '联系电话',
    `attachments`       VARCHAR(2048) DEFAULT NULL            COMMENT '附件URL(逗号分隔,最多3个)',
    `status`            TINYINT       NOT NULL DEFAULT 0      COMMENT '工单状态: 0-待处理(PENDING) 1-处理中(PROCESSING) 2-已处理(RESOLVED) 3-已关闭(CLOSED)',
    `handler_id`        BIGINT        DEFAULT NULL            COMMENT '处理人 user.id(客服)',
    `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_no` (`ticket_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_user_type_status` (`user_type`, `status`),
    KEY `idx_handler_id` (`handler_id`)
) ENGINE=InnoDB COMMENT='咨询工单表';


-- -----------------------------------------------------------
-- 7. 工单回复表 (客服回复记录)
-- -----------------------------------------------------------
CREATE TABLE `ticket_reply` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '回复ID(主键)',
    `ticket_id`       BIGINT        NOT NULL                COMMENT '关联 ticket.id',
    `replier_id`      BIGINT        NOT NULL                COMMENT '回复人 user.id(客服)',
    `replier_name`    VARCHAR(64)   NOT NULL                COMMENT '回复人账号名',
    `content`         VARCHAR(2000) NOT NULL                COMMENT '回复内容',
    `attachments`     VARCHAR(2048) DEFAULT NULL            COMMENT '附件URL(逗号分隔)',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
    PRIMARY KEY (`id`),
    KEY `idx_ticket_id` (`ticket_id`)
) ENGINE=InnoDB COMMENT='工单回复表';
