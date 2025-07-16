package com.steve.cloudpicturebackend.model.dto.picture;

import lombok.Data;

/**
 * 图片点赞相关的请求
 */
@Data
public class PictureLikeOrUnlikeRequest {

    private Long pictureId;
}
