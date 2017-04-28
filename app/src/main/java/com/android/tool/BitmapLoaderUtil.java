package com.android.tool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.R;
import com.android.GlobalApplication;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ChenHong on 2017/4/22 0022.
 */
public class BitmapLoaderUtil {

    public static final String TYPE_SMALL = "small";     //100 * 75
    public static final String TYPE_MEDIAN = "median";   //640 * 480
    public static final String TYPE_LARGE = "large";     //1280 * 960
    public static final String TYPE_ORIGINAL = "orig";
    private static BitmapLoaderUtil ourInstance = new BitmapLoaderUtil();

    private static Map<String, List<ImageView>> mImageMap = new HashMap();
    /**
     * 内存图片软引用缓冲
     */
    private HashMap<String, SoftReference<Bitmap>> imageCache = null;

    public static BitmapLoaderUtil getInstance() {
        return ourInstance;
    }

    private BitmapLoaderUtil() {
        imageCache = new HashMap<String, SoftReference<Bitmap>>();
    }

    /**
     * 根据图片类型和图片id来获取图片名
     * @param ImageType   图片类型
     * @param pid      图片id
     * @return
     */
    public String getImageURL(String ImageType, int pid) {
        return "campusplaying_"+ImageType + pid + ".jpg";
    }

    /**
     * 获取图片
     * @param imageView
     * @param ImageType 图片类型
     * @param pid   图片id
     */
    public void getImage(ImageView imageView, String ImageType, int pid) {
        String imageURL = getImageURL(ImageType, pid);
        //该图片已经在获取中
        if(mImageMap.containsKey(imageURL)) {
            List<ImageView> imageViewlist = mImageMap.get(imageURL);
            imageViewlist.add(imageView);//将容器加进来
            System.out.println("该图片已经在获取中");
            return ;
        }
        //在内存缓存中
        if(imageCache.containsKey(imageURL))
        {
            SoftReference<Bitmap> reference = imageCache.get(imageURL);
            Bitmap bitmap = reference.get();
            if(bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
                System.out.println("该图片(pid_"+pid+")在内存缓存中");
                return;
            }
        }

        //从本地缓存中查找
        File cacheDir = GlobalApplication.get().getExternalCacheDir();
        File[] cacheFiles = cacheDir.listFiles();
        String cachePath = cacheDir.getParent() + java.io.File.separator + cacheDir.getName();
        System.out.println("本地图片路径1：" + cachePath);
        if(null!=cacheFiles){
            for(int i = 0; i<cacheFiles.length; i++) //遍历是否有该图片了
            {
                System.out.println("imageURL-" + imageURL+ "(" + cacheFiles[i].getName() +")");
                if(imageURL.equals(cacheFiles[i].getName()))
                {   //根据图片完整路径获取到图片
                    System.out.println("本地图片路:2：" + cachePath + java.io.File.separator + imageURL);
                    Bitmap cacheImage = BitmapFactory.decodeFile(cachePath + java.io.File.separator + imageURL);
                    imageView.setImageBitmap(cacheImage);
                    imageCache.put(imageURL, new SoftReference<Bitmap>(cacheImage)); //放入内存缓存
                    return;
                }
            }
        }

        //下载图片
        List<ImageView> imageViews = new ArrayList<ImageView>();
        imageViews.add(imageView);
        mImageMap.put(imageURL, imageViews);
        imageLoad(ImageType, pid);
    }
    void imageLoad(final String ImageType, final int pid) {

        MyImageRequest avatarImageRequest = new MyImageRequest(
                GlobalApplication.get().getResources().getString(R.string.ROOT) + "media/" + pid
                , new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                Bitmap bitmap = null;
                switch (ImageType) {   //对图片进行压缩
                    case TYPE_SMALL:
                        bitmap = ImageUtils.comp(response,0,100,750);
                        break;
                    case TYPE_MEDIAN:
                        bitmap = ImageUtils.comp(response,0,640,480);
                        break;
                    case TYPE_LARGE:
                        bitmap = ImageUtils.comp(response,0,1280,960);
                        break;
                    case TYPE_ORIGINAL:
                        bitmap = ImageUtils.comp(response,0,150,150);//头像
                        break;
                    default :
                       // bitmap = BitmapFactory.decodeResource( GlobalApplication.get().getResources(), R.drawable.campus_playing_app_icon);
                        break;
                }
                if(bitmap == null) {
                    Toast.makeText(GlobalApplication.get(),"图片加载失败", Toast.LENGTH_SHORT).show();
                }
                String imageURL = getImageURL(ImageType, pid);
                saveLoadedImage(imageURL,bitmap); //将图片缓存

                List<ImageView> imageViewlist = mImageMap.get(imageURL);
                for (ImageView iv : imageViewlist){  //将加载的图片放入bitmap中去
                    iv.setImageBitmap(bitmap);
                }
                mImageMap.remove(imageURL);
            }
        }, 0, 0, Bitmap.Config.RGB_565
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(GlobalApplication.get(),"图片加载失败", Toast.LENGTH_SHORT).show();
            }
        });
        avatarImageRequest.addHeader("type", ImageType);
        GlobalApplication.get().getRequestQueue().add(avatarImageRequest);
    }

    private void saveLoadedImage(String imageURL,Bitmap bitmap) {
        imageCache.put(imageURL, new SoftReference<Bitmap>(bitmap));  //在内存缓存"my_upload_image.jpg"
        File file = new File(GlobalApplication.get().getExternalCacheDir(), imageURL);//生成文件
        ImageUtils.bitmapToFile(file, bitmap);//在本地缓存
    }


}
