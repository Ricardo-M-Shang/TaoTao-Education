ALTER TABLE `t_coupon` ADD COLUMN `grab_start_time` datetime DEFAULT NULL COMMENT '抢购开始时间';
ALTER TABLE `t_coupon` ADD COLUMN `grab_end_time` datetime DEFAULT NULL COMMENT '抢购结束时间';

-- 1) 是否写入学习明细
SELECT user_id, course_id, lesson_id, study_duration, last_position, study_date, update_time
FROM t_learning_record
WHERE user_id = 2047271640153657345
ORDER BY update_time DESC
LIMIT 10;
-- 2) 是否生成课程聚合
SELECT user_id, course_id, study_duration, completed_lessons, study_days, completion_rate, update_time
FROM t_learning_stats
WHERE user_id = 2047271640153657345
ORDER BY update_time DESC
LIMIT 10;