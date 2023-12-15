package com.imooc.mall.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.imooc.mall.common.Constant;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;

import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;

//生成二维码工具
public class QRCodeGenerator {
    public static void generateQRCodeImage(String text,int width,int height,String filePath) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        //解决中文乱码
        HashMap<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        //BitMatrix bitMatrix是一个比特矩阵
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height,hints);
        //生成path
        Path path = FileSystems.getDefault().getPath(filePath);
        //把bitMatrix转换成图片存到path
        MatrixToImageWriter.writeToPath(bitMatrix,"PNG",path);
    }

    public static void main(String[] args) {
        try {
            generateQRCodeImage("燕意是0",350,350, "E:/imooc/QRTest.png");
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
