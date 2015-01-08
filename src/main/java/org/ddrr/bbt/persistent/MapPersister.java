package org.ddrr.bbt.persistent;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import org.ddrr.bbt.BaseApplication;
import org.ddrr.bbt.infra.MapDecorer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by PhoebeHuyi on 2014.12.12.
 */
public class MapPersister {
    public <T extends MapDecorer, K, V> void writeToDB(T decorer) {
        Log.e("test", "writeToDB");
        String[][] selArgs = getSelectionArgs(decorer);
        Map<K, V> source = decorer.involute();
        if (!updateInternal(EntryBaseProvider.CONTENT_URI, source, selArgs)) {
            Log.e("test", "writeToDB before remove map = " + source);
            source.remove(BaseProvider.ID);
            Log.e("test", "writeToDB after remove map = " + source);
            insertIntoDBInternal(EntryBaseProvider.CONTENT_URI, source);
        }
    }

    private <T> String[][] getSelectionArgs(T decorer) {
        ArrayList<String[]> resultArray = new ArrayList<>();
        Class<?> clazz = decorer.getClass();
        MapDecorer.Key key;
        try {
            do {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(SelectionArg.class)) {
                        field.setAccessible(true);
                        Object value = field.get(decorer);
                        if (null == value) {
                            Log.w("getSelectionArgs", "" + field + " is null");
                        }
                        key = field.getAnnotation(MapDecorer.Key.class);
                        if (null == key) {
                            throw new RuntimeException("" + field + " is annotated with "
                                    + SelectionArg.class + " without " + MapDecorer.Key.class);
                        }
                        String k = key.key();
                        String v;
                        if (k.length() > 0) {
                            if (!MapDecorer.Converter.class.equals(key.backConverter())) {
                                v = key.backConverter().newInstance().convert(value).toString();
                            } else {
                                v = value.toString();
                            }
                        } else {
                            k = key.value();
                            v = value.toString();
                        }
                        resultArray.add(new String[]{k, v});
                    }
                }
                clazz = clazz.getSuperclass();
            } while (!Object.class.equals(clazz));
            String[][] result = new String[resultArray.size()][];
            System.arraycopy(resultArray.toArray(), 0, result, 0, result.length);
            return result;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <K, V> void insertIntoDBInternal(Uri uri, Map<K, V> map) {
        Log.e("test", "insertIntoDBInternal" + " map = " + map);
        ContentResolver cr = BaseApplication.getAppInstance().getContentResolver();
        ContentValues cv = new ContentValues();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            cv.put(entry.getKey().toString(), entry.getValue().toString());
        }
        cr.insert(uri, cv);
    }

    private <K, V> boolean updateInternal(Uri contentUri, Map<K, V> map, String[][] selArgs) {
        Log.e("test", "updateByIdInternal");
        if(selArgs.length == 0){
            return false;
        }
        ContentResolver cr = BaseApplication.getAppInstance().getContentResolver();
        ContentValues cv = new ContentValues();
        for (Map.Entry<K, V> entry : map.entrySet()) {

            try {
                cv.put(entry.getKey().toString(), entry.getValue().toString());
            } catch (NullPointerException e) {
                Log.w("compile update command","entry = " + entry);
            }
        }
        final int selSize = selArgs.length;
        StringBuilder sb = new StringBuilder();
        String[] selectionArgs = new String[selSize];
        for(int i = 0; i < selSize; i ++){
            sb.append(selArgs[i][0]).append(" = ? ");
            if(i < selSize - 1){
                sb.append("AND ");
            }
            selectionArgs[i] = selArgs[i][1];
        }
        return cr.update(contentUri, cv, sb.toString(), selectionArgs)!= 0;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SelectionArg {
    }
}

