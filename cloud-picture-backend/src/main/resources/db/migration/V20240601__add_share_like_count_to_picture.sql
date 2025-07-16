-- 为Picture表添加分享计数和点赞计数字段
ALTER TABLE picture
    ADD COLUMN downloadCount INT DEFAULT 0 COMMENT '下载次数' AFTER reviewTime;
ALTER TABLE picture
    ADD COLUMN shareCount INT DEFAULT 0 COMMENT '分享次数' AFTER downloadCount;

-- 更新已有记录，将NULL值设置为0
UPDATE picture
SET downloadCount = 0
WHERE downloadCount IS NULL;
UPDATE picture
SET shareCount = 0;
WHERE shareCount IS NULL;
