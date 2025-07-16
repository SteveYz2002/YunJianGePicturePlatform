package com.steve.cloudpicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steve.cloudpicturebackend.model.entity.PictureLike;
import com.steve.cloudpicturebackend.mapper.PictureLikeMapper;
import com.steve.cloudpicturebackend.service.PictureLikeService;
import org.springframework.stereotype.Service;

/**
 * 针对表【picture_like】的数据库操作Service实现
 */
@Service
public class PictureLikeServiceImpl extends ServiceImpl<PictureLikeMapper, PictureLike> implements PictureLikeService {
} 