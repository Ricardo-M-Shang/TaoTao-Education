-- 为已有的 t_user 表添加新字段
-- 如果你的数据库已经存在 t_user 表，请执行以下SQL

ALTER TABLE t_user 
ADD COLUMN gender TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女' AFTER avatar,
ADD COLUMN province VARCHAR(50) COMMENT '省份' AFTER gender,
ADD COLUMN city VARCHAR(50) COMMENT '城市' AFTER province,
ADD COLUMN signature VARCHAR(200) COMMENT '个性签名' AFTER city;

SELECT id, username, password FROM t_user WHERE username = 'teacher';

-- 使用新的 BCrypt 哈希重置所有测试用户密码
UPDATE t_user SET password = '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW'
WHERE username IN ('admin', 'teacher', 'student');