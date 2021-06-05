package me.loyio.mycontacts.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import me.loyio.mycontacts.R;
import me.loyio.mycontacts.provider.ContactProvider;
import me.loyio.mycontacts.dbhelper.ContactOpenHelper;
import me.loyio.mycontacts.VO.Contact;
import me.loyio.mycontacts.adapter.MyAdapter;
import me.loyio.mycontacts.utils.ToastUtils;
import me.loyio.mycontacts.utils.ThreadUtils;
import me.loyio.mycontacts.activity.RegisterActivity;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private EditText nameEdit;
    private EditText passwordEdit;
    private CheckBox rememberPass;
    private Button login;
    private Button register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameEdit=(EditText) findViewById(R.id.username);
        passwordEdit=(EditText) findViewById(R.id.password);
        rememberPass=(CheckBox) findViewById(R.id.remeber_pass);
        login=(Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        pref= (SharedPreferences) getSharedPreferences("data",MODE_PRIVATE);
        boolean isRemember=pref.getBoolean("remember_password",false);
        if (isRemember){
            String account=pref.getString("account","");
            String password=pref.getString("password","");
            nameEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }
        if(!pref.getString("account", "").equals("")){
            register.setVisibility(View.INVISIBLE);
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account=nameEdit.getText().toString();
                String password=passwordEdit.getText().toString();
                if(account.equals("") || password.equals("")){
                    Toast.makeText(LoginActivity.this,"账号或密码不能为空",Toast.LENGTH_SHORT).show();
                }else if(account.equals(pref.getString("account",""))&&password.equals(pref.getString("password",""))){
                    editor=pref.edit();
                    if (rememberPass.isChecked()){
                        editor.putBoolean("remember_password",true);
                        editor.putString("account",account);
                        editor.putString("password",password);
                    }else{
                        editor.clear();
                    }
                    editor.apply();
                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(LoginActivity.this,"密码或用户名输入错误",Toast.LENGTH_SHORT).show();
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}