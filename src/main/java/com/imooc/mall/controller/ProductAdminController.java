package com.imooc.mall.controller;

import com.github.pagehelper.PageInfo;
import com.imooc.mall.common.ApiRestResponse;
import com.imooc.mall.common.Constant;
import com.imooc.mall.common.ValidList;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.mapper.ProductMapper;
import com.imooc.mall.model.pojo.Product;
import com.imooc.mall.model.request.AddProductReq;
import com.imooc.mall.model.request.UpdateProductReq;
import com.imooc.mall.service.ProductService;
import io.swagger.annotations.ApiOperation;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Controller
@Validated
public class ProductAdminController {
    @Resource
    private ProductService productService;
    @Value("${file.upload.uri}")
    String uri;
    @PostMapping("admin/product/add")
    @ResponseBody
    public ApiRestResponse addProduct(@Valid @RequestBody AddProductReq addProductReq){
       productService.add(addProductReq);
       return ApiRestResponse.success();
    }
    @PostMapping("admin/upload/file")
    @ResponseBody
    public ApiRestResponse upload(HttpServletRequest httpServletRequest, @RequestParam MultipartFile file){
        //file.getOriginalFilename()获取原始名字
        String fileName = file.getOriginalFilename();
        //截取最后一个点的后面内容，其实就是后缀
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //生成文件名UUID
        UUID uuid = UUID.randomUUID();
        String newFileName = uuid.toString() + suffixName;
        //创建文件
        File fileDirectory = new File(Constant.FILE_UPLOAD_DTR);//文件夹
        File destFile = new File(Constant.FILE_UPLOAD_DTR + newFileName);
        createFile(fileDirectory, file, destFile);
        String address=uri;
            return ApiRestResponse.success("http://"+address+"/images/"+
                    newFileName);


    }
    private URI getHost(URI uri){
        URI effectivURI;
        try {
            effectivURI=new URI(uri.getScheme(),uri.getUserInfo(),
                    uri.getHost(), uri.getPort(),null,null,null );
        } catch (URISyntaxException e) {
            effectivURI=null;
        }
        return effectivURI;
    }
    @ApiOperation("后台更新商品")
    @PostMapping("/admin/product/update")
    @ResponseBody
    public ApiRestResponse updateProduct(@Valid @RequestBody UpdateProductReq updateProductReq) {
        Product product=new Product();
        BeanUtils.copyProperties(updateProductReq,product);
        productService.update(product);
        return ApiRestResponse.success();
    }
    @ApiOperation("后台删除商品")
    @PostMapping("/admin/product/delete")
    @ResponseBody
    public ApiRestResponse deleteProduct(Integer id) {
        productService.deleteProduct(id);
        return ApiRestResponse.success();
    }
    @ApiOperation("批量上下架接口")
    @PostMapping("/admin/product/batchUpdateSellStatus")
    @ResponseBody
    public ApiRestResponse batchUpdateSellStatus(@RequestParam Integer[] ids,@RequestParam Integer sellStatus) {
        productService.batchUpdateSellStatus(ids,sellStatus);
        return ApiRestResponse.success();
    }
    @ApiOperation("后台商品列表接口")
    @PostMapping("/admin/product/list")
    @ResponseBody
    public ApiRestResponse list(@RequestParam Integer pageNum,@RequestParam Integer pageSize) {
        PageInfo pageInfo = productService.listForAdmin(pageNum, pageSize);
        return ApiRestResponse.success(pageInfo);
    }

    @ApiOperation("后台批量上传商品接口")
    @PostMapping("/admin/upload/product")
    @ResponseBody
    public ApiRestResponse uploadProduct(@RequestParam("file")MultipartFile multipartFile) throws IOException {
        //获取名字
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix=originalFilename.substring(originalFilename.lastIndexOf("."));
        UUID uuid=UUID.randomUUID();
        String newFileName=uuid.toString()+suffix;
        //创建文件
        File fileDirectory = new File(Constant.FILE_UPLOAD_DTR);//文件夹
        File destFile = new File(Constant.FILE_UPLOAD_DTR + newFileName);
        createFile(fileDirectory, multipartFile, destFile);
        productService.addProductByExcel(destFile);
        return ApiRestResponse.success();
    }
    @PostMapping("admin/upload/image")
    @ResponseBody
    public ApiRestResponse uploadImage(HttpServletRequest httpServletRequest, @RequestBody MultipartFile file) throws IOException {
        //file.getOriginalFilename()获取原始名字
        String fileName = file.getOriginalFilename();
        //截取最后一个点的后面内容，其实就是后缀
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //生成文件名UUID
        UUID uuid = UUID.randomUUID();
        String newFileName = uuid.toString() + suffixName;
        //创建文件
        File fileDirectory = new File(Constant.FILE_UPLOAD_DTR);//文件夹
        File destFile = new File(Constant.FILE_UPLOAD_DTR + newFileName);
        createFile(fileDirectory, file, destFile);
        String address=uri;
        Thumbnails.of(destFile).size(Constant.IMAGE_SIZE,Constant.IMAGE_SIZE).watermark(Positions.BOTTOM_RIGHT,
                ImageIO.read(new File(Constant.FILE_UPLOAD_DTR+Constant.WATER_MARK_JPG)),Constant.MAGE_OPACITY)
                .toFile(new File(Constant.FILE_UPLOAD_DTR+newFileName));

            return ApiRestResponse.success("http://"+uri+"/images/"+
                    newFileName);

    }

    private void createFile(File fileDirectory, MultipartFile file, File destFile) {
        if(!fileDirectory.exists()){
            if(!fileDirectory.mkdir()){
                throw new ImoocMallException(ImoocMallExceptionEnum.MKDIR_FAILED);
            }
        }
        try {
            file.transferTo(destFile);//将图片写进去
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @ApiOperation("后台批量更新商品")
    @PostMapping("/admin/product/batchUpdateProduct")
    @ResponseBody
    //因为@Valid属于javaBean，不能对属于util的List进行校验
    public ApiRestResponse batchUpdateProduct(@Valid @RequestBody List<UpdateProductReq> updateProductReqs){
//        Product product=new Product();
//        BeanUtils.copyProperties(updateProductReq,product);
//        productService.update(product);
        return ApiRestResponse.success();
    }

}
