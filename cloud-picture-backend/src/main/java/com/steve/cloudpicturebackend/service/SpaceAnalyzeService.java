package com.steve.cloudpicturebackend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.steve.cloudpicturebackend.model.dto.space.analyze.*;
import com.steve.cloudpicturebackend.model.entity.Space;
import com.steve.cloudpicturebackend.model.entity.User;

import com.steve.cloudpicturebackend.model.vo.space.analyze.*;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-06-22 19:28:29
 */
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 获取空间使用情况分析
     *
     * @param spaceUsageAnalyzeRequest 请求参数
     * @param loginUser                登录用户
     * @return 空间使用情况分析响应
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片分类分析
     *
     * @param spaceCategoryAnalyzeRequest 请求参数
     * @param loginUser                   登录用户
     * @return 空间图片分类分析响应列表
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest 请求参数
     * @param loginUser              登录用户
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest 请求参数
     * @param loginUser               登录用户
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 获取空间用户行为分析
     *
     * @param spaceUserAnalyzeRequest 请求参数
     * @param loginUser               登录用户
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 获取空间使用排行分析
     * @param spaceRankAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);

}
