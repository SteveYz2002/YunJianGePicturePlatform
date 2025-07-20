package com.steve.cloudpicturebackend.api.aliyunai.txt2img.model;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 创建文生图任务请求
 */
@Data
public class CreateTextToImageTaskRequest implements Serializable {

    /**
     * 模型，例如 "wanx-v1"
     */
    private String model = "wanx-v1";

    /**
     * 输入内容信息
     */
    private Input input;

    /**
     * 图像生成参数
     */
    private Parameters parameters;

    @Data
    public static class Input {
        /**
         * 必选，文本提示词
         */
        private String prompt;
        
        /**
         * 可选，负面提示词，指定不希望在图像中出现的内容
         */
        @Alias("negative_prompt")
        private String negativePrompt;
    }

    @Data
    public static class Parameters implements Serializable {
        /**
         * 可选，控制随机种子，相同的种子和其他参数会生成相同的图像
         */
        private Long seed;

        /**
         * 可选，图像尺寸
         * 可选值：["1024*1024", "720*1280", "1280*720", "768*768"]
         * 默认值："1024*1024"
         */
        private String size;

        /**
         * 可选，控制生成图像和提示词的匹配程度
         * 范围 [1, 100]，默认值 10
         * 数值越大，生成图像和提示词的匹配度越高
         */
        @Alias("guidance_scale")
        private Integer guidanceScale;

        /**
         * 可选，推理步数
         * 范围 [1, 50]，默认值 30
         * 数值越大生成图像质量越高，但是耗时也越长
         */
        private Integer steps;

        /**
         * 可选，返回图像的数量
         * 范围 [1, 4]，默认值 1
         */
        @Alias("n")
        private Integer n;

        /**
         * 可选，是否添加水印
         * 默认值 false
         */
        @Alias("add_watermark")
        private Boolean addWatermark = false;
    }
} 