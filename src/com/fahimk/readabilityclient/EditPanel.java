package com.fahimk.readabilityclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.TableLayout;

public class EditPanel extends TableLayout {
	private Paint innerPaint, borderPaint ;

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
		innerPaint.setARGB(225, 75, 75, 75); //gray
		innerPaint.setAntiAlias(true);
		borderPaint = new Paint();
		borderPaint.setARGB(255, 255, 255, 255);
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setStrokeWidth(2);
	}
	public void setInnerPaint(Paint innerPaint) {
		this.innerPaint = innerPaint;
	}
	public void setBorderPaint(Paint borderPaint) {
		this.borderPaint = borderPaint;
	}
	@Override
	protected void dispatchDraw(Canvas canvas) {

		RectF drawRect = new RectF();
		drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
		canvas.drawRoundRect(drawRect, 5, 5, innerPaint);canvas.drawRoundRect(drawRect, 5, 5, borderPaint);
		super.dispatchDraw(canvas);
	}
}
