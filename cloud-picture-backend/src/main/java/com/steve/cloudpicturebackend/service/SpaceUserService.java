package com.steve.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steve.cloudpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.steve.cloudpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.steve.cloudpicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steve.cloudpicturebackend.model.entity.User;
import com.steve.cloudpicturebackend.model.vo.SpaceUserVO;
import com.steve.cloudpicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Administrator
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2025-06-27 22:03:29
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 创建空间成员
     *
     * @param spaceUserAddRequest 空间成员创建请求
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 验证空间成员
     *
     * @param spaceUser 空间成员
     * @param add       是否为创建时校验
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);


    /**
     * 获取空间成员包装类（单条）
     *
     * @param spaceUser 空间成员
     * @param request   请求
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员包装类（列表）
     *
     * @param spaceUserList 空间列表
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 获取查询对象
     *
     * @param spaceUserQueryRequest 空间成员查询请求
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);


}
