package com.steve.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steve.cloudpicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.steve.cloudpicturebackend.model.dto.picture.*;
import com.steve.cloudpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steve.cloudpicturebackend.model.entity.User;
import com.steve.cloudpicturebackend.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface PictureService extends IService<Picture> {

    void validPicture(Picture picture);

    /**
     * 上传图片
     * @param inputSource 输入源
     * @param pictureUploadRequest   上传请求
     * @param loginUser     登录用户
     * @return
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 上传头像
     */
    PictureVO uploadAvatar(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取图片包装类（单条）
     * @param picture 图片
     * @param request 请求
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类（分页）
     * @param picturePage 图片页
     * @param request     请求
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest 图片查询请求
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     * @param picture   图片
     * @param loginUser  登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量上传图片
     * @param pictureUploadByBatchRequest 图片批量上传请求
     * @param loginUser   登录用户
     * @return 成功创建的图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 删除图片文件
     * @param oldPicture 旧图片
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 删除图片
     * @param pictureId 图片id
     * @param loginUser 登录用户
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片
     * @param pictureEditRequest 图片编辑请求
     * @param loginUser           登录用户
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 校验图片空间的权限
     * @param loginUser    登录用户
     * @param picture      图片
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId   空间id
     * @param picColor  颜色
     * @param loginUser 登录用户
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑图片
     *
     * @param pictureEditByBatchRequest 图片批量编辑请求
     * @param loginUser                 登录用户
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建扩图任务
     *
     * @param createPictureOutPaintingTaskRequest 扩图任务请求
     * @param loginUser                           登录用户
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);

    /**
     * 下载图片
     *
     * @param pictureDownLoadRequest 图片下载请求
     * @param loginUser          登录用户
     */
    PictureVO downloadPicture(PictureDownLoadRequest pictureDownLoadRequest, User loginUser);

    /**
     * 分享图片
     *
     * @param pictureShareRequest 图片分享请求
     * @param loginUser           登录用户
     */
    PictureVO sharePicture(PictureShareRequest pictureShareRequest, User loginUser);
}