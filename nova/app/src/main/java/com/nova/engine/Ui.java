package com.nova.engine;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    static final int BG=0xFF151518, PANEL=0xFF1D1D22, RAISED=0xFF28282F, EDGE=0xFF383840;
    static final int TEXT=0xFFECECF0, MUTED=0xFF9999A4, PURPLE=0xFF8C63F7, GREEN=0xFF70D69A;
    static final int MATCH=ViewGroup.LayoutParams.MATCH_PARENT, WRAP=ViewGroup.LayoutParams.WRAP_CONTENT;
    private Ui() {}
    static int dp(Context c,float n){return (int)(n*c.getResources().getDisplayMetrics().density+0.5f);}
    static GradientDrawable shape(Context c,int color,float radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));if(stroke!=0)d.setStroke(dp(c,1),stroke);return d;}
    static TextView text(Context c,String s,int sp,int color,boolean bold){TextView t=new TextView(c);t.setText(s);t.setTextSize(TypedValue.COMPLEX_UNIT_SP,sp);t.setTextColor(color);t.setTypeface(Typeface.create(bold?"sans-serif-medium":"sans-serif",bold?Typeface.NORMAL:Typeface.NORMAL));t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    static TextView label(Context c,String s){TextView t=text(c,s,11,MUTED,true);t.setLetterSpacing(.08f);t.setPadding(0,dp(c,16),0,dp(c,7));return t;}
    static Button button(Context c,String s,boolean primary){Button b=new Button(c);b.setText(s);b.setAllCaps(false);b.setTextSize(14);b.setTextColor(primary?Color.WHITE:TEXT);b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));GradientDrawable bg=shape(c,primary?PURPLE:RAISED,11,primary?0:EDGE);b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33FFFFFF),bg,null));b.setPadding(dp(c,16),dp(c,8),dp(c,16),dp(c,8));b.setMinHeight(dp(c,44));b.setMinimumHeight(dp(c,44));b.setStateListAnimator(null);return b;}
    static EditText input(Context c,String hint){EditText e=new EditText(c);e.setSingleLine(true);e.setTextSize(15);e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setHint(hint);e.setPadding(dp(c,13),dp(c,10),dp(c,13),dp(c,10));e.setBackground(shape(c,RAISED,11,EDGE));return e;}
    static LinearLayout row(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    static LinearLayout col(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
    static LinearLayout.LayoutParams lp(int w,int h,float weight){return new LinearLayout.LayoutParams(w,h,weight);}
    static View gap(Context c,int widthDp){View v=new View(c);v.setLayoutParams(new LinearLayout.LayoutParams(dp(c,widthDp),1));return v;}
    static void bars(android.app.Activity a){a.getWindow().setStatusBarColor(BG);a.getWindow().setNavigationBarColor(BG);}
}
