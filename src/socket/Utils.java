package socket;

/**
 * Created by Chance on 2017/2/19.
 */
public class Utils {

    public static class NumberUtil {

        /**
         * int整数转换为4字节的byte数组
         *
         * @param i
         *            整数
         * @return byte数组
         */
        public static byte[] intToByte4(int i) {
            byte[] targets = new byte[4];
            targets[3] = (byte) (i & 0xFF);
            targets[2] = (byte) (i >> 8 & 0xFF);
            targets[1] = (byte) (i >> 16 & 0xFF);
            targets[0] = (byte) (i >> 24 & 0xFF);
            return targets;
        }

        /**
         * byte数组转换为int整数
         *
         * @param bytes
         *            byte数组
         * @param off
         *            开始位置
         * @return int整数
         */
        public static int byte4ToInt(byte[] bytes, int off) {
            int b0 = bytes[off] & 0xFF;
            int b1 = bytes[off + 1] & 0xFF;
            int b2 = bytes[off + 2] & 0xFF;
            int b3 = bytes[off + 3] & 0xFF;
            return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
        }
    }
}
