package xinQing.common.util;

/**
 * 驼峰 <==> 下划线
 *
 * Created by null on 2016/12/19.
 */
public class HumpUnderline {

    /**
     * 驼峰转下划线
     * 例如：
     * userName => user_name
     * UserName => user_name
     * U => u
     * UName => u_name
     * U_Name => u__name
     *
     * @param hump 驼峰字符串
     * @return 转为下划线字符串
     */
    public static String toUnderline(String hump) {
        if (isNull(hump)) {
            return hump;
        }
        char[] upperCharArray = hump.toUpperCase().toCharArray();
        StringBuilder result = new StringBuilder();
        // 思路：转为大写，比较每一个字符，如果相等，那么说明是大写，那么在前面添加'_',并将其转为小写
        for (int i = 0; i < upperCharArray.length; i++) {
            // 大写的首字母不加'_'，直接转为小写
            if (upperCharArray[i] == hump.charAt(i) && i == 0) {
                result.append((char) (upperCharArray[i] + 32));
            } else if (upperCharArray[i] == hump.charAt(i) && upperCharArray[i] != '_') {
                // 后面的大写字母加上下划线后转为小写
                result.append("_").append((char) (upperCharArray[i] + 32));
            } else {
                // 小写字母或者本身是下划线
                result.append(hump.charAt(i));
            }
        }
        return result.toString();
    }

    /**
     * 下划线转驼峰
     * user_name => userName
     * user_Name => userName
     * _ => ""
     * __ => ""
     * _user => User
     * _U => U
     * _u => U
     *
     * @param underline 下划线字符串
     * @return 转为驼峰字符串
     */
    public static String toHump(String underline) {
        if (isNull(underline)) {
            return underline;
        }
        // 思路：删掉'_'并将后一个字符小写
        StringBuilder result = new StringBuilder();
        char[] underlineCharArray = underline.toCharArray();
        boolean isToUpper = false;
        for (int i = 0; i < underlineCharArray.length; i++) {
            if (isToUpper && underlineCharArray[i] != '_') {
                // 判断是否大写，大写就不需要转为大写了
                if (65 <= underlineCharArray[i] && underlineCharArray[i] <= 90) {
                    result.append(underlineCharArray[i]);
                } else {
                    result.append((char)(underlineCharArray[i] -32 ));
                }
            } else if (underlineCharArray[i] != '_') {
                result.append(underlineCharArray[i]);
            }
            isToUpper = (underlineCharArray[i] == '_');
        }
        return result.toString();
    }

    private static boolean isNull(String text) {
        return text == null || text.isEmpty();
    }

}
