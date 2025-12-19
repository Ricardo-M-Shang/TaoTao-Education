-- 创建数据库
CREATE DATABASE IF NOT EXISTS taotao_education DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE taotao_education;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
                                      id BIGINT PRIMARY KEY COMMENT '主键ID',
                                      username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
                                      password VARCHAR(100) NOT NULL COMMENT '密码',
                                      phone VARCHAR(20) COMMENT '手机号',
                                      email VARCHAR(100) COMMENT '邮箱',
                                      nickname VARCHAR(50) COMMENT '昵称',
                                      avatar VARCHAR(255) COMMENT '头像URL',
                                      gender TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
                                      province VARCHAR(50) COMMENT '省份',
                                      city VARCHAR(50) COMMENT '城市',
                                      signature VARCHAR(200) COMMENT '个性签名',
                                      role TINYINT DEFAULT 1 COMMENT '用户角色 1-学员 2-讲师 4-机构 5-运营',
                                      status TINYINT DEFAULT 1 COMMENT '账号状态 0-禁用 1-正常',
                                      last_login_time DATETIME COMMENT '最后登录时间',
                                      last_login_ip VARCHAR(50) COMMENT '最后登录IP',
                                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                      INDEX idx_username (username),
                                      INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 课程分类表
CREATE TABLE IF NOT EXISTS t_category (
                                          id BIGINT PRIMARY KEY COMMENT '主键ID',
                                          name VARCHAR(50) NOT NULL COMMENT '分类名称',
                                          parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
                                          level TINYINT DEFAULT 1 COMMENT '层级',
                                          sort INT DEFAULT 0 COMMENT '排序',
                                          icon VARCHAR(255) COMMENT '图标',
                                          status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
                                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                          INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程分类表';

-- 课程表（含机构、审核、分成相关字段）
CREATE TABLE IF NOT EXISTS t_course (
                                        id BIGINT PRIMARY KEY COMMENT '主键ID',
                                        title VARCHAR(100) NOT NULL COMMENT '课程标题',
                                        subtitle VARCHAR(200) COMMENT '课程副标题',
                                        cover VARCHAR(255) COMMENT '课程封面图片',
                                        description TEXT COMMENT '课程简介',
                                        content LONGTEXT COMMENT '课程详情（富文本）',
                                        teacher_id BIGINT COMMENT '讲师ID',
                                        teacher_name VARCHAR(50) COMMENT '讲师名称',
                                        org_id BIGINT COMMENT '机构ID',
                                        org_name VARCHAR(100) COMMENT '机构名称',
                                        category_id BIGINT COMMENT '分类ID',
                                        category_name VARCHAR(50) COMMENT '分类名称',
                                        type TINYINT DEFAULT 1 COMMENT '课程类型 1-录播 2-直播 3-图文',
                                        price DECIMAL(10, 2) DEFAULT 0.00 COMMENT '课程价格',
                                        original_price DECIMAL(10, 2) COMMENT '原价',
                                        is_free TINYINT DEFAULT 0 COMMENT '是否免费 0-收费 1-免费',
                                        status TINYINT DEFAULT 0 COMMENT '课程状态 0-草稿 1-待审核 2-已发布 3-已下架',
                                        audit_remark VARCHAR(255) COMMENT '审核备注',
                                        audit_time DATETIME COMMENT '审核时间',
                                        auditor_id BIGINT COMMENT '审核人ID',
                                        auditor_name VARCHAR(50) COMMENT '审核人姓名',
                                        lesson_count INT DEFAULT 0 COMMENT '课时数',
                                        study_count INT DEFAULT 0 COMMENT '学习人数',
                                        score DECIMAL(2, 1) DEFAULT 0.0 COMMENT '评分',
                                        total_duration BIGINT DEFAULT 0 COMMENT '总时长（秒）',
                                        sort INT DEFAULT 0 COMMENT '排序',
                                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                        INDEX idx_teacher_id (teacher_id),
                                        INDEX idx_org_id (org_id),
                                        INDEX idx_category_id (category_id),
                                        INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 章节表
CREATE TABLE IF NOT EXISTS t_chapter (
                                         id BIGINT PRIMARY KEY COMMENT '主键ID',
                                         course_id BIGINT NOT NULL COMMENT '课程ID',
                                         title VARCHAR(100) NOT NULL COMMENT '章节标题',
                                         sort INT DEFAULT 0 COMMENT '排序',
                                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                         INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='章节表';

-- 课时表
CREATE TABLE IF NOT EXISTS t_lesson (
                                        id BIGINT PRIMARY KEY COMMENT '主键ID',
                                        course_id BIGINT NOT NULL COMMENT '课程ID',
                                        chapter_id BIGINT NOT NULL COMMENT '章节ID',
                                        title VARCHAR(100) NOT NULL COMMENT '课时标题',
                                        video_url VARCHAR(500) COMMENT '视频URL',
                                        duration BIGINT DEFAULT 0 COMMENT '视频时长（秒）',
                                        is_free TINYINT DEFAULT 0 COMMENT '是否可试看 0-否 1-是',
                                        sort INT DEFAULT 0 COMMENT '排序',
                                        type TINYINT DEFAULT 1 COMMENT '课时类型 1-视频 2-图文 3-直播',
                                        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                        INDEX idx_course_id (course_id),
                                        INDEX idx_chapter_id (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课时表';

-- 订单表（含机构分成）
CREATE TABLE IF NOT EXISTS t_order (
                                       id BIGINT PRIMARY KEY COMMENT '主键ID',
                                       order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
                                       user_id BIGINT NOT NULL COMMENT '用户ID',
                                       username VARCHAR(50) COMMENT '用户名',
                                       coupon_id BIGINT COMMENT '使用的用户优惠券ID',
                                       course_id BIGINT NOT NULL COMMENT '课程ID',
                                       course_title VARCHAR(100) COMMENT '课程标题',
                                       course_cover VARCHAR(255) COMMENT '课程封面',
                                       teacher_name VARCHAR(50) COMMENT '讲师名称',
                                       org_id BIGINT COMMENT '机构ID',
                                       org_name VARCHAR(100) COMMENT '机构名称',
                                       original_price DECIMAL(10, 2) COMMENT '订单原价',
                                       discount_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '优惠金额',
                                       pay_amount DECIMAL(10, 2) NOT NULL COMMENT '实付金额',
                                       org_income DECIMAL(10, 2) DEFAULT 0 COMMENT '机构分成',
                                       platform_income DECIMAL(10, 2) DEFAULT 0 COMMENT '平台分成',
                                       pay_type TINYINT COMMENT '支付方式 1-支付宝 2-微信',
                                       status TINYINT DEFAULT 0 COMMENT '订单状态 0-待支付 1-已支付 2-已取消 3-已退款',
                                       pay_time DATETIME COMMENT '支付时间',
                                       expire_time DATETIME COMMENT '过期时间',
                                       remark VARCHAR(255) COMMENT '备注',
                                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                       INDEX idx_order_no (order_no),
                                       INDEX idx_user_id (user_id),
                                       INDEX idx_course_id (course_id),
                                       INDEX idx_org_id (org_id),
                                       INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 用户课程关联表
CREATE TABLE IF NOT EXISTS t_user_course (
                                             id BIGINT PRIMARY KEY COMMENT '主键ID',
                                             user_id BIGINT NOT NULL COMMENT '用户ID',
                                             course_id BIGINT NOT NULL COMMENT '课程ID',
                                             order_id BIGINT COMMENT '订单ID',
                                             progress INT DEFAULT 0 COMMENT '学习进度（百分比）',
                                             last_study_time DATETIME COMMENT '最后学习时间',
                                             current_lesson_id BIGINT COMMENT '当前学习的课时ID',
                                             is_finished TINYINT DEFAULT 0 COMMENT '是否完成 0-未完成 1-已完成',
                                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                             UNIQUE KEY uk_user_course (user_id, course_id),
                                             INDEX idx_user_id (user_id),
                                             INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户课程关联表';

-- 课程评价表
CREATE TABLE IF NOT EXISTS t_course_review (
                                               id BIGINT PRIMARY KEY COMMENT '主键ID',
                                               course_id BIGINT NOT NULL COMMENT '课程ID',
                                               user_id BIGINT NOT NULL COMMENT '用户ID',
                                               username VARCHAR(50) COMMENT '用户名',
                                               nickname VARCHAR(50) COMMENT '用户昵称',
                                               avatar VARCHAR(255) COMMENT '用户头像',
                                               score INT NOT NULL COMMENT '评分（1-5星）',
                                               content VARCHAR(500) COMMENT '评价内容',
                                               is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名 0-否 1-是',
                                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                               INDEX idx_course_id (course_id),
                                               INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程评价表';

-- 课程问题表
CREATE TABLE IF NOT EXISTS t_course_question (
                                                 id BIGINT PRIMARY KEY COMMENT '主键ID',
                                                 course_id BIGINT NOT NULL COMMENT '课程ID',
                                                 user_id BIGINT NOT NULL COMMENT '用户ID',
                                                 username VARCHAR(50) COMMENT '用户名',
                                                 nickname VARCHAR(50) COMMENT '昵称',
                                                 content TEXT NOT NULL COMMENT '问题内容',
                                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                 update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                 deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                                 INDEX idx_course_id (course_id),
                                                 INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程问题表';

-- 课程回答表
CREATE TABLE IF NOT EXISTS t_course_answer (
                                               id BIGINT PRIMARY KEY COMMENT '主键ID',
                                               question_id BIGINT NOT NULL COMMENT '问题ID',
                                               course_id BIGINT NOT NULL COMMENT '课程ID',
                                               user_id BIGINT NOT NULL COMMENT '用户ID',
                                               username VARCHAR(50) COMMENT '用户名',
                                               nickname VARCHAR(50) COMMENT '昵称',
                                               content TEXT NOT NULL COMMENT '回答内容',
                                               accepted TINYINT DEFAULT 0 COMMENT '是否被采纳 0-否 1-是',
                                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                               INDEX idx_question_id (question_id),
                                               INDEX idx_course_id (course_id),
                                               INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程回答表';

-- 优惠券表
CREATE TABLE IF NOT EXISTS t_coupon (
                                       id BIGINT PRIMARY KEY COMMENT '主键ID',
                                       name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
                                       type TINYINT NOT NULL COMMENT '类型 1-满减 2-折扣',
                                       discount_amount DECIMAL(10,2) COMMENT '满减金额',
                                       discount_rate DECIMAL(5,2) COMMENT '折扣（0-1）',
                                       threshold_amount DECIMAL(10,2) DEFAULT 0 COMMENT '使用门槛金额',
                                       total INT NOT NULL COMMENT '发行总量',
                                       stock INT NOT NULL COMMENT '剩余库存',
                                       limit_per_user INT DEFAULT 1 COMMENT '每人限领张数',
                                       valid_from DATETIME NOT NULL COMMENT '生效时间',
                                       valid_to DATETIME NOT NULL COMMENT '失效时间',
                                       status TINYINT DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
                                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券主表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS t_user_coupon (
                                            id BIGINT PRIMARY KEY COMMENT '主键ID',
                                            coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
                                            user_id BIGINT NOT NULL COMMENT '用户ID',
                                            status TINYINT DEFAULT 0 COMMENT '0-未使用 1-已使用 2-已过期',
                                            order_no VARCHAR(32) COMMENT '使用的订单号',
                                            obtain_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
                                            use_time DATETIME COMMENT '使用时间',
                                            valid_from DATETIME NOT NULL COMMENT '生效时间',
                                            valid_to DATETIME NOT NULL COMMENT '失效时间',
                                            deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                            UNIQUE KEY uk_user_coupon (coupon_id, user_id),
                                            INDEX idx_user_id (user_id),
                                            INDEX idx_coupon_id (coupon_id),
                                            INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- 课程收藏表
CREATE TABLE IF NOT EXISTS t_course_favorite (
                                                 id BIGINT PRIMARY KEY COMMENT '主键ID',
                                                 course_id BIGINT NOT NULL COMMENT '课程ID',
                                                 user_id BIGINT NOT NULL COMMENT '用户ID',
                                                 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                 update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                 deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                                 UNIQUE KEY uk_user_course (user_id, course_id),
                                                 INDEX idx_user_id (user_id),
                                                 INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程收藏表';

-- 学习记录表
CREATE TABLE IF NOT EXISTS t_study_record (
                                              id BIGINT PRIMARY KEY COMMENT '主键ID',
                                              user_id BIGINT NOT NULL COMMENT '用户ID',
                                              course_id BIGINT NOT NULL COMMENT '课程ID',
                                              lesson_id BIGINT NOT NULL COMMENT '课时ID',
                                              chapter_id BIGINT COMMENT '章节ID',
                                              duration INT DEFAULT 0 COMMENT '本次学习时长（秒）',
                                              progress INT DEFAULT 0 COMMENT '课时学习进度（百分比）',
                                              is_finished TINYINT DEFAULT 0 COMMENT '是否完成 0-未完成 1-已完成',
                                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                              UNIQUE KEY uk_user_lesson (user_id, lesson_id),
                                              INDEX idx_user_id (user_id),
                                              INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习记录表';

-- 学习笔记表
CREATE TABLE IF NOT EXISTS t_note (
                                      id BIGINT PRIMARY KEY COMMENT '主键ID',
                                      user_id BIGINT NOT NULL COMMENT '用户ID',
                                      course_id BIGINT NOT NULL COMMENT '课程ID',
                                      lesson_id BIGINT NOT NULL COMMENT '课时ID',
                                      content TEXT NOT NULL COMMENT '笔记内容',
                                      video_time INT COMMENT '视频时间点（秒）',
                                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                      INDEX idx_user_id (user_id),
                                      INDEX idx_course_id (course_id),
                                      INDEX idx_lesson_id (lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习笔记表';

-- =============================================
-- 示例数据（INSERT IGNORE，便于多次执行）
-- =============================================

-- 用户：学员/讲师/机构/运营
INSERT IGNORE INTO t_user (id, username, password, nickname, role, avatar) VALUES
                                                                               (1, 'student1', '$2a$10$hash123', '学生A', 1, NULL),
                                                                               (2, 'teacher1', '$2a$10$hash123', '讲师A', 2, NULL),
                                                                               (3, 'org1',     '$2a$10$hash123', '机构A', 4, NULL),
                                                                               (4, 'ops1',     '$2a$10$hash123', '运营A', 5, NULL);

-- 分类
INSERT IGNORE INTO t_category (id, name, parent_id, level, sort) VALUES
                                                                     (100, '编程', 0, 1, 1),
                                                                     (101, '前端', 100, 2, 1),
                                                                     (102, '后端', 100, 2, 2);

-- 课程（示例：待审核、已发布）
INSERT IGNORE INTO t_course (id, title, subtitle, cover, description, teacher_id, teacher_name, org_id, org_name, category_id, category_name, type, price, original_price, is_free, status, lesson_count, study_count, score)
VALUES
    (1000, 'Vue3 实战', '从零到上线', NULL, 'Vue3 + TS 实战课程', 2, '讲师A', 3, '机构A', 101, '前端', 1, 199.00, 299.00, 0, 1, 0, 0, 0.0),
    (1001, 'Spring Boot 入门', '快速上手', NULL, 'Spring Boot 基础', 2, '讲师A', 3, '机构A', 102, '后端', 1, 99.00, 199.00, 0, 2, 5, 120, 4.8);

-- 章节
INSERT IGNORE INTO t_chapter (id, course_id, title, sort) VALUES
                                                              (2000, 1001, '第一章 环境准备', 1),
                                                              (2001, 1001, '第二章 快速开始', 2);

-- 课时
INSERT IGNORE INTO t_lesson (id, course_id, chapter_id, title, video_url, duration, is_free, sort, type) VALUES
                                                                                                             (3000, 1001, 2000, '安装与配置', 'http://localhost/uploads/video1.mp4', 600, 1, 1, 1),
                                                                                                             (3001, 1001, 2001, 'Hello World', 'http://localhost/uploads/video2.mp4', 900, 0, 1, 1);

-- 订单（示例：已支付，含机构分成 60/40）
INSERT IGNORE INTO t_order (id, order_no, user_id, username, course_id, course_title, course_cover, teacher_name, org_id, org_name, original_price, discount_amount, pay_amount, org_income, platform_income, pay_type, status, pay_time, expire_time)
VALUES
    (4000, 'ORD202401010001', 1, 'student1', 1001, 'Spring Boot 入门', NULL, '讲师A', 3, '机构A', 99.00, 0.00, 99.00, 59.40, 39.60, 1, 1, NOW(), NOW() + INTERVAL 30 MINUTE);

-- 用户课程（购买关系）
INSERT IGNORE INTO t_user_course (id, user_id, course_id, order_id, progress, is_finished) VALUES
    (5000, 1, 1001, 4000, 0, 0);

-- 收藏
INSERT IGNORE INTO t_course_favorite (id, course_id, user_id) VALUES
    (6000, 1001, 1);

-- 评价
INSERT IGNORE INTO t_course_review (id, course_id, user_id, username, nickname, score, content, is_anonymous) VALUES
    (7000, 1001, 1, 'student1', '学生A', 5, '课程很棒！', 0);

-- 学习记录
INSERT IGNORE INTO t_study_record (id, user_id, course_id, lesson_id, chapter_id, duration, progress, is_finished) VALUES
    (8000, 1, 1001, 3000, 2000, 300, 50, 0);

-- 笔记
INSERT IGNORE INTO t_note (id, user_id, course_id, lesson_id, content, video_time) VALUES
    (9000, 1, 1001, 3000, '重要概念记一下', 120);

ALTER TABLE t_order
    ADD COLUMN teacher_id BIGINT COMMENT '讲师ID' AFTER user_id;


UPDATE t_order AS o
    JOIN t_course AS c ON o.course_id = c.id
SET o.teacher_id = c.teacher_id
WHERE o.teacher_id IS NULL;

-- 用户：学员/讲师/机构/运营（密码需按你们的加密方式自行替换，这里先放明文占位）
INSERT INTO t_user (id, username, password, nickname, role, status, deleted)
VALUES
    (1001, 'student01', '$2a$10$BoOT6Os3XnWPehcgepKTOuKXbGzaAa03910aVFbq44GgVeG9', '学员01', 1, 1, 0),
    (1002, 'teacher01', '$2a$10$BoOT6Os3XnWPehcgepKTOuKXbGzaAa03910aVFbq44GgVeG9', '讲师01', 2, 1, 0),
    (1003, 'org01',     '$2a$10$BoOT6Os3XnWPehcgepKTOuKXbGzaAa03910aVFbq44GgVeG9', '机构01', 4, 1, 0),
    (1004, 'ops01',     '$2a$10$BoOT6Os3XnWPehcgepKTOuKXbGzaAa03910aVFbq44GgVeG9', '运营01', 5, 1, 0);

-- 分类
INSERT INTO t_category (id, name, parent_id, sort) VALUES
                                                       (2001, '编程开发', 0, 1),
                                                       (2002, '前端', 2001, 1);

-- 课程（已发布，org_id 关联机构，teacher_id 关联讲师）
INSERT INTO t_course (id, title, category_id, category_name, teacher_id, teacher_name, org_id, org_name,
                      price, is_free, status, study_count, lesson_count, cover, description, audit_remark, audit_time)
VALUES
    (3001, 'Vue3 实战入门', 2002, '前端', 1002, '讲师01', 1003, '机构01',
     199.00, 0, 2, 25, 2, 'https://example.com/cover/vue3.jpg', '从零到一掌握 Vue3 基础与实战', '自动通过', NOW());

-- 章节
INSERT INTO t_chapter (id, course_id, title, sort)
VALUES
    (4001, 3001, '开篇导学', 1),
    (4002, 3001, '核心语法', 2);

-- 课时（示例视频地址请按你们的存储替换）
INSERT INTO t_lesson (id, course_id, chapter_id, title, sort, type, video_url, duration)
VALUES
    (5001, 3001, 4001, '课程介绍', 1, 1, 'https://example.com/video/intro.mp4', 300),
    (5002, 3001, 4002, '组合式 API 实战', 1, 1, 'https://example.com/video/composition.mp4', 900);

-- 优惠券主表
INSERT INTO t_coupon (id, name, type, discount_amount, discount_rate, threshold_amount,
                      total, stock, limit_per_user, valid_from, valid_to, status)
VALUES
    (6001, '满100减30', 1, 30.00, NULL, 100.00,
     100, 100, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
    (6002, '95折通用', 2, NULL, 0.95, 0.00,
     100, 100, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1);

-- 用户领券
INSERT INTO t_user_coupon (id, coupon_id, user_id, status, order_no, obtain_time, use_time, valid_from, valid_to)
VALUES
    (6101, 6001, 1001, 0, NULL, NOW(), NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
    (6102, 6002, 1001, 0, NULL, NOW(), NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

-- 订单（含已支付，便于运营/机构看板有数据；pay_amount 体现分成）
-- 状态：0待支付，1已支付
INSERT INTO t_order (id, order_no, user_id, username, course_id, course_title, course_cover,
                     teacher_id, teacher_name, org_id, org_name,
                     original_price, discount_amount, pay_amount, status, pay_type, pay_time,
                     org_income, platform_income, coupon_id, create_time)
VALUES
    (7001, 'ORD-001', 1001, 'student01', 3001, 'Vue3 实战入门', 'https://example.com/cover/vue3.jpg',
     1002, '讲师01', 1003, '机构01',
     199.00, 30.00, 169.00, 1, 1, NOW(),
     169.00*0.6, 169.00*0.4, 6001, NOW()),
    (7002, 'ORD-002', 1001, 'student01', 3001, 'Vue3 实战入门', 'https://example.com/cover/vue3.jpg',
     1002, '讲师01', 1003, '机构01',
     199.00, 0.00, 199.00, 1, 1, DATE_SUB(NOW(), INTERVAL 1 DAY),
     199.00*0.6, 199.00*0.4, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 用户课程关联（已购）
INSERT INTO t_user_course (id, user_id, course_id, order_id, progress, is_finished)
VALUES
    (8001, 1001, 3001, 7001, 0, 0);

ALTER TABLE t_user_coupon
    ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER valid_to,
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

-- ========== Learning Service Tables ==========

-- 先删除可能存在的不完整表
DROP TABLE IF EXISTS t_learning_record;
DROP TABLE IF EXISTS t_learning_stats;  
DROP TABLE IF EXISTS t_user_learning_stats;

-- 学习记录表 (详细记录每次学习行为)
CREATE TABLE t_learning_record (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    lesson_id BIGINT NOT NULL COMMENT '课时ID',
    study_duration INT DEFAULT 0 COMMENT '本次学习时长(秒)',
    video_duration INT DEFAULT 0 COMMENT '视频总时长(秒)',
    last_position INT DEFAULT 0 COMMENT '最后播放位置(秒)',
    progress_percent DECIMAL(5,2) DEFAULT 0.00 COMMENT '播放进度百分比',
    is_completed TINYINT DEFAULT 0 COMMENT '是否完成 0-未完成 1-已完成',
    study_date DATE NOT NULL COMMENT '学习日期',
    device_type VARCHAR(20) COMMENT '设备类型 web/mobile/app',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_user_course (user_id, course_id),
    INDEX idx_user_date (user_id, study_date),
    INDEX idx_lesson (lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- 课程学习统计表 (聚合统计数据)
CREATE TABLE t_learning_stats (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    total_lessons INT DEFAULT 0 COMMENT '课程总课时数',
    completed_lessons INT DEFAULT 0 COMMENT '已完成课时数',
    total_duration INT DEFAULT 0 COMMENT '课程总时长(秒)',
    study_duration INT DEFAULT 0 COMMENT '已学习时长(秒)',
    completion_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '完成率',
    first_study_time DATETIME COMMENT '首次学习时间',
    last_study_time DATETIME COMMENT '最后学习时间',
    study_days INT DEFAULT 0 COMMENT '学习天数',
    avg_daily_duration INT DEFAULT 0 COMMENT '日均学习时长(秒)',
    is_finished TINYINT DEFAULT 0 COMMENT '是否完成课程 0-未完成 1-已完成',
    certificate_id BIGINT COMMENT '证书ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    UNIQUE KEY uk_user_course (user_id, course_id),
    INDEX idx_user (user_id),
    INDEX idx_completion (completion_rate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程学习统计表';

-- 用户学习总统计表
CREATE TABLE t_user_learning_stats (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    total_courses INT DEFAULT 0 COMMENT '总学习课程数',
    completed_courses INT DEFAULT 0 COMMENT '已完成课程数',
    total_study_duration INT DEFAULT 0 COMMENT '总学习时长(秒)',
    total_study_days INT DEFAULT 0 COMMENT '总学习天数',
    avg_daily_duration INT DEFAULT 0 COMMENT '日均学习时长(秒)',
    longest_streak INT DEFAULT 0 COMMENT '最长连续学习天数',
    current_streak INT DEFAULT 0 COMMENT '当前连续学习天数',
    last_study_date DATE COMMENT '最后学习日期',
    level_score INT DEFAULT 0 COMMENT '学习等级积分',
    certificates_count INT DEFAULT 0 COMMENT '获得证书数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_level (level_score),
    INDEX idx_last_study (last_study_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户学习总统计表';

-- 插入测试数据
-- 学习记录测试数据（learning-service 专用表）
INSERT INTO t_learning_record (id, user_id, course_id, lesson_id, study_duration, video_duration, last_position, progress_percent, is_completed, study_date, device_type) VALUES
(9001, 1001, 3001, 4001, 1200, 1800, 1200, 66.67, 0, CURDATE(), 'web'),
(9002, 1001, 3001, 4002, 1800, 1800, 1800, 100.00, 1, CURDATE(), 'web'),
(9003, 1001, 3001, 4001, 300, 1800, 1500, 83.33, 0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'mobile');

-- 课程学习统计测试数据  
INSERT INTO t_learning_stats (id, user_id, course_id, total_lessons, completed_lessons, total_duration, study_duration, completion_rate, first_study_time, last_study_time, study_days, avg_daily_duration, is_finished) VALUES
(9101, 1001, 3001, 10, 2, 18000, 3300, 20.00, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), 2, 1650, 0);

-- 用户学习总统计测试数据
INSERT INTO t_user_learning_stats (id, user_id, total_courses, completed_courses, total_study_duration, total_study_days, avg_daily_duration, longest_streak, current_streak, last_study_date, level_score) VALUES
(9201, 1001, 1, 0, 3300, 2, 1650, 2, 2, CURDATE(), 55);

-- ========== Chat Service Tables ==========

-- 聊天室表
DROP TABLE IF EXISTS t_chat_message;
DROP TABLE IF EXISTS t_chat_invitation;
DROP TABLE IF EXISTS t_chat_member;
DROP TABLE IF EXISTS t_chat_room;

CREATE TABLE t_chat_room (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '聊天室名称',
    description VARCHAR(500) COMMENT '聊天室描述',
    cover VARCHAR(255) COMMENT '聊天室封面',
    course_id BIGINT NOT NULL COMMENT '关联课程ID',
    course_title VARCHAR(100) COMMENT '课程名称',
    creator_id BIGINT NOT NULL COMMENT '创建者ID（讲师）',
    creator_name VARCHAR(50) COMMENT '创建者名称',
    creator_avatar VARCHAR(255) COMMENT '创建者头像',
    member_count INT DEFAULT 0 COMMENT '成员数量',
    max_members INT DEFAULT 0 COMMENT '最大成员数（0表示不限制）',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    need_approval TINYINT DEFAULT 0 COMMENT '是否需要审批加入 0-否 1-是',
    announcement TEXT COMMENT '公告',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    UNIQUE KEY uk_course (course_id),
    INDEX idx_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天室表';

-- 聊天室成员表
CREATE TABLE t_chat_member (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    room_id BIGINT NOT NULL COMMENT '聊天室ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    nickname VARCHAR(50) COMMENT '用户昵称',
    avatar VARCHAR(255) COMMENT '用户头像',
    role TINYINT DEFAULT 1 COMMENT '角色 1-普通成员 2-管理员 3-创建者',
    status TINYINT DEFAULT 1 COMMENT '状态 0-已退出 1-正常 2-已禁言',
    join_time DATETIME COMMENT '加入时间',
    last_active_time DATETIME COMMENT '最后活跃时间',
    unread_count INT DEFAULT 0 COMMENT '未读消息数',
    is_pinned TINYINT DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
    is_muted TINYINT DEFAULT 0 COMMENT '是否免打扰 0-否 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    UNIQUE KEY uk_room_user (room_id, user_id),
    INDEX idx_room (room_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天室成员表';

-- 聊天室邀请表
CREATE TABLE t_chat_invitation (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    room_id BIGINT NOT NULL COMMENT '聊天室ID',
    room_name VARCHAR(100) COMMENT '聊天室名称',
    invitee_id BIGINT NOT NULL COMMENT '被邀请用户ID',
    inviter_id BIGINT NOT NULL COMMENT '邀请人ID',
    inviter_name VARCHAR(50) COMMENT '邀请人名称',
    inviter_avatar VARCHAR(255) COMMENT '邀请人头像',
    status TINYINT DEFAULT 0 COMMENT '状态 0-待处理 1-已接受 2-已拒绝 3-已过期',
    message VARCHAR(200) COMMENT '邀请消息',
    expire_time DATETIME COMMENT '过期时间',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_room (room_id),
    INDEX idx_invitee (invitee_id),
    INDEX idx_inviter (inviter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天室邀请表';

-- 聊天消息表
CREATE TABLE t_chat_message (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    room_id BIGINT NOT NULL COMMENT '聊天室ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    sender_name VARCHAR(50) COMMENT '发送者昵称',
    sender_avatar VARCHAR(255) COMMENT '发送者头像',
    sender_role TINYINT COMMENT '发送者角色 1-学员 2-讲师',
    message_type TINYINT DEFAULT 1 COMMENT '消息类型 1-文本 2-图片 3-文件 4-系统消息',
    content TEXT COMMENT '消息内容',
    attachment_url VARCHAR(500) COMMENT '附件URL（图片/文件）',
    attachment_name VARCHAR(200) COMMENT '附件名称',
    attachment_size BIGINT COMMENT '附件大小（字节）',
    reply_to_id BIGINT COMMENT '回复的消息ID',
    status TINYINT DEFAULT 1 COMMENT '状态 0-已撤回 1-正常',
    mentioned_user_ids VARCHAR(500) COMMENT '@提及的用户ID列表（逗号分隔）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_room (room_id),
    INDEX idx_sender (sender_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';


ALTER TABLE t_order ADD COLUMN teacher_income DECIMAL(10,2) COMMENT '讲师分成' AFTER org_income;