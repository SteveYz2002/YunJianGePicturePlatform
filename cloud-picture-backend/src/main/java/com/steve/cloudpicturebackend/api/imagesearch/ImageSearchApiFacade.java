package com.steve.cloudpicturebackend.api.imagesearch;

import com.steve.cloudpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.steve.cloudpicturebackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.steve.cloudpicturebackend.api.imagesearch.sub.GetImageListApi;
import com.steve.cloudpicturebackend.api.imagesearch.sub.GetImagePageUrlApi;

import java.io.IOException;
import java.util.List;

public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) throws IOException {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) throws IOException {
        List<ImageSearchResult> imageList = searchImage("https://stevecp1-1364043722.cos.ap-beijing.myqcloud.com//public/1932727584688029697/2025-06-22_imEp6mSC96nlfQfm.webp");
        System.out.println("结果列表" + imageList);
    }
}
