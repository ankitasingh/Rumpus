package com.singh.ankita.leaf.customLayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

//Custom Layout for displaying image details 
public class TransparentPanel extends LinearLayout{
	
	public TransparentPanel(Context context, AttributeSet attrs) {
	    super(context, attrs);
	}
	public void dispatchDraw(Canvas canvas) {

		RectF drawRect = new RectF();//Creates a new rectangle
		drawRect.set(0,0, getMeasuredWidth(), getMeasuredHeight());
        Paint innerPaint=new Paint();
      
        innerPaint.setARGB(128, 255, 255,255);
        innerPaint.setColor(Color.BLACK);//Sets Paint Color to Black
        innerPaint.setAlpha(125);//Sets Paint Transparency
		canvas.drawRoundRect(drawRect, 5, 5, innerPaint);//Draws a rectangle on the canvas with the corresponding paint
	

		super.dispatchDraw(canvas);

		}
}
