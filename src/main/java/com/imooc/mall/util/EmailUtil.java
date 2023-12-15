package com.imooc.mall.util;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EmailUtil {
    //生成验证码
    public static String genVerificationCode(){
        List<String> verifivationChars= Arrays.asList(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
                "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"});
        Collections.shuffle(verifivationChars);
        String result="";
        for (int i = 0; i < 6; i++) {
            result+=verifivationChars.get(i);
        }
        return result;

    }

    //邮箱验证
    public static boolean isValidEmailAddress(String email){
        boolean result=true;
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            //该方法对地址进行校验
            internetAddress.validate();
        } catch (AddressException e) {
            e.printStackTrace();
            result =false;
        }
        return result;
    }
}
