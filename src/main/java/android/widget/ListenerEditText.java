package android.widget;

import android.content.Context;
import android.util.AttributeSet;

public class ListenerEditText extends EditText {
	private OnSelectionChangedListener mOnSelectionChangedListener;

	public ListenerEditText(Context context) {
		this(context, null);
	}

	public ListenerEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd) {
		super.onSelectionChanged(selStart, selEnd);
		if (null != mOnSelectionChangedListener) {
			mOnSelectionChangedListener.onSelectionChanged(this, selStart,
					selEnd);
		}
	}

	@Override
	public boolean isTextSelectable() {
		return true;
	}

	@Override
	public boolean onCheckIsTextEditor() {
		return false;
	}
	// @Override
	// public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
	// return null;
	// }

	public void setOnSelectionChangedListener(OnSelectionChangedListener l) {
		this.mOnSelectionChangedListener = l;
	}

	public interface OnSelectionChangedListener {
		public void onSelectionChanged(ListenerEditText et, int selStart,
                                       int selEnd);
	}
}
