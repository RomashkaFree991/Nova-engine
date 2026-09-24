package com.nova.engine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import org.json.JSONObject;
import java.io.File;
import java.util.List;

public class MainActivity extends Activity {
    private LinearLayout projects;
    private static final String[] TEMPLATES={"3D игра","2D игра","UI / интерфейс","Приложение","Пустой проект"};
    private static final String[] TEMPLATE_IDS={"3d","2d","ui","app","empty"};
    private static final int[] APIS={24,25,26,27,28,29,30,31,32,33,34};
    private static final String[] API_LABELS={"API 24 · Android 7.0","API 25 · Android 7.1","API 26 · Android 8.0","API 27 · Android 8.1","API 28 · Android 9","API 29 · Android 10","API 30 · Android 11","API 31 · Android 12","API 32 · Android 12L","API 33 · Android 13","API 34 · Android 14"};
    private static final String[] ABI_LABELS={"NovaScript (NDK не нужен)","arm64-v8a","armeabi-v7a","arm64-v8a + armeabi-v7a"};
    private static final String[] ABIS={"none","arm64-v8a","armeabi-v7a","arm64-v8a,armeabi-v7a"};
    private static final String[] ORIENT_LABELS={"Альбомная","Портретная","Авто-поворот"};
    private static final String[] ORIENTS={"landscape","portrait","sensor"};
    @Override public void onCreate(Bundle state){super.onCreate(state);Ui.bars(this);build();}
    @Override protected void onResume(){super.onResume();if(projects!=null)refresh();}
    private void build(){
        LinearLayout root=Ui.col(this);root.setBackgroundColor(Ui.BG);int pad=Ui.dp(this,24);root.setPadding(pad,pad,pad,pad);
        LinearLayout head=Ui.row(this);LinearLayout brand=Ui.col(this);TextView logo=Ui.text(this,"NOVA",30,Ui.TEXT,true);brand.addView(logo);TextView subtitle=Ui.text(this,"МОБИЛЬНАЯ СРЕДА РАЗРАБОТКИ",10,Ui.MUTED,true);subtitle.setLetterSpacing(.08f);brand.addView(subtitle);head.addView(brand);View space=new View(this);head.addView(space,new LinearLayout.LayoutParams(0,1,1));Button create=Ui.button(this,"＋  Создать",true);create.setOnClickListener(v->showCreate());head.addView(create);root.addView(head);
        TextView section=Ui.text(this,"Мои проекты",22,Ui.TEXT,true);LinearLayout.LayoutParams slp=Ui.lp(Ui.MATCH,Ui.WRAP,0);slp.topMargin=Ui.dp(this,38);root.addView(section,slp);
        TextView path=Ui.text(this,"Хранятся в  "+ProjectStore.root(this).getAbsolutePath(),11,Ui.MUTED,false);path.setSingleLine(true);path.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);LinearLayout.LayoutParams plp=Ui.lp(Ui.MATCH,Ui.WRAP,0);plp.topMargin=Ui.dp(this,4);root.addView(path,plp);
        ScrollView scroll=new ScrollView(this);projects=Ui.col(this);projects.setPadding(0,Ui.dp(this,22),0,Ui.dp(this,12));scroll.addView(projects);LinearLayout.LayoutParams sl=Ui.lp(Ui.MATCH,0,1);sl.topMargin=Ui.dp(this,6);root.addView(scroll,sl);
        TextView foot=Ui.text(this,"NOVA  ·  проекты остаются на этом устройстве",11,Ui.MUTED,false);foot.setGravity(Gravity.CENTER);root.addView(foot,Ui.lp(Ui.MATCH,Ui.dp(this,36),0));setContentView(root);
    }
    private void refresh(){projects.removeAllViews();List<File> items=ProjectStore.list(this);if(items.isEmpty()){LinearLayout empty=Ui.col(this);empty.setGravity(Gravity.CENTER);TextView symbol=Ui.text(this,"◇",34,Ui.PURPLE,true);symbol.setGravity(Gravity.CENTER);empty.addView(symbol);TextView title=Ui.text(this,"Пока нет проектов",17,Ui.TEXT,true);title.setGravity(Gravity.CENTER);empty.addView(title);TextView info=Ui.text(this,"Нажми «Создать», чтобы начать новую игру или приложение.",13,Ui.MUTED,false);info.setGravity(Gravity.CENTER);info.setPadding(0,Ui.dp(this,5),0,0);empty.addView(info);projects.addView(empty,new LinearLayout.LayoutParams(Ui.MATCH,Ui.dp(this,260)));return;}
        for(File f:items){JSONObject p=ProjectStore.readProject(f);JSONObject a=p.optJSONObject("android");String kind=p.optString("template","3d").toUpperCase();String meta=kind+"   ·   API "+(a==null?28:a.optInt("targetSdk",28))+"   ·   "+(a==null?"":a.optString("orientation","landscape"));LinearLayout row=Ui.row(this);row.setBackground(Ui.shape(this,Ui.PANEL,14,Ui.EDGE));row.setPadding(Ui.dp(this,16),Ui.dp(this,13),Ui.dp(this,16),Ui.dp(this,13));TextView icon=Ui.text(this,"N",18,Ui.PURPLE,true);icon.setGravity(Gravity.CENTER);icon.setBackground(Ui.shape(this,Ui.RAISED,10,0));row.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,42)));LinearLayout info=Ui.col(this);info.setPadding(Ui.dp(this,13),0,0,0);info.addView(Ui.text(this,f.getName(),16,Ui.TEXT,true));info.addView(Ui.text(this,meta,11,Ui.MUTED,false));row.addView(info,new LinearLayout.LayoutParams(0,Ui.WRAP,1));row.addView(Ui.text(this,"›",24,Ui.MUTED,false));LinearLayout.LayoutParams rp=Ui.lp(Ui.MATCH,Ui.WRAP,0);rp.bottomMargin=Ui.dp(this,9);projects.addView(row,rp);row.setOnClickListener(v->open(f));row.setOnLongClickListener(v->{new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert).setTitle("Удалить проект?").setMessage("Удалить «"+f.getName()+"» и все его файлы?").setNegativeButton("Отмена",null).setPositiveButton("Удалить",(d,w)->{delete(f);refresh();}).show();return true;});}
    }
    private void showCreate(){
        LinearLayout box=Ui.col(this);box.setPadding(Ui.dp(this,22),Ui.dp(this,18),Ui.dp(this,22),Ui.dp(this,20));box.setBackground(Ui.shape(this,Ui.PANEL,22,Ui.EDGE));box.addView(Ui.text(this,"Новый проект",21,Ui.TEXT,true));TextView intro=Ui.text(this,"Настройка папки и платформы",12,Ui.MUTED,false);intro.setPadding(0,Ui.dp(this,3),0,0);box.addView(intro);
        box.addView(Ui.label(this,"ИМЯ ПРОЕКТА"));EditText name=Ui.input(this,"Например, MyGame");name.setText(suggest());box.addView(name);
        box.addView(Ui.label(this,"ПАПКА ПРОЕКТА"));TextView path=Ui.text(this,ProjectStore.root(this).getAbsolutePath()+"/"+name.getText(),11,Ui.MUTED,false);path.setPadding(Ui.dp(this,3),0,0,0);path.setSingleLine(true);path.setEllipsize(android.text.TextUtils.TruncateAt.MIDDLE);box.addView(path);name.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){path.setText(ProjectStore.root(MainActivity.this).getAbsolutePath()+"/"+s);}public void afterTextChanged(android.text.Editable e){}});
        box.addView(Ui.label(this,"ШАБЛОН"));Spinner template=spinner(TEMPLATES,0);box.addView(template);
        LinearLayout versions=Ui.row(this);LinearLayout minBox=Ui.col(this),targetBox=Ui.col(this);minBox.addView(Ui.label(this,"MIN ANDROID"));Spinner min=spinner(API_LABELS,4);minBox.addView(min);targetBox.addView(Ui.label(this,"TARGET ANDROID"));Spinner target=spinner(API_LABELS,9);targetBox.addView(target);LinearLayout.LayoutParams vlp=new LinearLayout.LayoutParams(0,Ui.WRAP,1);vlp.rightMargin=Ui.dp(this,9);versions.addView(minBox,vlp);versions.addView(targetBox,new LinearLayout.LayoutParams(0,Ui.WRAP,1));box.addView(versions);
        box.addView(Ui.label(this,"NDK / ABI"));Spinner abi=spinner(ABI_LABELS,0);box.addView(abi);box.addView(Ui.label(this,"ОРИЕНТАЦИЯ"));Spinner orientation=spinner(ORIENT_LABELS,0);box.addView(orientation);
        LinearLayout buttons=Ui.row(this);buttons.setGravity(Gravity.END);LinearLayout.LayoutParams blp=Ui.lp(Ui.MATCH,Ui.WRAP,0);blp.topMargin=Ui.dp(this,22);box.addView(buttons,blp);Button cancel=Ui.button(this,"Отмена",false),ok=Ui.button(this,"Создать",true);buttons.addView(cancel);buttons.addView(Ui.gap(this,9));buttons.addView(ok);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.addView(box);Dialog dialog=new Dialog(this);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);dialog.setContentView(sc);dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.show();int width=Math.min((int)(getResources().getDisplayMetrics().widthPixels*.92f),Ui.dp(this,520));dialog.getWindow().setLayout(width,Math.min((int)(getResources().getDisplayMetrics().heightPixels*.9f),Ui.dp(this,760)));dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        cancel.setOnClickListener(v->dialog.dismiss());ok.setOnClickListener(v->{int mi=APIS[min.getSelectedItemPosition()],ta=APIS[target.getSelectedItemPosition()];if(ta<mi){toast("Target Android не может быть ниже Min Android");return;}try{File f=ProjectStore.create(this,name.getText().toString().trim(),mi,ta,ABIS[abi.getSelectedItemPosition()],TEMPLATE_IDS[template.getSelectedItemPosition()],ORIENTS[orientation.getSelectedItemPosition()]);dialog.dismiss();refresh();open(f);}catch(Exception e){toast(e.getMessage()==null?"Не удалось создать проект":e.getMessage());}});
    }
    private Spinner spinner(String[] values,int selected){Spinner s=new Spinner(this,Spinner.MODE_DROPDOWN);ArrayAdapter<String>a=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values){@Override public View getView(int p,View v,android.view.ViewGroup parent){TextView t=(TextView)super.getView(p,v,parent);t.setTextColor(Ui.TEXT);t.setTextSize(13);t.setPadding(Ui.dp(MainActivity.this,8),0,0,0);return t;}@Override public View getDropDownView(int p,View v,android.view.ViewGroup parent){TextView t=(TextView)super.getDropDownView(p,v,parent);t.setTextColor(Ui.TEXT);t.setTextSize(14);t.setPadding(Ui.dp(MainActivity.this,12),Ui.dp(MainActivity.this,12),Ui.dp(MainActivity.this,12),Ui.dp(MainActivity.this,12));t.setBackgroundColor(Ui.RAISED);return t;}};a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);s.setAdapter(a);s.setSelection(selected);s.setBackground(Ui.shape(this,Ui.RAISED,10,Ui.EDGE));s.setPadding(Ui.dp(this,7),Ui.dp(this,2),Ui.dp(this,7),Ui.dp(this,2));s.setMinimumHeight(Ui.dp(this,46));return s;}
    private String suggest(){String name="MyGame";int i=2;while(new File(ProjectStore.root(this),name).exists())name="MyGame"+i++;return name;}
    private void open(File f){Intent i=new Intent(this,WorkspaceActivity.class);i.putExtra("project",f.getAbsolutePath());startActivity(i);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private void delete(File f){File[] a=f.listFiles();if(a!=null)for(File x:a)delete(x);f.delete();}
}
