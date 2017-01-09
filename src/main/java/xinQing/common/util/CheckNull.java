package xinQing.common.util;

/**
 * 检查null
 *
 * Created by null on 2017/1/1.
 */
public class CheckNull {

    public static boolean isNull(String[] arr) {
        return  arr == null || (arr.length == 0);
    }

    public static boolean isNull(Object object) {
        return object == null;
    }
}
