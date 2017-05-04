package com.android.status;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.GlobalApplication;
import com.android.R;
import com.android.adapter.CommentListAdapter;
import com.android.model.Comment;
import com.android.status.picture.DetailActivity;
import com.android.tool.MyStringRequest;
import com.android.tool.NetworkConnectStatus;
import com.android.tool.ProgressHUD;
import com.android.tool.ReplyDialog;
import com.android.tool.ReportUtil;
import com.android.tool.VolleyRequestParams;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论列表
 */
public class CommentFragment extends Fragment {

    private CommentListAdapter mCommentAdapter;

    private RecyclerView mRvComment;
    private List<Comment> mComments = new ArrayList();
    private NetworkConnectStatus networkStatus;//网络连接状态
    private MyStringRequest mStringRequest;
    private RequestQueue mQueue;
    private ProgressHUD mProgressHUD;

    private int commentsTotal; //评论总的条数
/*    private int currentPage;  //加载的当前页面
    private int pageCount;    //每页的记录数*/
    private int loadedCount;   //已加载的条数
    private Boolean isLoadData = false;
    private int mCommentType;//评论类型
    private int mAttachId;
    private int mAttachCreatorId;//发布活动/通知/照片者的id

    private String root;

    public int getLoadedCount() {
        return mComments.size();
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null)
        {
           /* mDiscussType = bundle.getInt(DISCUSS_TYPE);
            mDiscussId = bundle.getInt(DISCUSS_ID);*/
            //Intent intent = new Intent();
            mCommentType = bundle.getInt("commentType");
            mAttachId = bundle.getInt("attachId");
            mAttachCreatorId = bundle.getInt("creatorId");
        }

    }
    public static CommentFragment newInstance(int commentType ,int attachId, int creatorId)
    {
        Bundle bundle = new Bundle();
        bundle.putInt("commentType", commentType);
        bundle.putInt("attachId", attachId);
        bundle.putInt("creatorId", creatorId);
        CommentFragment commentFragment = new CommentFragment();
        commentFragment.setArguments(bundle);
        return commentFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discuss, container, false);
        mRvComment = (RecyclerView) view.findViewById(R.id.rv_comment);
        mQueue = GlobalApplication.get().getRequestQueue();
        networkStatus = new NetworkConnectStatus(getActivity());
        mComments = new ArrayList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRvComment.setLayoutManager(layoutManager);
        mCommentAdapter = new CommentListAdapter(getActivity(), mAttachCreatorId, mComments, new CommentListAdapter.MyItemHandle() {
            @Override
            public void handleReply(final int position) {
                final ReplyDialog replyDialog = new ReplyDialog(getActivity());
                replyDialog.setHintText("回复评论...")
                        .setOnBtnCommitClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {   //回复键的回调函数
                                replyDialog.dismiss();
                                Log.d("XXX", replyDialog.getContent());
                                addMyComment(mComments.get(position).getId(), replyDialog.getContent());
                            }
                        })
                        .show();
            }

            @Override
            public void handleDelete(final int position) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("确定要删除该评论？");
                //dialog.setMessage("确定要删除该评论？");
                dialog.setCancelable(true);
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (networkStatus.isConnectInternet()) {
                            mProgressHUD = ProgressHUD.show(getActivity(), "删除中...", true, true, new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mProgressHUD.dismiss();
                                }
                            });
                            String rootString = getResources().getString(R.string.ROOT) + "msg/comment/" + mComments.get(position).getId();
                            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                                    .with("token", GlobalApplication.getToken());
                            mStringRequest = new MyStringRequest(Request.Method.DELETE, rootString, headerParams, null,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            if ("null".equals(response) || null == response) {
                                                //登录未获取响应，登录失败
                                                mProgressHUD.dismiss();
                                                Toast.makeText(getActivity(), "删除失败".toString(), Toast.LENGTH_SHORT).show();

                                                return;
                                            }
                                            Log.d("PublishActivity:TAG", response);
                                            mComments.remove(position);
                                            mCommentAdapter.notifyDataSetChanged();
                                            commentsTotal--;
                                            if(mCommentType == Comment.ACTIVITY_TYPE) {
                                                ((com.android.activity.DetailActivity) getActivity()).setCommentCount(commentsTotal);
                                            }else if(mCommentType == Comment.NOTIFICATION_TYPE) {
                                                ((com.android.remind.notification.DetailActivity) getActivity()).setCommentCount(commentsTotal);
                                            } else if(mCommentType == Comment.PHOTO_TYPE) {
                                                ((DetailActivity) getActivity()).setCommentCount(commentsTotal);
                                            }
                                            Toast.makeText(getActivity(), "已删除".toString(), Toast.LENGTH_SHORT).show();
                                            mProgressHUD.dismiss();
                                        }
                                    }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                mProgressHUD.dismiss();
                                                Log.e("PublishActivity:TAG", error.getMessage(), error);
                                                byte[] htmlBodyBytes = error.networkResponse.data;
                                                Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                                                Toast.makeText(getActivity(), "网络超时".toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            mQueue = GlobalApplication.get().getRequestQueue();
                            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400*1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            mQueue.add(mStringRequest);
                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog.show();
            }

            @Override
            public void handleReport(final int position) {
                new ReportUtil(getActivity(), ReportUtil.TYPE_COMMENT, mComments.get(position).getId()).doReport();
            }
        });   //适配器初始化
        mRvComment.setAdapter(mCommentAdapter);
        return view;
    }




    /**
     * 回复内容
     * @param content
     */
    public void addMyComment(int parentId, String content) {
        if (networkStatus.isConnectInternet()) {
            mProgressHUD = ProgressHUD.show(getActivity(), "发布中...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mProgressHUD.dismiss();
                }
            });
            String rootString = getResources().getString(R.string.ROOT) + "msg/comment";
            VolleyRequestParams bodyParams = new VolleyRequestParams()
                    .with("attach_type", String.valueOf(mCommentType))
                    .with("attach_id", String.valueOf(mAttachId))
                    .with("parent", String.valueOf(parentId))
                    .with("content", String.valueOf(content));

            VolleyRequestParams headerParams = new VolleyRequestParams() //URL上的参数
                    .with("token", GlobalApplication.getToken())
                    .with("Accept", "application/json"); // 数据格式设置为json
            mStringRequest = new MyStringRequest(Request.Method.POST, rootString, headerParams, bodyParams,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if ("null".equals(response) || null == response) {
                                //登录未获取响应，登录失败
                                mProgressHUD.dismiss();
                                Toast.makeText(getActivity(), "发布失败".toString(), Toast.LENGTH_SHORT).show();

                                return;
                            }
                            try {
                                Log.d("PublishActivity:TAG", response);
                                mProgressHUD.dismiss();

                                JSONObject jsonObject1 = new JSONObject(response);
                                try {
                                        commentsTotal++;//总数
                                        Comment comment = new Comment();
                                        comment.setId(jsonObject1.getInt("id"));//评论id
                                        comment.setCreatorId(jsonObject1.getInt("creator"));
                                        comment.setAvatarId(jsonObject1.getJSONObject("creator_obj").getInt("avatar"));
                                        comment.setName(jsonObject1.getJSONObject("creator_obj").getString("name"));
                                        comment.setCreatedTime(jsonObject1.getLong("created_at"));
                                        comment.setContent(jsonObject1.getString("content"));
                                        comment.setParentId(jsonObject1.getInt("parent"));
                                        mComments.add(0,comment);
                                        mCommentAdapter.notifyDataSetChanged();

                                    //适配器中添加数据项
                                        if(!isLoadData) {   //还没有加载数据
                                            isLoadData = true;
                                            loadedCount = 1;//已加载数
                                        }

                                        if(mCommentType == Comment.ACTIVITY_TYPE) {
                                            ((com.android.activity.DetailActivity) getActivity()).setCommentCount(commentsTotal);
                                        } else if(mCommentType == Comment.NOTIFICATION_TYPE) {
                                            ((com.android.remind.notification.DetailActivity) getActivity()).setCommentCount(commentsTotal);
                                        } else if(mCommentType == Comment.PHOTO_TYPE) {
                                            ((DetailActivity) getActivity()).setCommentCount(commentsTotal);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.d("getTIMELINE:TAG", e.toString());
                                }
                                //Toast.makeText(PublishActivity.this, "发布完成".toString(), Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                e.printStackTrace();
                                           /* Log.e("PublishActivity:TAG", error.getMessage(), error);
                                            byte[] htmlBodyBytes = error.networkResponse.data;
                                            Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);*/
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressHUD.dismiss();
                    Log.e("PublishActivity:TAG", error.getMessage(), error);
                    byte[] htmlBodyBytes = error.networkResponse.data;
                    Log.e("PublishActivity:TAG", new String(htmlBodyBytes), error);
                    Toast.makeText(getActivity(), "网络超时".toString(), Toast.LENGTH_SHORT).show();
                }
            });
            mQueue = GlobalApplication.get().getRequestQueue();
            mStringRequest.setRetryPolicy(new DefaultRetryPolicy(400*1000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(mStringRequest);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.network_fail).toString(), Toast.LENGTH_SHORT).show();
        }
    }
    public void addData(JSONObject jsObject) {
        System.out.println("comment(Json)"+jsObject.toString());
        try {
            commentsTotal = jsObject.getInt("total");//总数
            if(commentsTotal <= mComments.size() ) {
               // Toast.makeText(getActivity(), "评论已加载完！", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONArray jsonArray = jsObject.getJSONArray("comments");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
               // System.out.println("comments:"+jsonObject1.toString());
                Comment comment = new Comment();
                comment.setId(jsonObject1.getInt("id"));//评论id
                comment.setCreatorId(jsonObject1.getInt("creator"));
                comment.setAvatarId(jsonObject1.getJSONObject("creator_obj").getInt("avatar"));
                comment.setName(jsonObject1.getJSONObject("creator_obj").getString("name"));
                comment.setCreatedTime(jsonObject1.getLong("created_at"));
                comment.setContent(jsonObject1.getString("content"));
                comment.setParentId(jsonObject1.getInt("parent"));
                mComments.add(comment);
                //适配器中添加数据项
            }
            if(jsonArray.length() > 0) {
                isLoadData = true;
                loadedCount = mComments.size();//已加载数
                mCommentAdapter.notifyDataSetChanged();
            }
        }catch (Exception e) {
            e.printStackTrace();
            Log.d("getTIMELINE:TAG", e.toString());
        }
    }
}
