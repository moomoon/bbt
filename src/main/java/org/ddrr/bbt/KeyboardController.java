package org.ddrr.bbt;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mmscn.utils.ViewController;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class KeyboardController extends ViewController implements
        KeyboardCallbacks {
    private final KeyboardCallbacks mExternalCallbacks;

    public KeyboardController(KeyboardCallbacks externalCallbacks) {
        this.mExternalCallbacks = externalCallbacks;
    }

    @Override
    protected View onCreateView(Context context, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.keyboard,
                parent, false);
        Map<Integer, TextView> map = new LinkedHashMap<>();
        final Resources r = context.getResources();
        final String prefix = "keyboard_button_container_";
        final String defType = "id";
        final String defPackage = context.getPackageName();
        View dummy = ((Activity) context).getWindow().getDecorView().findViewById(R.id.new_entry_dummy_keyboard);
        for (int i = 0; i < 10; i++) {
            String idStr = prefix + i;
            int id = r.getIdentifier(idStr, defType, defPackage);
            TextView tv = (TextView) ((ViewGroup) v.findViewById(id))
                    .getChildAt(0);
            tv.setBackgroundResource(0);
            View dummyCell = ((ViewGroup) dummy.findViewById(id)).getChildAt(0);
            final AnimationDrawable ad = (AnimationDrawable) dummyCell.getBackground();
            tv.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                        ad.stop();
                        ad.start();
                    }
                    return false;
                }
            });
            map.put(i, tv);
        }
        TextView tvBackspace = (TextView) ((ViewGroup) v
                .findViewById(R.id.keyboard_button_container_backspace))
                .getChildAt(0);
        final AnimationDrawable ad = (AnimationDrawable) ((ViewGroup) dummy.findViewById(R.id.keyboard_button_container_backspace)).getChildAt(0).getBackground();
        tvBackspace.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                    ad.stop();
                    ad.start();
                }
                return false;
            }
        });
        map.put(R.id.keyboard_button_container_backspace, tvBackspace
        );
        setupView(map);
        return v;
    }

    protected abstract void setupView(Map<Integer, TextView> views);

    @Override
    public void onInput(String content) {
        mExternalCallbacks.onInput(content);

    }

    @Override
    public void onBackspace() {
        mExternalCallbacks.onBackspace();
    }

}
