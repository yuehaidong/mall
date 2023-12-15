package com.imooc.mall.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.zxing.WriterException;
import com.imooc.mall.common.Constant;
import com.imooc.mall.exception.ImoocMallException;
import com.imooc.mall.exception.ImoocMallExceptionEnum;
import com.imooc.mall.filter.UserFilter;
import com.imooc.mall.mapper.CartMapper;
import com.imooc.mall.mapper.OrderItemMapper;
import com.imooc.mall.mapper.OrderMapper;
import com.imooc.mall.mapper.ProductMapper;
import com.imooc.mall.model.VO.CartVO;
import com.imooc.mall.model.VO.OrderItemVO;
import com.imooc.mall.model.VO.OrderStatisticsVO;
import com.imooc.mall.model.VO.OrderVO;
import com.imooc.mall.model.pojo.Order;
import com.imooc.mall.model.pojo.OrderItem;
import com.imooc.mall.model.pojo.Product;
import com.imooc.mall.model.pojo.User;
import com.imooc.mall.model.query.OrderStatisticsQuery;
import com.imooc.mall.model.request.CreateOrderReq;
import com.imooc.mall.service.CartService;
import com.imooc.mall.service.OrderService;
import com.imooc.mall.service.UserService;
import com.imooc.mall.util.OrderCodeFactory;
import com.imooc.mall.util.QRCodeGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED,readOnly = true)
public class OrderServiceImpl implements OrderService {
    @Resource
    CartService cartService;
    @Resource
    ProductMapper productMapper;
    @Resource
    CartMapper cartMapper;
    @Resource
    OrderMapper orderMapper;
    @Resource
    OrderItemMapper orderItemMapper;
    @Value("${file.upload.uri}")
    String uri;
    @Resource
    UserService userService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(CreateOrderReq createOrderReq){
         //拿到用户ID
        Integer userId = UserFilter.currentUser.getId();
        // 从购物车查找已经勾选的商品
        ArrayList<CartVO> cartVOListTemp = new ArrayList<>();
        List<CartVO> cartVOList = cartService.list(userId);
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            if(cartVO.getSelected().equals(Constant.Cart.CHECKED)){
                cartVOListTemp.add(cartVO);
            }
        }
       cartVOList = cartVOListTemp;
        //如果购物车以勾选为空，报错
        if(CollectionUtils.isEmpty(cartVOList)){
            throw new ImoocMallException(ImoocMallExceptionEnum.CART_EMPTY);
        }

