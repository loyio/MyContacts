package me.loyio.mycontacts.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import me.loyio.mycontacts.R;
import me.loyio.mycontacts.provider.ContactProvider;
import me.loyio.mycontacts.dbhelper.ContactOpenHelper;
import me.loyio.mycontacts.VO.Contact;
import me.loyio.mycontacts.adapter.MyAdapter;
import me.loyio.mycontacts.utils.ToastUtils;
import me.loyio.mycontacts.utils.ThreadUtils;

public class RegisterActivity extends AppCompatActivity {

    private TextView registeNameText;
    private TextView registePasswordText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        registeNameText=findViewById(R.id.registe_nameText);
        registePasswordText=findViewById(R.id.registe_password);
        Button mregisteButton=(Button) findViewById(R.id.registe_button);
        mregisteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = registeNameText.getText().toString().trim();
                String password = registePasswordText.getText().toString().trim();
                if(account.equals("") || password.equals("")){
                    Toast.makeText(RegisterActivity.this,"账号或密码不能为空",Toast.LENGTH_SHORT).show();
                }else{
                    SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                    editor.putString("account",account);
                    editor.putString("password",password);
                    editor.apply();
                    Toast.makeText(RegisterActivity.this,"已存储",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

    }
}