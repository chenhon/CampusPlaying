package com.android.tool;

import android.app.Activity;

import com.android.R;
import com.android.GlobalApplication;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * Created by Administrator on 2017/4/13 0013.
 */

public class ShareUtil {
    //分享功能调用函数
    //mobile.html?id=3
    public static void showShare(Activity activity,int aid, String title) {
        ShareSDK.initSDK(activity);
        String url = GlobalApplication.get().getResources().getString(R.string.ROOT) + "mobile.html?id=" + aid;
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
        oks.setTitle(title);
        // titleUrl是标题的网络链接，QQ和QQ空间等使用
        oks.setTitleUrl(url);
        // text是分享文本，所有平台都需要这个字段
        oks.setText("校园约完网");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("让校园约完变得更加便捷");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(activity.getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(url);

// 启动分享GUI
        oks.show(activity);
    }
}
