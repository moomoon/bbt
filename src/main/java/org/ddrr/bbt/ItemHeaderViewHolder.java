package org.ddrr.bbt;

import android.view.View;

import com.mmscn.widgets.ItemHeaderLayout;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

public class ItemHeaderViewHolder extends com.mmscn.utils.ViewHolder {
    private ItemHeaderLayout headerLayout;

    protected ItemHeaderViewHolder(View v) {
        super(v);
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Header.class)) {
                field.setAccessible(true);
                try {
                    this.headerLayout = (ItemHeaderLayout) field.get(this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void updateHeader() {
        headerLayout.onScroll(null, 0, 0, 0);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    protected @interface Header {
    }

}
