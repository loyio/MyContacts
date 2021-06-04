package me.loyio.mycontacts.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.loyio.mycontacts.R;
import me.loyio.mycontacts.provider.ContactProvider;
import me.loyio.mycontacts.utils.L;
import me.loyio.mycontacts.utils.ThreadUtils;
import me.loyio.mycontacts.utils.ToastUtils;

public class MainActivity extends AppCompatActivity {

    private ListView mMainLv;
    private MyContentObserver mMyContentObserver = new MyContentObserver(new Handler());
    private Cursor mcurrentCursor;
    private CursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainLv = findViewById(R.id.mainLv);
        L.d("onCreate start");
        initData();
        initListener();
    }

    private void initListener() {
        registerContentObserver();
        registerForContextMenu(mMainLv);
        mMainLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                mcurrentCursor = (Cursor) adapterView.getItemAtPosition(i);
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        L.d("onDestory start");
        super.onDestroy();
        unRegistContentObserver();
    }

    private void initData() {
        setOrUpdateAdapter();
    }

    private void setOrUpdateAdapter() {
        //涉及到UI变化，显示用UI
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                if (mCursorAdapter != null) {
                    mCursorAdapter.getCursor().requery();
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
                mCursorAdapter = new CursorAdapter(MainActivity.this,
                        cursor,
                        true) {
                    @Override
                    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                        View view = View.inflate(context, R.layout.data_list, null);
                        return view;
                    }
                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        TextView nameTv = view.findViewById(R.id.nameTv_datalist);
                        TextView phoneTv = view.findViewById(R.id.phoneTv_datalist);
                        TextView emailTv = view.findViewById(R.id.emailTv_datalist);
                        TextView qqTv = view.findViewById(R.id.qqTv_datalist);
                        nameTv.setText(cursor.getString(1));
                        phoneTv.setText(cursor.getString(2));
                        emailTv.setText(cursor.getString(3));
                        qqTv.setText(cursor.getString(4));
                    }
                };
                mMainLv.setAdapter(mCursorAdapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        L.d("onCreateOptionsMenu start");
        menu.add(1, Menu.FIRST, 1, "新建联系人").setIcon(resizeImage(R.drawable.contact_add, 150, 150));
        menu.add(1, Menu.FIRST + 1, 2, "查找联系人").setIcon(resizeImage(R.drawable.contact_search, 150, 150));
        menu.add(1, Menu.FIRST + 2, 3, "导入联系人").setIcon(resizeImage(R.drawable.contact_import, 150, 150));
        menu.add(1, Menu.FIRST + 3, 4, "导出联系人").setIcon(resizeImage(R.drawable.contact_export, 150, 150));
        menu.add(1, Menu.FIRST + 4, 5, "修改密码").setIcon(resizeImage(R.drawable.contact_my, 150, 150));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST:
                addContact();
                break;
            case Menu.FIRST + 1:
                searchContact();
                break;
            case Menu.FIRST + 2:
                importContact();
                break;
            case Menu.FIRST + 3:
                exportContact();
                break;
            case Menu.FIRST + 4:
                repassWord();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void importContact() {
        Intent intent=new Intent(MainActivity.this,ImportContactActivity.class);
        startActivity(intent);
    }

    private void repassWord() {
        final EditText rePassWord = new EditText(MainActivity.this);
        //组件之一提示对话框AlertDialog.Builder
        final AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("请输入要修改的密码")
                .setView(rePassWord)
                .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                        String password=rePassWord.getText().toString().trim();
                        editor.putString("password",password);
                        editor.commit();
                        Toast.makeText(MainActivity.this,"succeed",Toast.LENGTH_SHORT).show(); }
                }).show();


    }

    private void exportContact() {
        Intent intent=new Intent(MainActivity.this,ExportContactActivity.class);
        startActivity(intent);
    }

    private void searchContact() {
        final EditText inputNameTv = new EditText(MainActivity.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("请输入要查找联系人的姓名")
                .setView(inputNameTv)
                .setPositiveButton("查找", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        searchResultShow(inputNameTv.getText().toString().trim());
                    }
                }).show();
    }

    private void searchResultShow(final String name) {
        ThreadUtils.runInUIThread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getContentResolver().query(ContactProvider.URI_CONTACT,
                        null,
                        "name like ?",
                        new String[]{"%" + name + ""},
                        null);
                if (cursor.getCount() == 0) {
                    ToastUtils.showToastSafe(MainActivity.this, "没有找到该联系人");
                    return;
                }
                CursorAdapter searchAdapter = new CursorAdapter(MainActivity.this, cursor, true) {
                    @Override
                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
                        View view = View.inflate(context, R.layout.data_list, null);
                        return view;
                    }

                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        TextView nameTv = view.findViewById(R.id.nameTv_datalist);
                        TextView phoneTv = view.findViewById(R.id.phoneTv_datalist);
                        TextView emailTv = view.findViewById(R.id.emailTv_datalist);
                        TextView qqTv = view.findViewById(R.id.qqTv_datalist);
                        nameTv.setText(cursor.getString(1));
                        phoneTv.setText(cursor.getString(2));
                        emailTv.setText(cursor.getString(3));
                        qqTv.setText(cursor.getString(4));

                    }
                };
                mMainLv.setAdapter(searchAdapter);
            }
        });
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("联系人操作");
        L.d("onCreateContextMenu start");
        menu.add(Menu.NONE, Menu.FIRST, 1, "编辑联系人");
        menu.add(Menu.NONE, Menu.FIRST + 1, 2, "删除联系人");
        menu.add(Menu.NONE, Menu.FIRST + 2, 3, "拨打电话");
        menu.add(Menu.NONE, Menu.FIRST + 3, 4, "发送短信");
        menu.add(Menu.NONE, Menu.FIRST + 4, 5, "发送邮件");
        menu.add(Menu.NONE, Menu.FIRST + 5, 6, "显示全部");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST:
                editContact();
                break;
            case Menu.FIRST + 1:
                delContact();
                break;
            case Menu.FIRST + 2:
                dial();
                break;
            case Menu.FIRST + 3:
                sms();
                break;
            case Menu.FIRST + 4:
                email();
                break;
            case Menu.FIRST + 5:
                setOrUpdateAdapter();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void editContact() {
        Intent intent = new Intent(MainActivity.this, EditContactActivity.class);
        intent.putExtra("name", mcurrentCursor.getString(1));
        intent.putExtra("phone", mcurrentCursor.getString(2));
        intent.putExtra("email", mcurrentCursor.getString(3));
        intent.putExtra("qq", mcurrentCursor.getString(4));
        L.d("editContact start");
        startActivity(intent);
    }


    private void delContact() {
        getContentResolver().delete(ContactProvider.URI_CONTACT,
                "name=?",
                new String[]{mcurrentCursor.getString(1)});
        ToastUtils.showToastSafe(this, "该联系人已删除！");
    }

    private void dial() {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},1);
        }else{
            Intent intent = new Intent(Intent.ACTION_CALL);
            Uri uri = Uri.parse("tel:" + mcurrentCursor.getString(2));
            intent.setData(uri);
            startActivity(intent);
        }

    }

    private void sms() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:"+mcurrentCursor.getString(2)));
        startActivity(intent);
    }

    private void email() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"+mcurrentCursor.getString(3)));
        startActivity(intent);
    }

    private void addContact() {
        Intent intent=new Intent(MainActivity.this,AddContactActivity.class);
        startActivity(intent);
    }

    /**
     * Display Images using reflection mechanism
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if  (menu!=null)
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")){
                try {
                    Method method=menu.getClass().getDeclaredMethod("setOptionalIconsVisible",Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu,true);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        return super.onMenuOpened(featureId, menu);
    }

    /**
     * Listen to the database, Like the Trigger
     */
    class MyContentObserver extends ContentObserver {

        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            setOrUpdateAdapter();
        }
    }

    /**
     *  Register a ContentObserver-derived class instance for the specified Uri, and when the given Uri changes, the instance object will be called back for processing.
     */
    private void registerContentObserver(){
        L.d("registerContentObserver start");
        getContentResolver().registerContentObserver(ContactProvider.URI_CONTACT,
                true,
                mMyContentObserver);
    }

    /**
     * Cancel the observation of a given Uri
     */
    private void unRegistContentObserver(){
        L.d("unregisterContentObserver start");
        getContentResolver().unregisterContentObserver(mMyContentObserver);
    }

    private Drawable resizeImage(int resId, int w, int h) {
        // load the origial Bitmap
        Bitmap BitmapOrg = BitmapFactory.decodeResource(getResources(), resId);
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;
        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0,width, height, matrix, true);
        return new BitmapDrawable(resizedBitmap);
    }
}