package com.example.zqh.imagekeyboardsupport;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

/**
 * 设置系统软件盘可以选择图片
 * <p> 需要v13支持库
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "KeyBoardImage";
    private KeyBoardImageEditText keyboard_edit;
    private ImageView attach_iv;

    private ImageKeyBoardAnyTask imageKeyBoardAnyTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keyboard_edit = (KeyBoardImageEditText) findViewById(R.id.keyboard_edit);
        attach_iv = (ImageView) findViewById(R.id.attach_iv);
        keyboard_edit.setKeyBoardInputCallbackListener(new KeyBoardImageEditText.KeyBoardInputCallbackListener() {
            @Override
            public void onCommitContent(InputContentInfoCompat inputContentInfo, int flags, Bundle opts) {
                if (imageKeyBoardAnyTask != null) {
                    imageKeyBoardAnyTask.cancel(true);
                    imageKeyBoardAnyTask = null;
                }
                imageKeyBoardAnyTask = new ImageKeyBoardAnyTask();
                imageKeyBoardAnyTask.execute(inputContentInfo);
            }
        });
    }

    /**
     * 创建目标文件，要保存的文件
     *
     * @param extension
     * @return
     */
    public static File buildImageKeyboardSupportUri(final String extension) {
        final long fileId = System.currentTimeMillis();
        String filePath = Environment.getExternalStorageDirectory().toString() +
                "/keyboardImage/.shared/iks" + String.valueOf(fileId) +
                (TextUtils.isEmpty(extension) ? "" : ("." + extension));
        checkAndCreateDir(filePath);
        return new File(filePath);
    }

    /**
     * 文件夹是否存在，否则创建
     *
     * @param filePath
     * @return
     */
    public static boolean checkAndCreateDir(String filePath) {
        File tmp = new File(filePath);
        File parentFile = tmp.getParentFile();

        if (!parentFile.exists() && !parentFile.mkdirs()) {
            Log.d("", "mkdir:" + parentFile.getPath() + " error!");
            return false;
        }
        return true;

    }

    /**
     * 复制文件
     *
     * @param fileFromPath
     * @param fileToPath
     * @throws Exception
     */
    public static void copyFile(String fileFromPath, String fileToPath)
            throws Exception {
        InputStream in = null;
        OutputStream out = null;

        File f = new File(fileToPath);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }

        int bufferSize = 8196;
        try {
            in = new FileInputStream(fileFromPath);
            out = new FileOutputStream(fileToPath);

            int bytesRead = 0;
            byte[] buffer = new byte[bufferSize];
            while ((bytesRead = in.read(buffer, 0, bufferSize)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }


    /**
     * 点击软键盘上的图片进行的回调处理
     */
    private class ImageKeyBoardAnyTask extends AsyncTask<InputContentInfoCompat, Long, Integer> {
        File file1;
        String newFilepath;

        private static final int Error = -1;
        private static final int Success = 1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(InputContentInfoCompat... inputContentInfoCompats) {
            InputContentInfoCompat inputContentInfo = inputContentInfoCompats[0];
            Uri likUri = inputContentInfo.getLinkUri();
            int count = inputContentInfo.getDescription().getMimeTypeCount();
            String ext = null;
            for (int i = 0; i < count; i++) {
                String mimetype = inputContentInfo.getDescription().getMimeType(i);
                if (!TextUtils.isEmpty(mimetype)) {
                    ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype);
                    if (!TextUtils.isEmpty(ext)) break;
                }
            }
            if (TextUtils.isEmpty(ext)) {
                Log.i(TAG, "ext is empty!");
                return Error;
            }
            final Uri contentUri = inputContentInfo.getContentUri();
            try {
                File file = Glide.with(MainActivity.this)
                        .load(contentUri).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                file1 = buildImageKeyboardSupportUri(ext);
                newFilepath = file1.getAbsolutePath();
                copyFile(file.getAbsolutePath(), newFilepath);
                Log.i(TAG, "Copy Success!");
            } catch (InterruptedException e) {
                e.printStackTrace();
                return Error;
            } catch (ExecutionException e) {
                e.printStackTrace();
                return Error;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return Success;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == Error) {
                Log.i(TAG, "Copy error");
                return;
            }
            Glide.with(MainActivity.this).load(Uri.fromFile(new File(newFilepath)))
                    .placeholder(R.mipmap.ic_launcher)
                    .listener(new RequestListener<Uri, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                            Log.i("ImputTest", "error: " + e.getMessage());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Log.i("ImputTest", "success: ");
                            return false;
                        }
                    })
                    .into(attach_iv);
//            setattachment(PHOTO_REQUST, "file://" +file1.getAbsolutePath(), true);
            cancel(true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageKeyBoardAnyTask != null) {
            imageKeyBoardAnyTask.cancel(true);
            imageKeyBoardAnyTask = null;
        }
    }
}
