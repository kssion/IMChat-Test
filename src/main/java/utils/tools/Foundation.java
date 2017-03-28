package main.java.utils.tools;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;

/**
 * 工具包
 * Created by Chance on 2017/2/28.
 */
public class Foundation {

    /**
     * 编码
     * @param bstr 内容
     * @return String
     */
    public static String encode_base64(byte[] bstr){
        return new sun.misc.BASE64Encoder().encode(bstr);
    }

    /**
     * 解码
     * @param str 内容
     * @return string
     */
    public static String decode_base64(String str){
        byte[] bt = null;
        try {
            sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
            bt = decoder.decodeBuffer(str);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String(bt);
    }

    public final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return "";
    }

    /**
     * 传入文本内容，返回 SHA-256 串
     *
     * @param strText
     * @return
     */
    public static String SHA256(final String strText)
    {
        return SHA(strText, "SHA-256");
    }

    /**
     * 传入文本内容，返回 SHA-512 串
     *
     * @param strText
     * @return
     */
    public static String SHA512(final String strText)
    {
        return SHA(strText, "SHA-512");
    }

    /**
     * 字符串 SHA 加密
     *
     * @param strText
     * @return
     */
    private static String SHA(final String strText, final String strType)
    {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (strText != null && strText.length() > 0)
        {
            try
            {
                // SHA 加密开始
                // 创建加密对象 并傳入加密類型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(strText.getBytes());
                // 得到 byte 類型结果
                byte byteBuffer[] = messageDigest.digest();

                // 將 byte 轉換爲 string
                StringBuffer strHexString = new StringBuffer();
                // 遍歷 byte buffer
                for (int i = 0; i < byteBuffer.length; i++)
                {
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1)
                    {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回結果
                strResult = strHexString.toString();
            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
        }

        return strResult;
    }

    /**
     * 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址;
     *
     * @param request 请求
     * @return
     * @throws IOException s
     */
    public static String getIpAddress(HttpServletRequest request) {
        // 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址

        String ip = request.getHeader("X-Forwarded-For");
        System.out.println("getIpAddress(HttpServletRequest) - X-Forwarded-For - String ip=" + ip);

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            System.out.println("getIpAddress(HttpServletRequest) - Proxy-Client-IP - String ip=" + ip);

            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
                System.out.println("getIpAddress(HttpServletRequest) - WL-Proxy-Client-IP - String ip=" + ip);

                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("HTTP_CLIENT_IP");
                    System.out.println("getIpAddress(HttpServletRequest) - HTTP_CLIENT_IP - String ip=" + ip);

                    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
                        System.out.println("getIpAddress(HttpServletRequest) - HTTP_X_FORWARDED_FOR - String ip=" + ip);

                        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                            ip = request.getRemoteAddr();
                            System.out.println("getIpAddress(HttpServletRequest) - getRemoteAddr - String ip=" + ip);
                        }
                    }
                }
            }

        } else if (ip.length() > 15) {
            String[] ips = ip.split(",");
            for (String ip1 : ips) {
                if (!("unknown".equalsIgnoreCase(ip1))) {
                    ip = ip1;
                    break;
                }
            }
        } else {
            ip = "0.0.0.0";
            System.out.println("getIpAddress(HttpServletRequest) - unknown - String ip=" + ip);
        }
        return ip;
    }

    public static String decryption_sign(String text) {

//        text = rm_disturb(text);
//        text = rm_text_transform(text);
//        text = rm_base64(text);
//        text = rm_disturb(text);
//        text = rm_text_transform(text);
//        text = rm_base64(text);
//        text = rm_disturb(text);
//        text = rm_text_transform(text);
//        text = rm_base64(text);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i += 2) {
            if (i + 1 < text.length()) {
                sb.append(text.charAt(i + 1));
            }
            sb.append(text.charAt(i));
        }

        return sb.toString();
    }

    /// 删除干扰字符
    private static String rm_disturb(String text) {
        StringBuilder sb = new StringBuilder(text);
        sb.deleteCharAt(text.length() - 3);
        sb.deleteCharAt(1);
        return sb.toString();
    }

    /// 大小写反转
    private static String rm_text_transform(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 'a' && c <= 'z') {
                c -= 32;
            } else if (c >= 'A' && c <= 'Z') {
                c += 32;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    ///
    private static String rm_base64(String text) {
        return Foundation.decode_base64(text);
    }

}
