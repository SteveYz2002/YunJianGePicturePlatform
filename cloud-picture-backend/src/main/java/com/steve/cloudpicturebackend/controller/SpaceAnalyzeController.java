package com.steve.cloudpicturebackend.controller;

import com.steve.cloudpicturebackend.common.BaseResponse;
import com.steve.cloudpicturebackend.common.ResultUtils;
import com.steve.cloudpicturebackend.exception.ErrorCode;
import com.steve.cloudpicturebackend.exception.ThrowUtils;
import com.steve.cloudpicturebackend.model.dto.space.analyze.*;
import com.steve.cloudpicturebackend.model.entity.Space;
import com.steve.cloudpicturebackend.model.entity.User;
import com.steve.cloudpicturebackend.model.vo.space.analyze.*;
import com.steve.cloudpicturebackend.service.SpaceAnalyzeService;
import com.steve.cloudpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间上传接口
 *
 * @author steve
 */
@Slf4j
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    /**
     * 获取空间图片使用状态
     *
     * @param spaceUsageAnalyzeRequest 空间图片使用分析请求参数
     * @param request                  http请求
     * @return 空间图片使用状态响应
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
                                                                        HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyzeResponse);

    }

    /**
     * 获取空间图片分类分析
     *
     * @param spaceCategoryAnalyzeRequest 空间分类分析请求参数
     * @param request                     http请求
     * @return 空间图片分类分析响应列表
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyzeResponseList = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceCategoryAnalyzeResponseList);
    }


    /**
     * 获取空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest 空间标签分析请求参数
     * @param request                http请求
     * @return 空间图片标签分析响应列表
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(
            @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> spaceTagAnalyzeResponseList = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceTagAnalyzeResponseList);
    }


    /**
     * 获取空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest 空间大小分析请求参数
     * @param request                 http请求
     * @return 空间图片大小分析响应列表
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(
            @RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> spaceSizeAnalyzeResponseList = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceSizeAnalyzeResponseList);
    }


    /**
     * 获取空间用户行为分析
     *
     * @param spaceUserAnalyzeRequest 空间图片用户请求参数
     * @param request                 http请求
     * @return 空间用户行为分析响应列表
     */
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(
            @RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> spaceUserAnalyzeResponseList = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUserAnalyzeResponseList);
    }

    /**
     * 获取空间使用排行分析
     * @param spaceRankAnalyzeRequest 空间使用排行请求参数
     * @param request http请求
     * @return 空间使用排行响应列表
     */
    @PostMapping("/rank")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(
            @RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<Space> spaceList = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceList);
    }
}
