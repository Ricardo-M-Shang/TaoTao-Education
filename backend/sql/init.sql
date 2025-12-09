-- 创建数据库
CREATE DATABASE IF NOT EXISTS taotao_education DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE taotao_education;

-- =============================================
-- 用户表
-- =============================================
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
    role TINYINT DEFAULT 1 COMMENT '用户角色 1-学员 2-讲师 3-机构管理员 4-运营管理员',
    status TINYINT DEFAULT 1 COMMENT '账号状态 0-禁用 1-正常',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 课程分类表
-- =============================================
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

-- =============================================
-- 课程表
-- =============================================
CREATE TABLE IF NOT EXISTS t_course (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(100) NOT NULL COMMENT '课程标题',
    subtitle VARCHAR(200) COMMENT '课程副标题',
    cover VARCHAR(255) COMMENT '课程封面图片',
    description TEXT COMMENT '课程简介',
    content LONGTEXT COMMENT '课程详情（富文本）',
    teacher_id BIGINT COMMENT '讲师ID',
    teacher_name VARCHAR(50) COMMENT '讲师名称',
    category_id BIGINT COMMENT '分类ID',
    category_name VARCHAR(50) COMMENT '分类名称',
    type TINYINT DEFAULT 1 COMMENT '课程类型 1-录播 2-直播 3-图文',
    price DECIMAL(10, 2) DEFAULT 0.00 COMMENT '课程价格',
    original_price DECIMAL(10, 2) COMMENT '原价',
    is_free TINYINT DEFAULT 0 COMMENT '是否免费 0-收费 1-免费',
    status TINYINT DEFAULT 0 COMMENT '课程状态 0-草稿 1-待审核 2-已发布 3-已下架',
    lesson_count INT DEFAULT 0 COMMENT '课时数',
    study_count INT DEFAULT 0 COMMENT '学习人数',
    score DECIMAL(2, 1) DEFAULT 0.0 COMMENT '评分',
    total_duration BIGINT DEFAULT 0 COMMENT '总时长（秒）',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- =============================================
-- 章节表
-- =============================================
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

-- =============================================
-- 课时表
-- =============================================
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

-- =============================================
-- 订单表
-- =============================================
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    course_title VARCHAR(100) COMMENT '课程标题',
    course_cover VARCHAR(255) COMMENT '课程封面',
    teacher_name VARCHAR(50) COMMENT '讲师名称',
    original_price DECIMAL(10, 2) COMMENT '订单原价',
    discount_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '优惠金额',
    pay_amount DECIMAL(10, 2) NOT NULL COMMENT '实付金额',
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
    INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =============================================
-- 用户课程关联表
-- =============================================
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

-- =============================================
-- 课程评价表
-- =============================================
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

-- =============================================
-- 课程收藏表
-- =============================================
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

-- =============================================
-- 学习记录表
-- =============================================
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

-- =============================================
-- 学习笔记表
-- =============================================
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
-- 初始化数据
-- =============================================

-- 插入测试用户（密码：123456）
INSERT IGNORE INTO t_user (id, username, password, nickname, role, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKqxNbq2D7zvwLkWMb9YDQ8oq8gy', '管理员', 4, 1),
(2, 'teacher', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKqxNbq2D7zvwLkWMb9YDQ8oq8gy', '张老师', 2, 1),
(3, 'student', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKqxNbq2D7zvwLkWMb9YDQ8oq8gy', '李同学', 1, 1);

-- 插入课程分类
INSERT IGNORE INTO t_category (id, name, parent_id, level, sort, status) VALUES
(1, '前端开发', 0, 1, 1, 1),
(2, '后端开发', 0, 1, 2, 1),
(3, '移动开发', 0, 1, 3, 1),
(4, '人工智能', 0, 1, 4, 1),
(5, 'Vue.js', 1, 2, 1, 1),
(6, 'React', 1, 2, 2, 1),
(7, 'Java', 2, 2, 1, 1),
(8, 'Python', 2, 2, 2, 1),
(9, 'Spring Boot', 7, 3, 1, 1),
(10, 'Spring Cloud', 7, 3, 2, 1);

-- 插入示例课程
INSERT IGNORE INTO t_course (id, title, subtitle, description, teacher_id, teacher_name, category_id, category_name, type, price, original_price, is_free, status, lesson_count, study_count, score) VALUES
(1, 'Vue3从入门到精通', '2024最新Vue3全家桶实战教程', '本课程将带你从零开始学习Vue3，掌握Composition API、Vue Router、Pinia等核心技术', 2, '张老师', 5, 'Vue.js', 1, 199.00, 299.00, 0, 2, 30, 1500, 4.8),
(2, 'Spring Boot实战', '企业级Spring Boot项目开发', '从基础到进阶，全面掌握Spring Boot企业级开发', 2, '张老师', 9, 'Spring Boot', 1, 299.00, 399.00, 0, 2, 45, 2000, 4.9),
(3, 'Python入门教程', '零基础学Python', 'Python基础语法、数据结构、函数、面向对象编程', 2, '张老师', 8, 'Python', 1, 0.00, 0.00, 1, 2, 20, 5000, 4.7);

-- 插入章节
INSERT IGNORE INTO t_chapter (id, course_id, title, sort) VALUES
(1, 1, '第一章：Vue3基础入门', 1),
(2, 1, '第二章：组合式API详解', 2),
(3, 1, '第三章：Vue Router路由', 3),
(4, 2, '第一章：Spring Boot入门', 1),
(5, 2, '第二章：Web开发基础', 2);

-- 插入课时
INSERT IGNORE INTO t_lesson (id, course_id, chapter_id, title, duration, is_free, sort, type) VALUES
(1, 1, 1, '1.1 课程介绍', 600, 1, 1, 1),
(2, 1, 1, '1.2 开发环境搭建', 900, 1, 2, 1),
(3, 1, 1, '1.3 创建第一个Vue3项目', 1200, 0, 3, 1),
(4, 1, 2, '2.1 ref和reactive', 1500, 0, 1, 1),
(5, 1, 2, '2.2 computed和watch', 1200, 0, 2, 1),
(6, 2, 4, '1.1 Spring Boot简介', 800, 1, 1, 1),
(7, 2, 4, '1.2 快速开始', 1000, 0, 2, 1);

-- 给t_order表添加teacher_id字段（与原建表语句属性一致）
ALTER TABLE t_order
    ADD COLUMN teacher_id BIGINT COMMENT '讲师ID' AFTER id; -- AFTER id 指定字段位置（可选，建议和原结构一致）