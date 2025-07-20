package com.steve.cloudpicturebackend.api.aliyunai.txt2img;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.steve.cloudpicturebackend.api.aliyunai.txt2img.model.CreateTextToImageTaskRequest;
import com.steve.cloudpicturebackend.api.aliyunai.txt2img.model.CreateTextToImageTaskResponse;
import com.steve.cloudpicturebackend.api.aliyunai.txt2img.model.GetTextToImageTaskResponse;
import com.steve.cloudpicturebackend.exception.BusinessException;
import com.steve.cloudpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


@Slf4j
@Component
public class TxtToImgAliYunAiApi {

    // 读取配置文件
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 创建文生图任务地址
    public static final String CREATE_TEXT_TO_IMAGE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";

    // 查询任务状态
    public static final String GET_TEXT_TO_IMAGE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";


    /**
     * 创建文生图任务
     *
     * @param createTextToImageTaskRequest
     * @return
     */
    public CreateTextToImageTaskResponse createTextToImageTask(CreateTextToImageTaskRequest createTextToImageTaskRequest) {
        if (createTextToImageTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文生图参数为空");
        }
        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_TEXT_TO_IMAGE_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                // 必须开启异步处理
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createTextToImageTaskRequest));
        // 处理响应
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 文生图失败");
            }
            CreateTextToImageTaskResponse createTextToImageTaskResponse = JSONUtil.toBean(httpResponse.body(), CreateTextToImageTaskResponse.class);
            if (createTextToImageTaskResponse.getCode() != null) {
                String errorMessage = createTextToImageTaskResponse.getMessage();
                log.error("请求异常：{}", errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 文生图失败，" + errorMessage);
            }
            return createTextToImageTaskResponse;
        }
    }

    /**
     * 查询创建的任务结果
     *
     * @param taskId
     * @return
     */
    public GetTextToImageTaskResponse getTextToImageTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 ID 不能为空");
        }
        // 处理响应
        String url = String.format(GET_TEXT_TO_IMAGE_TASK_URL, taskId);
        try (HttpResponse httpResponse = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetTextToImageTaskResponse.class);
        }
    }
} 