package com.steve.cloudpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片点赞请求
 *
 * @author steve
 */
@Data
public class PictureDownLoadRequest implements Serializable {

    /**
     * 图片id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
