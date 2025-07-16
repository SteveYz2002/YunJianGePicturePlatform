package com.steve.cloudpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图片点赞
 * @TableName picture_like
 */
@TableName(value = "picture_like")
@Data
public class PictureLike implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 点赞时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
} 