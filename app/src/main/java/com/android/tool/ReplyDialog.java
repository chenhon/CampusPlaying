package com.android.tool;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.R;

/**
 * 回复对话框
 *
 * @author Jaiky
 * @date 2015-3-30 PS: Not easy to write code, please indicate.
 */
public class ReplyDialog extends Dialog {

    private EditText etContent;
    private LinearLayout llBtnReply;

    private Context mContext;

    public ReplyDialog(Context context) {
        super(context, R.style.MyNoFrame_Dialog);
        mContext = context;
        init();
    }

    private ReplyDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_replyform);

        // 设置宽度
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        etContent = (EditText) findViewById(R.id.dialog_reply_etContent);
        llBtnReply = (LinearLayout) findViewById(R.id.dialog_reply_llBtnReply);

        // 弹出键盘
        etContent.setFocusable(true);
        etContent.setFocusableInTouchMode(true);
        etContent.requestFocus();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(etContent, 0);
            }
        }, 200);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ReplyDialog setContent(String content) {
        etContent.setText(content);
        return this;
    }

    //设置评论框提示
    public ReplyDialog setHintText(String hint) {
        etContent.setHint(hint);
        return this;
    }

    //获取评论框内容
    public String getContent() {
        return etContent.getText().toString();
    }

    //设置回复按钮回调
    public ReplyDialog setOnBtnCommitClickListener(
            android.view.View.OnClickListener onClickListener) {
        llBtnReply.setOnClickListener(onClickListener);
        return this;
    }
}


/*    final ReplyDialog replyDialog = new ReplyDialog(MainActivity.this);
replyDialog.setHintText("回复某人的评论...")
        .setOnBtnCommitClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        replyDialog.dismiss();
        Log.d("XXX", replyDialog.getContent());
        tvShow.setText(Html.fromHtml(replyDialog.getContent(), mFaceImageGetter, null));
        }
        })
        .show();*/
