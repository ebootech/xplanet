package tech.eboot.xplanet.common.util;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Author: TangThree
 * created on 2021/7/22 5:07 PM
 */
public class StringConverter
{
    public static Object stringToObj(String in, Type type)
    {
        if(in == null){
            return null;
        } else if (type == byte.class || type == Byte.class) {
            return Byte.valueOf(in);
        } else if (type == short.class || type == Short.class) {
            return Short.valueOf(in);
        } else if (type == int.class || type == Integer.class) {
            return Integer.valueOf(in);
        } else if (type == long.class || type == Long.class) {
            return Long.valueOf(in);
        } else if (type == float.class || type == Float.class) {
            return Float.valueOf(in);
        } else if (type == double.class || type == Double.class) {
            return Double.valueOf(in);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.valueOf(in);
        } else if (type == String.class) {
            return in;
        }else if (type == Date.class) {
            return new Date(Long.valueOf(in));
        } else if (type == BigDecimal.class) {
            return new BigDecimal(in);
        }else {
            //其他类型默认使用jackson序列化
            return JsonUtils.readObject(in, type);
        }
    }

    public static String objToString(Object obj)
    {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number
                || obj instanceof Boolean
                || obj instanceof CharSequence) {
            return obj.toString();
        } else if (obj instanceof Date) {
            return ((Date)obj).getTime() + "";
        } else {
            //其他类型默认使用jackson序列化
            return JsonUtils.toJson(obj);
        }
    }
}
