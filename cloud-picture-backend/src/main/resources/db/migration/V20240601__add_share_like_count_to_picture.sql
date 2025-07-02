-- 为Picture表添加分享计数和点赞计数字段
ALTER TABLE picture
    ADD COLUMN downloadCount INT DEFAULT 0 COMMENT '分享次数' AFTER reviewTime;

-- 更新已有记录，将NULL值设置为0
UPDATE picture
SET downloadCount = 0
WHERE downloadCount IS NULL;
