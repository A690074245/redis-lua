package zy.redis.lua.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 * @date 2021/1/8 14:47
 */
public class ObjectToMap {
    public static Map<String,Object> exchange(Object obj){
        Map<String,Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        for(Field field:clazz.getDeclaredFields()){
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = null;
                try {
                    value = field.get(obj);
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
                map.put(fieldName,value);
        }
        return map;
    }
}
