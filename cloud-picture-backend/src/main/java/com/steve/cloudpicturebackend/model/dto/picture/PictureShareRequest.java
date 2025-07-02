package com.steve.cloudpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片分享请求
 *
 * @author steve
 */
@Data
public class PictureShareRequest implements Serializable {

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 分享平台（可选）
     * 例如：wechat, weibo, qq等
     */
    private String platform;

    private static final long serialVersionUID = 1L;
}
