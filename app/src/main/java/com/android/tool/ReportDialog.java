package com.android.tool;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.R;

/**
 * 回复对话框
 *
 * @author Jaiky
 * @date 2015-3-30 PS: Not easy to write code, please indicate.
 */
public class ReportDialog extends Dialog {

    TextView mTitle;
    EditText mReportContent;
    LinearLayout mPositiveButton;
    LinearLayout mNegativeButton;

    private Context mContext;

    public ReportDialog(Context context) {
        super(context, R.style.MyNoFrame_Dialog);
        mContext = context;
        init();
    }

    private ReportDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        init();
    }

    private void init() {
        setContentView(R.layout.dialog_report);

        // 设置宽度
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        mTitle = (TextView) findViewById(R.id.title);
        mReportContent = (EditText) findViewById(R.id.report_content);
        mPositiveButton = (LinearLayout) findViewById(R.id.positiveButton);
        mNegativeButton = (LinearLayout) findViewById(R.id.negativeButton);

        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportDialog.this.dismiss();
            }
        });
        // 弹出键盘
        mReportContent.setFocusable(true);
        mReportContent.setFocusableInTouchMode(true);
        mReportContent.requestFocus();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mReportContent, 0);
            }
        }, 200);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ReportDialog setTitle(String title) {
        mTitle.setText(title);
        return this;
    }
    public ReportDialog setContent(String content) {
        mReportContent.setText(content);
        return this;
    }

    //设置评论框提示
    public ReportDialog setHintText(String hint) {
        mReportContent.setHint(hint);
        return this;
    }

    //获取评论框内容
    public String getContent() {
        return mReportContent.getText().toString();
    }

    //设置回复按钮回调
    public ReportDialog setOnBtnCommitClickListener(
            View.OnClickListener onClickListener) {
        mPositiveButton.setOnClickListener(onClickListener);
        return this;
    }
}



