package com.android.person;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.R;
import com.android.adapter.MsgAdapter;
import com.android.guide.BaseActivity;
import com.android.model.Private;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommunicateActivity extends BaseActivity {

    @BindView(R.id.back_btn)
    ImageView mBackBtn;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.msg_recycle_view)
    RecyclerView mMsgRecycleView;
    @BindView(R.id.input_text)
    EditText mInputText;
    @BindView(R.id.send_btn)
    Button mSendBtn;
    @BindView(R.id.user_center)
    ImageView mUserCenter;

    private MsgAdapter adapter;
    private List<Private> msgs = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMsgRecycleView.setLayoutManager(layoutManager);
        msgs = new ArrayList();
        adapter = new MsgAdapter(msgs);
        mMsgRecycleView.setAdapter(adapter);

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mInputText.getText().toString();
                if(!"".equals(msg)) {
                    msgs.add(new Private(Private.SEND_TYPE,msg));
                    adapter.notifyItemInserted(msgs.size() - 1);
                    mMsgRecycleView.scrollToPosition(msgs.size() - 1); //定位到最后一条数据
                    mInputText.setText("");
                }
            }
        });
    }
}