        //判断商品是否存在，上下架状态，库存
        validSalStatusAndStockt(cartVOList);
        //把购物车对象转为订单item对象
        List<OrderItem> orderItems = cartVOListToOrderItemList(cartVOList);
        //扣库存
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem orderItem = orderItems.get(i);
            Product product = productMapper.selectById(orderItem.getProductId());
            int stock=product.getStock()-orderItem.getQuantity();
            if(stock<0){
                throw new ImoocMallException(ImoocMallExceptionEnum.NOT_ENOUGH);
            }
            product.setStock(stock);
            productMapper.updateByPrimaryKeySelective(product);
        }
        //把购物车中的已勾选商品删除
        cleanCart(cartVOList);
        //生成订单号，有独立的规则
        Order order=new Order();
        String orderCode = OrderCodeFactory.getOrderCode(Long.valueOf(userId));
        order.setOrderNo(orderCode);
        order.setUserId(userId);
        order.setTotalPrice(totalPrice(orderItems));
        order.setReceiverName(createOrderReq.getReceiverName());
        order.setReceiverMobile(createOrderReq.getReceiverMobile());
        order.setReceiverAdress(createOrderReq.getReceiverAddress());
        order.setOrderStatus(Constant.OrderStatuEnum.NOT_PAID.getCode());
        order.setPostage(0);
        order.setPaymentType(1);
        //插入到order表
        orderMapper.insertOrder(order);
        //循环保存每个商品到order_item表
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem orderItem = orderItems.get(i);
            orderItem.setOrderNo(order.getOrderNo());
            orderItemMapper.insert(orderItem);
        }
        //把结果返回
        return order.getOrderNo();
    }
    //判断商品是否存在，上下架状态，库存
    private void validSalStatusAndStockt(List<CartVO> cartVOList){
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            Product product = productMapper.selectById(cartVO.getProductId());
            //判断商品是否存在，是否上架
            if(product==null|| product.getStatus().equals(Constant.SaleStatus.NOT_SALE)){
                throw new ImoocMallException(ImoocMallExceptionEnum.NOT_SALE);
            }
            //判断商品库存
            if(cartVO.getQuantity()>product.getStock()){
                throw new ImoocMallException(ImoocMallExceptionEnum.NOT_ENOUGH);
            }
        }

    }
    // //把购物车对象转为订单item对象
    private List<OrderItem> cartVOListToOrderItemList(List<CartVO> cartVOList){
        ArrayList<OrderItem> orderItemList = new ArrayList<>();
        for (int i = 0; i < cartVOList.size(); i++) {
            CartVO cartVO = cartVOList.get(i);
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartVO.getProductId());
            //记录商品快照信息
            orderItem.setProductName(cartVO.getProductName());
            orderItem.setProductImg(cartVO.getProductImage());
            orderItem.setUnitPrice(cartVO.getPrice());
            orderItem.setQuantity(cartVO.getQuantity());
            orderItem.setTotalPrice(cartVO.getTotalPrice());
            orderItemList.add(orderItem);
        }
        return orderItemList;
    }
    private void cleanCart(List<CartVO> cartVO){
        for (int i = 0; i < cartVO.size(); i++) {
            CartVO cartVO1 = cartVO.get(i);
            cartMapper.deleteById(cartVO1.getId());

        }
    }
    private Integer totalPrice(List<OrderItem> orderItemList){
        Integer totalPrice=0;
        for (int i = 0; i < orderItemList.size(); i++) {
            totalPrice=totalPrice+orderItemList.get(i).getTotalPrice();
        }
        return totalPrice;
    }

    //订单详情
    @Override
    public OrderVO detail(String oderNo){
        Order order = orderMapper.selectByOrderNo(oderNo);
        //订单不存在，则报错
        if(order==null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ORDER);
        }
        //订单存在，需要判断所属
        Integer userId = UserFilter.currentUser.getId();
        if(!order.getUserId().equals(userId)){
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_YOUR_ORDER);
        }
        OrderVO orderVO = getOrderVO(order);
        return orderVO;
    }
    private OrderVO getOrderVO(Order order){
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        //获取订单对应的orderItemVOList
        List<OrderItem> orderItems = orderItemMapper.selectByOrderNo(order.getOrderNo());
        ArrayList<OrderItemVO> orderItemVOS = new ArrayList<>();
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem orderItem = orderItems.get(i);
            OrderItemVO orderItemVO = new OrderItemVO();
            BeanUtils.copyProperties(orderItem,orderItemVO);
            orderItemVOS.add(orderItemVO);
        }
        orderVO.setOrderItemVOList(orderItemVOS);
        orderVO.setOrderStatusName(Constant.OrderStatuEnum.codeOf(orderVO.getOrderStatus()).getValue());
        return orderVO;
    }
    //给用户列表
    @Override
    public PageInfo listForCustpmer(Integer pageNum, Integer pageSize){
        Integer userId = UserFilter.currentUser.getId();
        PageHelper.startPage(pageNum,pageSize);
        List<Order> orders = orderMapper.selectForCustomer(userId);
        List<OrderVO> orderVOS = orderListToOrderVOList(orders);
        //由于构造时必须是查出来的列表，所以后买你要将Listset为OrderVO
        //由于Page本身就是个list可以强转，page和pageInfo的使用主要是看开发文档返回数据
//        Page page=(Page)orders;
        PageInfo orderVOPageInfo = new PageInfo<>(orders);
        orderVOPageInfo.setList(orderVOS);
        return orderVOPageInfo;
    }
    private List<OrderVO> orderListToOrderVOList(List<Order> orders){
        ArrayList<OrderVO> orderVOS = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            OrderVO orderVO = getOrderVO(order);
            orderVOS.add(orderVO);
        }
        return orderVOS;
    }
    @Override
    public void cancel(String orderNo){
        //查询订单，查询到，查询不到说明错误
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ORDER);
        }
        //验证用户身份
        Integer userId = UserFilter.currentUser.getId();
        if(!order.getUserId().equals(userId)){
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_YOUR_ORDER);
        }
        //只有未付款时才能取消订单
        if(order.getOrderStatus().equals(Constant.OrderStatuEnum.NOT_PAID.getCode())){
            order.setOrderStatus(Constant.OrderStatuEnum.CANCELED.getCode());
            order.setEndTime(new Date());
            orderMapper.updateByOrderNo(order);
        }else {
            throw new ImoocMallException(ImoocMallExceptionEnum.CANCEL_WRONG_ORDER_STATUS);
        }
    }
    @Override
    //生成二维码
    public String qrcode(String orderNo){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        //获取端口号加上ip
        String address = uri;
        String payUrl="http://"+address+"pay?orderNo="+orderNo;
        try {
            QRCodeGenerator.generateQRCodeImage(payUrl,350,350,Constant.FILE_UPLOAD_DTR+orderNo+".png");
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pngAddress="http://"+address+"/images/"+orderNo+".png";
        return pngAddress;
    }
    //管理员列表
    @Override
    public PageInfo listForAdmin(Integer pageNum, Integer pageSize){

        PageHelper.startPage(pageNum,pageSize);
        List<Order> orders = orderMapper.selectForAdmin();
        List<OrderVO> orderVOS = orderListToOrderVOList(orders);
        //由于构造时必须是查出来的列表，所以后买你要将Listset为OrderVO
        //由于Page本身就是个list可以强转，page和pageInfo的使用主要是看开发文档返回数据
//        Page page=(Page)orders;
        PageInfo orderVOPageInfo = new PageInfo<>(orders);
        orderVOPageInfo.setList(orderVOS);
        return orderVOPageInfo;
    }
    //支付接口
    @Override
    public void pay(String orderNo){
        //查询订单，查询到，查询不到说明错误
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ORDER);
        }
        //验证用户身份
        Integer userId = UserFilter.currentUser.getId();
        if(!order.getUserId().equals(userId)){
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_YOUR_ORDER);
        }
        if(order.getOrderStatus()== Constant.OrderStatuEnum.NOT_PAID.getCode()){
            order.setOrderStatus(Constant.OrderStatuEnum.PAID.getCode());
            order.setPayTime(new Date());
            orderMapper.updateByOrderNo(order);
        }else{
            throw new ImoocMallException(ImoocMallExceptionEnum.PAY_WRONG_ORDER_STATUS);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    //发货接口
    @Override
    public void deliver(String orderNo){
        //查询订单，查询到，查询不到说明错误
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ORDER);
        }
        if(order.getOrderStatus()== Constant.OrderStatuEnum.PAID.getCode()){
            order.setOrderStatus(Constant.OrderStatuEnum.DELIVERED.getCode());
            order.setPayTime(new Date());
            orderMapper.updateByOrderNo(order);
        }else{
            throw new ImoocMallException(ImoocMallExceptionEnum.DELIVER_WRONG_ORDER_STATUS);
        }
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    //完结订单
    public void finish(String orderNo){
        //查询订单，查询到，查询不到说明错误
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order==null){
            throw new ImoocMallException(ImoocMallExceptionEnum.NO_ORDER);
        }
        //如果是普通用户，就要校验订单的所属U
        User currentUser = UserFilter.currentUser;
        boolean role = userService.getRole(currentUser);
        if(!role&&!order.getUserId().equals(currentUser.getId())){
            throw new ImoocMallException(ImoocMallExceptionEnum.NOT_YOUR_ORDER);
        }
        //发货后可以完结订单
        if(order.getOrderStatus()== Constant.OrderStatuEnum.DELIVERED.getCode()){
            order.setOrderStatus(Constant.OrderStatuEnum.FINISHED.getCode());
            order.setEndTime(new Date());
            orderMapper.updateByOrderNo(order);
        }else {
            throw new ImoocMallException(ImoocMallExceptionEnum.FINISH_WRONG_ORDER_STATUS);
        }
    }

    @Override
    public List<OrderStatisticsVO> statistics(Date startDate, Date endDate) {
        OrderStatisticsQuery orderStatisticsQuery = new OrderStatisticsQuery();
        orderStatisticsQuery.setStartDate(startDate);
        orderStatisticsQuery.setEndDate(endDate);
        List<OrderStatisticsVO> orderStatisticsVOS = orderMapper.selectOrderStatistics(orderStatisticsQuery);
        return orderStatisticsVOS;
    }


}
