package com.steve.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steve.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.steve.cloudpicturebackend.model.dto.space.SpaceQueryRequest;
import com.steve.cloudpicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steve.cloudpicturebackend.model.entity.User;
import com.steve.cloudpicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-06-22 19:28:29
 */
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     *
     * @param spaceAddRequest 空间创建请求
     * @param user            用户
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User user);

    /**
     * 验证空间
     *
     * @param space 空间
     * @param add   是否为创建时校验
     */
    void validSpace(Space space, boolean add);


    /**
     * 获取空间包装类（单条）
     *
     * @param space   空间
     * @param request 请求
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间包装类（分页）
     *
     * @param spacePage 空间分页
     * @param request   请求
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取查询对象
     *
     * @param spaceQueryRequest 空间查询请求
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间级别填充空间对象
     *
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验用户对空间的权限
     *
     * @param loginUser 登录用户
     * @param space     空间
     */
    void checkSpaceAuth(User loginUser, Space space);

}
