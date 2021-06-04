package me.loyio.mycontacts.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import me.loyio.mycontacts.R;
import me.loyio.mycontacts.provider.ContactProvider;
import me.loyio.mycontacts.dbhelper.ContactOpenHelper;
import me.loyio.mycontacts.VO.Contact;
import me.loyio.mycontacts.adapter.MyAdapter;
import me.loyio.mycontacts.utils.ToastUtils;
import me.loyio.mycontacts.utils.ThreadUtils;

public class ImportContactActivity extends AppCompatActivity {

    private ListView mImportLv;
    private List<Contact> mContactList=new ArrayList<>();
    private MyAdapter mMyAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_contact);
        mImportLv=findViewById(R.id.importLv);

        // Menu and Data
        registerForContextMenu(mImportLv);
        initData();
        initView();
    }

    private void initView() {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                mMyAdapter = new MyAdapter(ImportContactActivity.this,mContactList);
                mImportLv.setAdapter(mMyAdapter);
            }
        });
    }

    private void initData() {
        if (ContextCompat.checkSelfPermission(ImportContactActivity.this, Manifest.permission.READ_CONTACTS)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ImportContactActivity.this,new String[]{Manifest.permission.READ_CONTACTS},1);
        }else{
            importContact();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("导入联系人操作");
        menu.add(0, Menu.FIRST,0,"全部选择");
        menu.add(0, Menu.FIRST+1,1,"取消选择");
        menu.add(0, Menu.FIRST+2,1,"确认导出");
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Menu.FIRST:
                selectAll();
                break;
            case Menu.FIRST + 1:
                unSelectAll();
                break;
            case Menu.FIRST + 2:
                writeContact();
                finish();
        }
        return super.onContextItemSelected(item);
    }

    private void selectAll() {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                for (Contact c:mContactList) {
                    c.setChecked(true);
                }
                initView();
            }
        });
    }

    private void unSelectAll() {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                for (Contact c:mContactList) {
                    c.setChecked(false);
                }
                initView();
            }
        });
    }

    private void writeContact() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                for (Contact contact :mContactList) {
                    if (contact.getChecked() == true){
                        Cursor cursor = getContentResolver().query(ContactProvider.URI_CONTACT,null,"name=?",new String[]{contact.getName()},null);
                        if (cursor.getCount() == 0){
                            ContentValues cv = new ContentValues();
                            cv.put("name",contact.getName());
                            cv.put("phone",contact.getPhone());
                            cv.put("email",contact.getEmail());
                            cv.put("qq", contact.getQq());
                            getContentResolver().insert(ContactProvider.URI_CONTACT,cv);
                            count++;
                        }else{
                            ToastUtils.showToastSafe(ImportContactActivity.this,contact.getName()+"已存在！");
                        }
                        cursor.close();
                    }
                }
                ToastUtils.showToastSafe(ImportContactActivity.this,"已导入"+count+"个联系人！");
            }
        });
    }

    private void importContact() {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                // Get The Contacts in My Mobile Phone
                while (cursor.moveToNext()) {
                    String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                    // Name
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    // The First Number
                    Cursor phones = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                            null,
                            null);
                    String phoneNumber = "";
                    if (phones.moveToFirst())
                        phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phones.close();

                    // The First Email Address
                    Cursor emails = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
                            null, null);
                    String emailAddress = "";
                    if (emails.moveToFirst())
                        emailAddress = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    emails.close();

                    // Create the Contact
                    Contact contact = new Contact(name, phoneNumber, emailAddress, null, false);
                    mContactList.add(contact);
                }
                cursor.close();
            }
        });
    }
}