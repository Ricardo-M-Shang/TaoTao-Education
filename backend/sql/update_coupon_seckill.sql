ALTER TABLE `t_coupon` ADD COLUMN `grab_start_time` datetime DEFAULT NULL COMMENT '抢购开始时间';
ALTER TABLE `t_coupon` ADD COLUMN `grab_end_time` datetime DEFAULT NULL COMMENT '抢购结束时间';
