package com.fahimk.readabilityclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class EditPanel extends LinearLayout {
	private Paint innerPaint;

	public EditPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public EditPanel(Context context) {
		super(context);
		init();
	}
	private void init() {
		innerPaint = new Paint();
		innerPaint.setARGB(225, 15, 15, 15); //gray
		innerPaint.setAntiAlias(true);
	}
	public void setInnerPaint(Paint innerPaint) {
		this.innerPaint = innerPaint;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {

		RectF drawRect = new RectF();
		drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
		canvas.drawRect(drawRect, innerPaint);
		super.dispatchDraw(canvas);
	}
}
