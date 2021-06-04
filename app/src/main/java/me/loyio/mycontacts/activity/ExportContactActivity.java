package me.loyio.mycontacts.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
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
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.loyio.mycontacts.R;
import me.loyio.mycontacts.VO.Contact;
import me.loyio.mycontacts.adapter.MyAdapter;
import me.loyio.mycontacts.dbhelper.ContactOpenHelper;
import me.loyio.mycontacts.provider.ContactProvider;
import me.loyio.mycontacts.utils.L;
import me.loyio.mycontacts.utils.ThreadUtils;
import me.loyio.mycontacts.utils.ToastUtils;

public class ExportContactActivity extends AppCompatActivity {

    private ListView mExportLv;
    private List<Contact> mContactList = new ArrayList<>();
    private CursorAdapter eMyAdapter;
    private MyAdapter mmMyAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_contact);
        mExportLv=findViewById(R.id.exportLv);

        // Menu and Data
        registerForContextMenu(mExportLv);
        initData();
        initView();
    }

    private void initView() {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                mmMyAdapter = new MyAdapter(ExportContactActivity.this,mContactList);
                mExportLv.setAdapter(mmMyAdapter);
            }
        });
    }

    private void initData() {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                // Get the contact data
                Cursor cursor = getContentResolver().query(ContactProvider.URI_CONTACT, null, null, null);
                if (cursor.getCount() == 0){
                    ToastUtils.showToastSafe(ExportContactActivity.this,"联系人为空");
                    return;
                }
                while(cursor.moveToNext()){
                    Contact contact = new Contact(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4), false);
                    mContactList.add(contact);
                }
                cursor.close();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("导出联系人操作");
        menu.add(0, Menu.FIRST,0,"全部选择");
        menu.add(0, Menu.FIRST+1,1,"取消选择");
        menu.add(0, Menu.FIRST+2,1,"确认导出");
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Menu.FIRST:selectAll();break;
            case Menu.FIRST+1:unSelectAll();break;
            case Menu.FIRST+2:
                if (ContextCompat.checkSelfPermission(ExportContactActivity.this, Manifest.permission.WRITE_CONTACTS)!=
                        PackageManager.PERMISSION_GRANTED){
                    // Get the permission
                    ActivityCompat.requestPermissions(ExportContactActivity.this,new String[]{Manifest.permission.WRITE_CONTACTS},1);
                }else {
                    exportContact(this);
                }
                finish();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void exportContact(Context context) {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                int num = 0;
                for (Contact contact : mContactList) {
                    if (contact.getChecked() == true) {
                        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                        while (cursor.moveToNext()) {
                            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            if (contact.getName().equalsIgnoreCase(contactName)) {
                                num++;
                            }
                        }
                        if (num == 0) {
                            ContentValues cv = new ContentValues();
                            Uri uri = context.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, cv);
                            long rawContactId = ContentUris.parseId(uri);
                            cv.clear();

                            // Name
                            cv.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                            cv.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                            cv.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.getName());
                            context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
                            cv.clear();

                            // PhoneNumber
                            cv.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                            cv.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                            cv.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhone());
                            cv.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                            context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
                            cv.clear();

                            // Email
                            cv.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
                            cv.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                            cv.put(ContactsContract.CommonDataKinds.Email.DATA, contact.getEmail());
                            cv.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
                            context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, cv);
                            count++;
                        } else {
                            ToastUtils.showToastSafe(ExportContactActivity.this, contact.getName() + "已存在！");
                        }
                        cursor.close();
                    }
                }
                ToastUtils.showToastSafe(ExportContactActivity.this, "导出" + count + "个联系人！");
            }
        });
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
}