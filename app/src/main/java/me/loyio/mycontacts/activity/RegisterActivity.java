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
                SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                editor.putString("name",registeNameText.getText().toString().trim());
                editor.putString("password",registePasswordText.getText().toString().trim());
                editor.apply();
                Toast.makeText(RegisterActivity.this,"已存储",Toast.LENGTH_LONG).show();
                finish();
            }
        });

    }
}