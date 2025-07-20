package com.steve.cloudpicturebackend.model.dto.picture;

import com.steve.cloudpicturebackend.api.aliyunai.txt2img.model.CreateTextToImageTaskRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class CreatePictureTextToImageTaskRequest implements Serializable {

    /**
     * 提示词
     */
    private String prompt;
    
    /**
     * 负面提示词
     */
    private String negativePrompt;
    
    /**
     * 文生图参数
     */
    private CreateTextToImageTaskRequest.Parameters parameters;

    /**
     * 空间ID，用于保存生成的图片到特定空间
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
} 