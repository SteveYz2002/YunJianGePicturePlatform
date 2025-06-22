package com.steve.cloudpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.steve.cloudpicturebackend.exception.BusinessException;
import com.steve.cloudpicturebackend.exception.ErrorCode;
import com.steve.cloudpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * 图片上传（url）
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {

    @Override
    protected String validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        String contentType = "";
        // 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");
        // 校验URL格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式错误");
        }
        // 校验URL的协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "仅支持http和https协议");
        // 发送HEAD请求验证文件是否存在
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl)
                    .execute();
            // 未正常返回，无需执行其他判断
            if (httpResponse.getStatus()!= HttpStatus.HTTP_OK) {
                return contentType;
            }
            // 文件存在，文件类型校验
            contentType = httpResponse.header("Content-Type");
            // 不为空才校验
            if (StrUtil.isNotBlank(contentType)) {
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            switch (contentType) {
                case "image/jpg":
                    contentType = "jpg";
                    break;
                case "image/png":
                    contentType = "png";
                    break;
                case "image/webp":
                    contentType = "webp";
                    break;
                default:
                    contentType = "jpg";
                    break;
            }
            // 文件大小校验
            String contentLength = httpResponse.header("Content-Length");
            if (StrUtil.isBlank(contentLength)) {
                try {
                    final long ONE_M = 1024 * 1024;
                    ThrowUtils.throwIf(Long.parseLong(contentLength) > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
                }catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        }finally {
            // 记得释放资源
            if (httpResponse != null) {
                httpResponse.close();
            }

        }
        return contentType;
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 从 URL 中提取文件名
        return FileUtil.mainName(fileUrl);
    }



    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        // 下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }
}

