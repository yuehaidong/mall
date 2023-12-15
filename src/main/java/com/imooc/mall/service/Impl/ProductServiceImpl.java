package com.imooc.mall.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.mall.common.Constant;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.mapper.ProductMapper;
import com.imooc.mall.model.VO.CategoryVO;
import com.imooc.mall.model.pojo.Product;
import com.imooc.mall.model.query.ProductListQuery;
import com.imooc.mall.model.request.AddProductReq;
import com.imooc.mall.model.request.ProductListReq;
import com.imooc.mall.service.CategoryService;
import com.imooc.mall.service.ProductService;
import com.imooc.mall.util.ExcelUtil;
import com.mysql.cj.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Resource

    private ProductMapper productMapper;
    @Resource
    CategoryService categoryService;
    @Override
    public void add(AddProductReq addProductReq){
        Product product=new Product();
        BeanUtils.copyProperties(addProductReq,product);
        //校验是否重名
        Product productOld = productMapper.selectByName(addProductReq.getName());
        //如果不为空，说明重名
        if(productOld !=null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NAME_EXISTED);
        }
        Integer count = productMapper.insertSelective(product);
        if(count==0){
            throw new ImoocMallException(ImoocMallExceptionEnum.INSERT_FAILED);
        }
    }
    @Override
    public void update(Product updateProduct){
        Product productOld = productMapper.selectByName(updateProduct.getName());
        //同名不同id。不能继续修改
        if(productOld!=null && !productOld.getId().equals(updateProduct.getId())){
            throw new ImoocMallException(ImoocMallExceptionEnum.NAME_EXISTED);
        }
        int count = productMapper.updateByPrimaryKeySelective(updateProduct);
        if(count==0){
            throw new ImoocMallException(ImoocMallExceptionEnum.UPDATE_FAILED);
        }
    }
    @Override
    public void deleteProduct(Integer id){
        Product product = productMapper.selectById(id);
        //查不到该记录
        if(product==null){
            throw new ImoocMallException(ImoocMallExceptionEnum.DELETE_FAILED);
        }
        int count = productMapper.deleteById(id);
        if(count==0){
            throw new ImoocMallException(ImoocMallExceptionEnum.DELETE_FAILED);
        }

    }
    @Override
    public void batchUpdateSellStatus(Integer[] ids, Integer sellStatus){
        productMapper.batchUpdateSellStatus(ids, sellStatus);
    }
    @Override
    public PageInfo<Product> listForAdmin(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Product> products = productMapper.selectListForAdmin();
        PageInfo<Product> pageInfo = new PageInfo<>(products);
        return pageInfo;
    }
    @Override
    public Product detail(Integer id){
        Product product = productMapper.selectById(id);
        return product;
    }
    @Override
    public PageInfo list(ProductListReq productListReq){
        //构建Query对象
        ProductListQuery productListQuery = new ProductListQuery();
        //搜索处理
        if(!StringUtils.isNullOrEmpty(productListReq.getKeyWord())){
            //如果关键字不为空，就为其左右加上%，为了可以使用模糊查找
            System.out.println(productListReq.getKeyWord());
            String keyword=new StringBuilder().append("%").append(productListReq.getKeyWord()).append("%").toString();
            productListQuery.setKeyword(keyword);
        }
        //目录处理，查某个目录下的商品，不仅是需要查出该目录，还要把所有子目录的所有商品都查出来，所以要拿到一个目录id的list
        if(productListReq.getCategoryId() !=null){
            List<CategoryVO> categoryVOSList = categoryService.listCategoryForCustomer(productListReq.getCategoryId());
            ArrayList<Integer> cateGoryIds = new ArrayList<>();
            //因为目录查询是根据parentId来查询，所以查出来的没有包含该parentId，所以要手动添加进去
            cateGoryIds.add(productListReq.getCategoryId());
            getCategoryId(categoryVOSList,cateGoryIds);
            productListQuery.setCategoryIds(cateGoryIds);
        }
        //排序处理
        String orderBy = productListReq.getOrderBy();
        if(Constant.ProductListOrder.PRICE_ASC_Desc.contains(orderBy)){
            PageHelper.startPage(productListReq.getPageNum(),productListReq.getPageSize(),orderBy);
        }else{
            PageHelper.startPage(productListReq.getPageNum(),productListReq.getPageSize());
        }
        List<Product> products = productMapper.selectList(productListQuery);
        PageInfo<Product> pageInfo = new PageInfo<>(products);

        return pageInfo;
    }



    private void getCategoryId(List<CategoryVO> categoryVOList,ArrayList<Integer> categoryIds){
        for (int i = 0; i < categoryVOList.size(); i++) {
            CategoryVO categoryVO = categoryVOList.get(i);
            if(categoryVO!=null){
                categoryIds.add(categoryVO.getId());
                getCategoryId(categoryVO.getChildCategory(),categoryIds);
            }
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProductByExcel(File destFile) throws IOException {
        List<Product> products = readProductFromExcel(destFile);
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            Product productOld = productMapper.selectByName(product.getName());
            if(productOld!=null){
                throw new ImoocMallException(ImoocMallExceptionEnum.NAME_EXISTED);
            }
            Integer count = productMapper.insertSelective(product);
            if(count==0){
                throw new ImoocMallException(ImoocMallExceptionEnum.INSERT_FAILED);
            }

        }
    }
    private List<Product> readProductFromExcel(File excelFile) throws IOException {
        ArrayList<Product> products = new ArrayList<>();
        FileInputStream inputStream = new FileInputStream(excelFile);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet firstSheet = workbook.getSheetAt(0);//获取第一个工作簿
        Iterator<Row> iterator = firstSheet.iterator();//对每行遍历
        while(iterator.hasNext()){
            Row nextRow = iterator.next();
            Iterator<Cell> cellIterator = nextRow.cellIterator();
            Product aproduct = new Product();//保存数据用
            while (cellIterator.hasNext()){
                Cell nextCell = cellIterator.next();
                int columnIndex = nextCell.getColumnIndex();//获取cell在第几列
                switch (columnIndex){
                    case 0:
                        aproduct.setName((String) ExcelUtil.getCellValue(nextCell));
                        break;
                    case 1:
                        aproduct.setImage((String) ExcelUtil.getCellValue(nextCell));
                        break;
                    case 2:
                        aproduct.setDetail((String) ExcelUtil.getCellValue(nextCell));
                        break;
                    case 3:
                        //因为excel数值取出来Double
                        Double cellValue=(Double)ExcelUtil.getCellValue(nextCell);
                        aproduct.setCategoryId(cellValue.intValue());
                        break;
                    case 4:
                        cellValue=(Double)ExcelUtil.getCellValue(nextCell);
                        aproduct.setPrice(cellValue.intValue());
                        break;
                    case 5:
                        cellValue=(Double)ExcelUtil.getCellValue(nextCell);
                        aproduct.setStock(cellValue.intValue());
                        break;
                    case 6:
                        cellValue=(Double)ExcelUtil.getCellValue(nextCell);
                        aproduct.setStatus(cellValue.intValue());
                        break;
                    default:
                        break;
                }
            }
            products.add(aproduct);
        }
        workbook.close();
        inputStream.close();
        return products;

    }
}
