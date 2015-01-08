package org.ddrr.bbt.infra;

import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDecorer<K, V> {
    protected final static String KEY_PARCEL_BUNDLE = "key_bundle_map";
    protected Map<K, V> mMap;

    protected MapDecorer() {

    }

    public static <T extends MapDecorer<K, V>, K, V> T createFromMap(
            Class<T> clazz, Map<K, V> src) {
        try {
            T result;
            result = clazz.newInstance();
            result.inject(src);
            result.mMap = src;
            return result;
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new MapDecorerConstructorException(
                    "MapDecorer "
                            + clazz.getCanonicalName()
                            + " must have a constructor that does not require parameters.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public Map<K, V> involute() {
        Log.e("test", "involute");
        Map<K, V> result = new HashMap<K, V>();
        Class<?> clazz = this.getClass();
        do {
            Log.e("test", "class = " + clazz);
            for (Field field : clazz.getDeclaredFields()) {
                Key key = field.getAnnotation(Key.class);
                Log.e("test", "field = " + field);
                for(Annotation a : field.getDeclaredAnnotations()){
                    Log.e("test", a.toString());
                }
                if (null != key && ! field.isAnnotationPresent(IgnoreField.class)) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(this);
                        Log.e("involute","field = " + field + " value = " + value);
                        if (null == value) {
                            continue;
                        }
                        String effectiveKey = key.key();
                        Class<? extends Converter> backConverter = key.backConverter();
                        if (effectiveKey.length() > 0) {
                            if (backConverter != Converter.class) {
                                try {
                                    Converter<java.lang.Object, V> c = key.backConverter().newInstance();
                                    result.put((K) effectiveKey, c.convert(value));
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                    throw new MapDecorerConstructorException(
                                            "MapDecorer.Converter "
                                                    + key.backConverter()
                                                    .getCanonicalName()
                                                    + " must have a constructor that does not require parameters.");

                                }
                                continue;
                            }
                        } else {
                            effectiveKey = key.value();
                        }
                        result.put((K) effectiveKey, (V) value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
            clazz = clazz.getSuperclass();
        }
        while (!clazz.equals(MapDecorer.class));
        Log.e("test", "finish involute map = " + result);
        return result;
    }

    protected void inject(Map<K, V> src) throws IllegalAccessException,
            IllegalArgumentException {
        if (null != src) {
            Class<?> clazz = this.getClass();
            do {
                for (Field field : clazz.getDeclaredFields()) {
                    Key key = field.getAnnotation(Key.class);
                    if (null != key) {
                        field.setAccessible(true);
                        String effectiveKey = key.key();
                        if (effectiveKey.length() > 0) {
                            try {
                                Converter<V, ?> c = key.converter()
                                        .newInstance();
                                field.set(this,
                                        c.convert(src.get(effectiveKey)));
                            } catch (InstantiationException e) {
                                e.printStackTrace();
                                throw new MapDecorerConstructorException(
                                        "MapDecorer.Converter "
                                                + key.converter()
                                                .getCanonicalName()
                                                + " must have a public constructor that does not require parameters.");

                            }
                        } else {
                            effectiveKey = key.value();
                            field.set(this, src.get(effectiveKey));
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            } while (!clazz.equals(MapDecorer.class));
        }

    }

    public static <T extends MapDecorer<K, V>, K, V> List<T> createListFromMaps(
            Class<T> clazz, Collection<Map<K, V>> maps) {
        int size = 0;
        if (null != maps) {
            size = maps.size();
        }
        List<T> result = new ArrayList<T>(size);
        if (size > 0) {
            for (Map<K, V> map : maps) {
                result.add(createFromMap(clazz, map));
            }
        }
        return result;
    }

    public Map<K, V> getMap() {
        return mMap;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Key {
        String value() default "";

        String key() default "";

        Class<? extends Converter> converter() default Converter.class;

        Class<? extends Converter> backConverter() default Converter.class;

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected @interface IgnoreField{
    }

    public static abstract class Converter<F, T> {
        public abstract T convert(F from);
    }

    protected static class StringToLongConverter extends
            Converter<String, Long> {

        @Override
        public Long convert(String from) {
            try {
                return Long.valueOf(from);
            } catch (NumberFormatException | NullPointerException e) {
                e.printStackTrace();
            }
            return Long.MIN_VALUE;
        }

    }

    protected static class StringToFloatConverter extends
            Converter<String, Float> {

        @Override
        public Float convert(String from) {
            try {
                return Float.valueOf(from);
            } catch (NumberFormatException | NullPointerException e) {
                e.printStackTrace();
            }

            return Float.NaN;
        }

    }

    protected static class StringToIntegerConverter extends
            Converter<String, Integer> {

        @Override
        public Integer convert(String from) {

            try {
                return Integer.valueOf(from);
            } catch (NumberFormatException | NullPointerException e) {
                e.printStackTrace();
            }
            return Integer.MIN_VALUE;
        }

    }

    protected static class MapDecorerConstructorException extends
            RuntimeException {
        private static final long serialVersionUID = -3758528196495207608L;

        public MapDecorerConstructorException(String detail) {
            super(detail);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append('[');
        Class<?> clazz = getClass();
        do{
            for(Field field : clazz.getDeclaredFields()){
                try {
                    sb.append(field).append(':').append(field.get(this)).append(',');
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            clazz = clazz.getSuperclass();
        } while(!Object.class.equals(clazz));
        if(sb.charAt(sb.length() - 1)== ','){
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(']');
        return sb.toString();
    }

}
