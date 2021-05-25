package me.loyio.mycontacts.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import me.loyio.mycontacts.R;

public class MainLayout extends LinearLayout {
    public MainLayout(Context context, AttributeSet attrs) {
        super(context,attrs);
        LayoutInflater.from(context).inflate(R.layout.title,this);
    }
}
