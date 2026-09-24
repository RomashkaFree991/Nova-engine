package com.nova.engine;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;
import java.io.File;

/** Full-screen lightweight source editor for text files in the open project. */
public class CodeEditorActivity extends Activity {
    private File file;private EditText code;
    @Override protected void onCreate(Bundle b){super.onCreate(b);Ui.bars(this);String path=getIntent().getStringExtra("path");if(path==null){finish();return;}file=new File(path);if(!file.isFile()||file.length()>1024*1024){Toast.makeText(this,"Файл отсутствует или больше 1 МБ",Toast.LENGTH_LONG).show();finish();return;}build();}
    private void build(){LinearLayout root=Ui.col(this);root.setBackgroundColor(Ui.BG);root.setPadding(Ui.dp(this,12),Ui.dp(this,8),Ui.dp(this,12),Ui.dp(this,10));LinearLayout bar=Ui.row(this);IconButton back=new IconButton(this,IconButton.CLOSE,"Назад");back.setOnClickListener(v->finish());bar.addView(back);TextView title=Ui.text(this,file.getName(),15,Ui.TEXT,true);title.setPadding(Ui.dp(this,10),0,0,0);bar.addView(title,new LinearLayout.LayoutParams(0,Ui.WRAP,1));Button save=Ui.button(this,"Сохранить",true);save.setTextSize(12);bar.addView(save);root.addView(bar);
        code=new EditText(this);try{code.setText(ProjectStore.read(file));}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();finish();return;}code.setGravity(Gravity.TOP|Gravity.LEFT);code.setTextColor(Ui.TEXT);code.setHintTextColor(Ui.MUTED);code.setTextSize(13);code.setTypeface(android.graphics.Typeface.MONOSPACE);code.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));code.setBackground(Ui.shape(this,Ui.PANEL,12,Ui.EDGE));code.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);code.setHorizontallyScrolling(true);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(Ui.MATCH,0,1);cp.topMargin=Ui.dp(this,8);root.addView(code,cp);
        TextView hint=Ui.text(this,"Изменения сохраняются в файл проекта",10,Ui.MUTED,false);hint.setPadding(Ui.dp(this,3),Ui.dp(this,5),0,0);root.addView(hint);save.setOnClickListener(v->{try{ProjectStore.write(file,code.getText().toString());Toast.makeText(this,"Файл сохранён",Toast.LENGTH_SHORT).show();setResult(RESULT_OK);}catch(Exception e){Toast.makeText(this,"Ошибка сохранения: "+e.getMessage(),Toast.LENGTH_LONG).show();}});}
}
