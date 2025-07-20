package com.steve.cloudpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steve.cloudpicturebackend.exception.BusinessException;
import com.steve.cloudpicturebackend.exception.ErrorCode;
import com.steve.cloudpicturebackend.exception.ThrowUtils;
import com.steve.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.steve.cloudpicturebackend.model.dto.space.SpaceQueryRequest;
import com.steve.cloudpicturebackend.model.entity.Space;
import com.steve.cloudpicturebackend.model.entity.SpaceUser;
import com.steve.cloudpicturebackend.model.entity.User;
import com.steve.cloudpicturebackend.model.enums.SpaceLevelEnum;
import com.steve.cloudpicturebackend.model.enums.SpaceRoleEnum;
import com.steve.cloudpicturebackend.model.enums.SpaceTypeEnum;
import com.steve.cloudpicturebackend.model.vo.SpaceVO;
import com.steve.cloudpicturebackend.model.vo.UserVO;
import com.steve.cloudpicturebackend.service.SpaceService;
import com.steve.cloudpicturebackend.mapper.SpaceMapper;
import com.steve.cloudpicturebackend.service.SpaceUserService;
import com.steve.cloudpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-06-22 19:28:29
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    Map<Long, Object> lockMap = new ConcurrentHashMap<>();

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserService spaceUserService;

//    @Resource
//    @Lazy
//    private DynamicShardingManager dynamicShardingManager;

    /**
     * 创建空间
     *
     * @param spaceAddRequest 空间创建请求
     * @param loginUser       登录用户
     * @return 空间id
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1. 填充参数默认值
        // 转换实体类和DTO
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        if (StrUtil.isBlank(spaceAddRequest.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        this.fillSpaceBySpaceLevel(space);
        // 2. 校验参数
        this.validSpace(space, true);
        // 3. 校验权限，非管理员只能创建普通级别的空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "非管理员只能创建普通级别的空间");
        }
        // 4. 控制同一用户只能创建一个私有空间，以及一个团队空间
        Object lock = lockMap.computeIfAbsent(userId, key -> new Object());
        synchronized (lock) {
            try {
                Long newSpaceId = transactionTemplate.execute(status -> {
                    // 查询是否已经创建了空间
                    boolean exists = this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            .eq(Space::getSpaceType, space.getSpaceType())
                            .exists();
                    // 如果有就不能再创建
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "该用户已经创建了该类型的空间");
                    // 创建
                    boolean result = this.save(space);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建空间失败");
                    // 创建成功后，如果是团队空间，关联新增团队成员记录
                    if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
                        SpaceUser spaceUser = new SpaceUser();
                        spaceUser.setSpaceId(space.getId());
                        spaceUser.setUserId(userId);
                        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                        result = spaceUserService.save(spaceUser);
                        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                    }
//                    // 创建分表，仅对团队空间生效（暂时不使用）
//                    dynamicShardingManager.createSpacePictureTable(space);
                    // 返回空间ID
                    return space.getId();
                });
                return Optional.ofNullable(newSpaceId).orElse(-1L);
            } finally {
                lockMap.remove(userId);
            }
        }
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer spaceType = space.getSpaceType();
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        // 创建时校验
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceLevelEnum == null, ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            ThrowUtils.throwIf(spaceType == null, ErrorCode.PARAMS_ERROR, "空间类别不能为空");
        }
        // 修改数据时，空间名称进行校验
        if (StrUtil.isNotBlank(spaceName)) {
            ThrowUtils.throwIf(spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        // 修改数据时，空间级别进行校验
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new RuntimeException("空间级别不存在");
        }
        // 修改数据时，空间类别进行校验
        if (spaceType != null && spaceTypeEnum == null) {
            throw new RuntimeException("空间类别不存在");
        }
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 未登录，不能操作空间
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 空间不存在
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 管理员可以操作任意空间
        if (userService.isAdmin(loginUser)) {
            return;
        }
        // 私有空间只能创建者自己操作
        if (SpaceTypeEnum.PRIVATE.getValue() == space.getSpaceType()) {
            ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "无权限操作该空间");
            return;
        }
        // 团队空间，需要校验是否是团队空间的成员
        if (SpaceTypeEnum.TEAM.getValue() == space.getSpaceType()) {
            // 查询团队成员
            boolean exists = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, space.getId())
                    .eq(SpaceUser::getUserId, loginUser.getId())
                    .exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NO_AUTH_ERROR, "无权限操作该空间");
        }
    }

    /**
     * 根据用户ID获取该用户的空间列表（VO）
     *
     * @param userId 用户ID
     * @return 该用户的空间列表
     */
    @Override
    public List<SpaceVO> listSpaceVOByUserId(Long userId) {
        // 校验参数
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR);
        
        // 获取该用户创建的私有空间
        List<Space> privateSpaces = this.lambdaQuery()
                .eq(Space::getUserId, userId)
                .eq(Space::getSpaceType, SpaceTypeEnum.PRIVATE.getValue())
                .list();
        
        // 获取该用户所在的团队空间
        List<SpaceUser> spaceUsers = spaceUserService.lambdaQuery()
                .eq(SpaceUser::getUserId, userId)
                .list();
        
        Set<Long> teamSpaceIds = spaceUsers.stream()
                .map(SpaceUser::getSpaceId)
                .collect(Collectors.toSet());
        
        List<Space> teamSpaces = new ArrayList<>();
        if (!teamSpaceIds.isEmpty()) {
            teamSpaces = this.lambdaQuery()
                    .in(Space::getId, teamSpaceIds)
                    .eq(Space::getSpaceType, SpaceTypeEnum.TEAM.getValue())
                    .list();
        }
        
        // 合并两种空间并转换为VO
        List<Space> allSpaces = new ArrayList<>();
        allSpaces.addAll(privateSpaces);
        allSpaces.addAll(teamSpaces);
        
        // 转换为VO对象
        List<SpaceVO> spaceVOList = allSpaces.stream()
                .map(space -> {
                    SpaceVO spaceVO = SpaceVO.objToVo(space);
                    // 获取空间拥有者信息
                    User owner = userService.getById(space.getUserId());
                    if (owner != null) {
                        spaceVO.setUser(userService.getUserVO(owner));
                    }
                    return spaceVO;
                })
                .collect(Collectors.toList());
        
        return spaceVOList;
    }
}




