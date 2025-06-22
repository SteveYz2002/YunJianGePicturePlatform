package com.steve.cloudpicturebackend.manager.upload;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.steve.cloudpicturebackend.config.CosClientConfig;
import com.steve.cloudpicturebackend.exception.BusinessException;
import com.steve.cloudpicturebackend.exception.ErrorCode;
import com.steve.cloudpicturebackend.manager.CosManager;
import com.steve.cloudpicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 图片上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected CosManager cosManager;

    @Resource
    protected CosClientConfig cosClientConfig;

    /**
     * 模板方法，定义上传流程
     */
    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片
        String contentType = validPicture(inputSource);

        // 2. 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginFilename(inputSource);
        String uploadFilename ;
        if(!contentType.isEmpty()){
            uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                    contentType);
        }else{
            uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                    FileUtil.getSuffix(originFilename));
        }
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        File file = null;
        try {
            // 3. 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源（本地或 URL）
            processFile(inputSource, file);
            // 4. 上传图片到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 5. 获取图片信息，封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 获取图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if(CollectionUtil.isNotEmpty(objectList)) {
                // 获取压缩后的图片信息
                CIObject compressedCIObject = objectList.get(0);
                // 缩略图默认等于压缩图
                CIObject thumbnailCIObject = compressedCIObject;
                if (objectList.size() > 1) {
                    thumbnailCIObject = objectList.get(1);
                }
                // 封装返回结果
                return buildResult(originFilename, compressedCIObject, thumbnailCIObject);
            }
            return buildResult(originFilename, file, uploadPath, imageInfo);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 删除原图，只保留webp图
            cosManager.deleteObject(uploadPath);
            // 6. 清理临时文件
            this.deleteTempFile(file);
        }
    }

    /**
     * 封装返回结果
     * @param originFilename 原始文件名
     * @param compressedCIObject 压缩后的对象
     * @param thumbnailCIObject 缩略图对象
     */
    private UploadPictureResult buildResult(String originFilename, CIObject compressedCIObject, CIObject thumbnailCIObject) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = compressedCIObject.getWidth();
        int picHeight = compressedCIObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.getName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCIObject.getFormat());
        uploadPictureResult.setPicSize(compressedCIObject.getSize().longValue());
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCIObject.getKey());
        // 设置缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" +thumbnailCIObject.getKey());
        return uploadPictureResult;
    }

    /**
     * 校验输入源（本地文件或 URL）
     */
    protected abstract String validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     */
    protected abstract String getOriginFilename(Object inputSource);


    /**
     * 处理输入源并生成本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     * 封装返回结果
     * @param originFilename 原始文件名
     * @param file 临时文件
     * @param uploadPath 上传路径
     * @param imageInfo 图片信息
     */
    private UploadPictureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.getName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        return uploadPictureResult;
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

}

