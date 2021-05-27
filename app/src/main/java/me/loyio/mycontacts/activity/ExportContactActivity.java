package me.loyio.mycontacts.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
    private List<Contact> mContactList=new ArrayList<>();
    private CursorAdapter eMyAdapter;
    private MyAdapter mmMyAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_contact);
        mExportLv=findViewById(R.id.exportLv);
        initDate();
        initView();
        registerForContextMenu(mExportLv);
    }

    private void initView() {
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
            case Menu.FIRST+2:exportContact();break;
        }
        return super.onContextItemSelected(item);
    }

    private void exportContact() {
        ThreadUtils.runInThread(new Runnable() {
            @Override
            public void run() {
                for (Contact contact : mContactList) {
                    if (contact.getChecked()==true) {
                        Cursor cursor = getContentResolver().query(ContactProvider.URI_CONTACT,
                                null,
                                null,
                                null,
                                null);
                        while (cursor.moveToNext()) {
                            L.d("woshitrue"+contact.getName());
                            init(contact.getName(),contact.getPhone());
                        }
                        cursor.close();
                    }
                }
                finish();
            }
        });
    }

    private void init(String name,String phonenumber) {
        //内容解析者
        ContentResolver resolver = getContentResolver();
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri datauri = Uri.parse("content://com.android.contacts/data");
        Cursor cursor = resolver.query(uri, null, null, null, null);
        int count = cursor.getCount();
        ContentValues values = new ContentValues();
        int contact_id = count + 1;
        values.put("contact_id", contact_id);
        resolver.insert(uri, values);
        //插入具体的数据到data表，数据类型，data1具体的数据
        ContentValues namevalue = new ContentValues();
        namevalue.put("mimetype", "vnd.android.cursor.item/name"); // 指定数据类型
        namevalue.put("data1", name);
        namevalue.put("raw_contact_id", contact_id); // 一定要记得指定数据属于哪个联系人
        resolver.insert(datauri, namevalue);

        ContentValues phonevalue = new ContentValues();
        phonevalue.put("mimetype", "vnd.android.cursor.item/phone_v2");
        phonevalue.put("data1",phonenumber);
        phonevalue.put("raw_contact_id", contact_id);
        resolver.insert(datauri, phonevalue);

    }


    private void selectAll() {
        Cursor cursor = getContentResolver().query(ContactProvider.URI_CONTACT,
                null,
                null,
                null,
                null);
        if (cursor.getCount() == 0) {
            return;
        }
        while (cursor.moveToNext()) {
            //创建一个联系人对象
            Contact contact = new Contact(cursor.getString(1),cursor.getString(2), cursor.getString(3), false);
            //将contact加入到contacts集合中
            mContactList.add(contact);
        }
        cursor.close();
        for (Contact contact:mContactList) {
            contact.setChecked(true);
        }
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run(){
                mmMyAdapter=new MyAdapter(ExportContactActivity.this,mContactList);
                mExportLv.setAdapter(mmMyAdapter);
            }
        });

    }

    private void unSelectAll() {
        for (Contact contact:mContactList) {
            contact.setChecked(false);
        }
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run(){
                mmMyAdapter=new MyAdapter(ExportContactActivity.this,mContactList);
                mExportLv.setAdapter(mmMyAdapter);
            }
        });
    }

    private void initDate() {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                if (eMyAdapter != null) {
                    eMyAdapter.getCursor().requery();
                    return;
                }
                Cursor cursor = getContentResolver().query(ContactProvider.URI_CONTACT,
                        null,
                        null,
                        null,
                        null);
                if (cursor.getCount() == 0) {
                    return;
                }
                eMyAdapter = new CursorAdapter(ExportContactActivity.this,
                        cursor,
                        true) {
                    @Override
                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
                        View view = View.inflate(context, R.layout.contactlist_checkbox, null);
                        return view;
                    }

                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        TextView nameTv = view.findViewById(R.id.importNameTv);
                        TextView phoneTv = view.findViewById(R.id.importPhoneTv);
                        TextView emailTv = view.findViewById(R.id.importEmailTv);
                        nameTv.setText(cursor.getString(1));
                        phoneTv.setText(cursor.getString(2));
                        emailTv.setText(cursor.getString(3));

                    }
                };
                mExportLv.setAdapter(eMyAdapter);
            }
        });
    }
}