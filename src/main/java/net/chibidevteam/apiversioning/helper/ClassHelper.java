package net.chibidevteam.apiversioning.helper;

import static net.chibidevteam.apiversioning.config.ApiVersioningConfiguration.PRIME_NBR;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ClassHelper {

    private ClassHelper() {
    }

    public static <T> boolean areEquals(Class<T> clazz, T self, Object obj) {
        if (self == obj) {
            return true;
        }
        if (!(clazz.isInstance(obj))) {
            return false;
        }
        T t = clazz.cast(obj);
        Map<String, Object> mapA = getFields(clazz, self);
        Map<String, Object> mapB = getFields(clazz, t);
        return Objects.equals(mapA, mapB);
    }

    public static <T> int hash(Class<T> clazz, T obj) {
        Map<String, Object> map = getFields(clazz, obj);
        int h = 0;
        int i = 0;
        for (Object val : map.values()) {
            ++i;
            h += val == null ? 0 : val.hashCode() * PRIME_NBR ^ i;
        }
        return h;
    }

    private static <T> Map<String, Object> getFields(Class<T> clazz, T obj) {
        Map<String, Object> map = new TreeMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(obj));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                map.put(field.getName(), null);
            }
        }
        return map;
    }
}
