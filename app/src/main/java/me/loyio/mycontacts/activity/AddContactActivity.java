package me.loyio.mycontacts.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import me.loyio.mycontacts.R;
import me.loyio.mycontacts.utils.ThreadUtils;
import me.loyio.mycontacts.dbhelper.ContactOpenHelper;
import me.loyio.mycontacts.provider.ContactProvider;
import me.loyio.mycontacts.utils.ToastUtils;

public class AddContactActivity extends AppCompatActivity {

    private EditText myAddName,myAddPhone,myAddEmail,myAddQq;
    private Button myAddBtn,myBackBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        myAddName=findViewById(R.id.addNameEt);
        myAddPhone=findViewById(R.id.addPhoneEt);
        myAddEmail=findViewById(R.id.addEmailEt);
        myAddQq=findViewById(R.id.addQqEt);
        myAddBtn=findViewById(R.id.addContactBtn);
        myAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThreadUtils.runInThread(new Runnable() {
                    @Override
                    public void run() {
                        addContact();
                        finish();
                    }
                });
            }


        });
    }

    private void addContact() {
        /**
         * ContentValues 和HashTable类似都是一种存储的机制 但是两者最大的区别就在于，
         * contenvalues只能存储基本类型的数据，像string，int之类的，
         * 不能存储对象这种东西，而HashTable却可以存储对象。
         * ContentValues存储对象的时候，以(key,value)的形式来存储数据。
         */
        ContentValues cv=new ContentValues();
        cv.put(ContactOpenHelper.ContactTable.NAME,myAddName.getText().toString());
        cv.put(ContactOpenHelper.ContactTable.PHONE,myAddPhone.getText().toString());
        cv.put(ContactOpenHelper.ContactTable.EMAIL,myAddEmail.getText().toString());
        cv.put(ContactOpenHelper.ContactTable.QQ,myAddQq.getText().toString());
        Cursor cursor=getContentResolver().query(ContactProvider.URI_CONTACT,
                null,
                "name=?",
                new String[]{myAddName.getText().toString()},
                null);
        if (cursor.getCount()>0){
            ToastUtils.showToastSafe(this,"该联系人已存在！");
        }else{
            getContentResolver().insert(ContactProvider.URI_CONTACT,cv);
            ToastUtils.showToastSafe(this,"联系人已添加！");
        }
    }
}