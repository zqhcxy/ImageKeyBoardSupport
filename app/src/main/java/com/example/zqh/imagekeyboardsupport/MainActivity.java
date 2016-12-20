package com.example.zqh.imagekeyboardsupport;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.os.BuildCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Arrays;

/**
 * 设置系统软件盘可以选择图片
 * <p> 需要v13支持库
 *
 */
public class MainActivity extends AppCompatActivity {

    private EditText img_edit;
    private LinearLayout edit_ly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        img_edit=(EditText)findViewById(R.id.img_edit);
        edit_ly=(LinearLayout)findViewById(R.id.edit_ly);
        EditText editText = new EditText(this) {
            @Override
            public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
                final InputConnection ic = super.onCreateInputConnection(editorInfo);
                EditorInfoCompat.setContentMimeTypes(editorInfo,
                        new String[]{"image/png", "image/gif", "image/jpeg", "image/webp"});

                final InputConnectionCompat.OnCommitContentListener callback =
                        new InputConnectionCompat.OnCommitContentListener() {
                            @Override
                            public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                           int flags, Bundle opts) {
                                // read and display inputContentInfo asynchronously
                                if (BuildCompat.isAtLeastNMR1() && (flags &
                                        InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                                    try {
                                        inputContentInfo.requestPermission();
                                    }
                                    catch (Exception e) {
                                        return false; // return false if failed
                                    }
                                }
                                Uri likUri=inputContentInfo.getLinkUri();
                                Uri contentUri=inputContentInfo.getContentUri();
                                Log.i("ImputTest","linkUri: "+likUri.toString()+"\n"+"ContentUri: "+contentUri);
                                // read and display inputContentInfo asynchronously.
                                // call inputContentInfo.releasePermission() as needed.

                                return true;  // return true if succeeded
                            }
                        };
                return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
            }
        };
        edit_ly.addView(editText);
    }
}