package com.imooc.mall.util;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import java.io.IOException;

//图片工具类
public class ImageUtil {
    public static void main(String[] args) throws IOException {
        String path="C:\\Users\\岳\\Desktop\\";
        //切割
        Thumbnails.of(path+"OIP-C.jpg").sourceRegion(Positions.BOTTOM_RIGHT,50,5).size(50,50)
                .toFile(path+"crop.jpg");
        //缩放(按比例)
        Thumbnails.of(path+"OIP-C.jpg").scale(0.7).toFile(path+"scale1.jpg");
        //缩放（按长宽）keepAspectRatio(false)->是否按比例缩放
        Thumbnails.of(path+"OIP -C.jpg").size(500,500).keepAspectRatio(false).toFile(path+"scale2.jpg");

    }
}
