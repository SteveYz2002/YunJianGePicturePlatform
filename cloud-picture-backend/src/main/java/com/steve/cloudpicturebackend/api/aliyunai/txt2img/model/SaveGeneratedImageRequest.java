package com.steve.cloudpicturebackend.api.aliyunai.txt2img.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存生成的图片请求
 */
@Data
public class SaveGeneratedImageRequest implements Serializable {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 生成图片使用的提示词
     */
    private String prompt;
    
    /**
     * 保存到的空间ID
     */
    private Long spaceId;
    
    private static final long serialVersionUID = 1L;
} 