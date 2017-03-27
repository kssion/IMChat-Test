package socket;

import java.io.UnsupportedEncodingException;

/**
 * Created by Chance on 2017/2/13.
 */
public class ArraysUtil {

    public static int search(byte[] bytes, byte[] search) {
        return search(bytes, search, 0);
    }

    public static int search(byte[] bytes, byte[] search, int startIndex) {
        if (startIndex > bytes.length || search.length > bytes.length) return -1;
        int n = 0;
        for (int i = startIndex; i < bytes.length; i++) {
            if (i > bytes.length - search.length + n) return -1;
            n = (bytes[i] == search[n] ? ++n : 0);
            if (n == search.length) {
                return i - n + 1;
            }
        }
        return -1;
    }


}
