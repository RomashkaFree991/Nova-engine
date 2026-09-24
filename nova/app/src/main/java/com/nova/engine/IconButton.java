package com.nova.engine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Small touch target with hand-drawn vector icons, no image assets required. */
final class IconButton extends View {
    static final int PLUS=1,PLAY=2,STOP=3,FOLDER=4,LOGS=5,SEND=6,CLEAR=7,CLOSE=8,OPEN=9,MODEL=10;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private int icon;private final int size;private String description="";
    IconButton(Context c,int icon,String description){super(c);this.icon=icon;this.description=description;size=Ui.dp(c,38);setMinimumWidth(size);setMinimumHeight(size);setFocusable(true);setClickable(true);setContentDescription(description);}
    void setIcon(int id){icon=id;invalidate();}
    @Override protected void onMeasure(int w,int h){setMeasuredDimension(size,size);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float d=getResources().getDisplayMetrics().density;float cx=getWidth()/2f,cy=getHeight()/2f,r=Ui.dp(getContext(),15);paint.setStyle(Paint.Style.FILL);paint.setColor(isPressed()?0xFF34343C:Ui.RAISED);c.drawCircle(cx,cy,r,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1.8f,1.8f*d));paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);paint.setColor(icon==PLAY?Ui.GREEN:Ui.TEXT);float u=d;Path p=new Path();switch(icon){case PLUS:c.drawLine(cx-5*u,cy,cx+5*u,cy,paint);c.drawLine(cx,cy-5*u,cx,cy+5*u,paint);break;case PLAY:p.moveTo(cx-3*u,cy-6*u);p.lineTo(cx+6*u,cy);p.lineTo(cx-3*u,cy+6*u);p.close();paint.setStyle(Paint.Style.FILL);c.drawPath(p,paint);break;case STOP:c.drawRoundRect(new RectF(cx-5*u,cy-5*u,cx+5*u,cy+5*u),1.5f*u,1.5f*u,paint);break;case FOLDER:p.moveTo(cx-8*u,cy-5*u);p.lineTo(cx-2*u,cy-5*u);p.lineTo(cx,cy-2*u);p.lineTo(cx+8*u,cy-2*u);p.lineTo(cx+7*u,cy+6*u);p.lineTo(cx-8*u,cy+6*u);p.close();c.drawPath(p,paint);break;case LOGS:c.drawLine(cx-6*u,cy-5*u,cx+6*u,cy-5*u,paint);c.drawLine(cx-6*u,cy,cx+6*u,cy,paint);c.drawLine(cx-6*u,cy+5*u,cx+3*u,cy+5*u,paint);break;case SEND:p.moveTo(cx-7*u,cy);p.lineTo(cx+6*u,cy);p.moveTo(cx+1*u,cy-5*u);p.lineTo(cx+6*u,cy);p.lineTo(cx+1*u,cy+5*u);c.drawPath(p,paint);break;case CLEAR:c.drawLine(cx-5*u,cy-5*u,cx+5*u,cy+5*u,paint);c.drawLine(cx+5*u,cy-5*u,cx-5*u,cy+5*u,paint);break;case CLOSE:c.drawLine(cx-5*u,cy-5*u,cx+5*u,cy+5*u,paint);c.drawLine(cx+5*u,cy-5*u,cx-5*u,cy+5*u,paint);break;case OPEN:p.moveTo(cx-5*u,cy+5*u);p.lineTo(cx+5*u,cy-5*u);p.moveTo(cx-4*u,cy-5*u);p.lineTo(cx+5*u,cy-5*u);p.lineTo(cx+5*u,cy+4*u);c.drawPath(p,paint);break;case MODEL:p.moveTo(cx,cy-7*u);p.lineTo(cx+6*u,cy-3*u);p.lineTo(cx+6*u,cy+4*u);p.lineTo(cx,cy+7*u);p.lineTo(cx-6*u,cy+4*u);p.lineTo(cx-6*u,cy-3*u);p.close();c.drawPath(p,paint);c.drawLine(cx,cy-7*u,cx,cy+7*u,paint);c.drawLine(cx-6*u,cy-3*u,cx,cy,paint);c.drawLine(cx+6*u,cy-3*u,cx,cy,paint);break;}}
}
