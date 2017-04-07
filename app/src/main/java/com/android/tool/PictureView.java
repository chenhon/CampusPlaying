package com.android.tool;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * 显示任何View的九宫格控件
 * 这个控件将如何测量和排列孩子的逻辑给抽取了出来,针对有些时候需要使用九宫格形式来展示的效果
 * 特别说明:此控件的包裹效果和填充父容器的效果是一样的,因为在本测量方法中并没有处理包裹的形式,也不能处理
 * 针对在listview的条目item中的时候,传入的高度的测量模式为:{@link MeasureSpec#UNSPECIFIED},此时高度就就根本孩子的个数来决定了
 * 因为不同的孩子格式,孩子的排列方式不一样
 */
public class PictureView extends ViewGroup {

    public static final int HOME_TYPE = 0;//图片在home页显示模式,自适应显示，但最多显示9张图片
    public static final int DETAIL_TYPE = 1;//图片在详情页显示模式，自适应显示，无张数限制
    public static final int PUBLISH_TYPE = 2;//图片在发表界面显示模式,一行显示3张图片，无张数限制

    private static final int INTERVAL_DISTANCE = 4;//图片之间的间隔距离

    private Context context = null; //上下文
    private int pictureType;  //图片布局类型
    private int column; // 一行的图片数
    private List<RectEntity> rectEntityList = new ArrayList<>();//用于保存每一个孩子的在父容器的位置

    public PictureView(Context context) {
        super(context);
        init(context);
    }

    public PictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
    }

    public PictureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(context, attrs);

    }


    private void init(Context context) {
        this.context = context;
    }

    /**
     * 初始化自定义的属性
     * @param context
     * @param attrs
     */
    private void initParams(Context context, AttributeSet attrs) {
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PictureView);
        if (typedArray != null) {      //获取属性值
            pictureType = typedArray.getInt(R.styleable.PictureView_picture_type, HOME_TYPE);
            typedArray.recycle();
        }
    }


    /**
     * 实体类,
     * 描述一个矩形的 左上角的点坐标
     *           和 右下角的点的坐标
     */
     class RectEntity {

        // 左上角横坐标
        public int leftX;

        // 左上角纵坐标
        public int leftY;

        // 右下角横坐标
        public int rightX;

        // 右下角纵坐标
        public int rightY;

    }

    /**
     * 设置容器和各孩子的大小
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //获取推荐的宽高和计算模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childCount = getChildCount();

      //  if (heightMode == MeasureSpec.UNSPECIFIED) { //出现在listView的item中
        {
            switch (pictureType) {
                case HOME_TYPE: //自适应，最多显示九张
                    if (childCount == 1 || childCount == 3 || childCount == 4 || childCount > 6) {
                        heightSize = widthSize;
                    } else if (childCount == 2) {
                        heightSize = widthSize / 2;
                    } else if (childCount == 5 || childCount == 6) {
                        heightSize = widthSize * 2 / 3;
                    }

                    if (childCount > 4) {
                        column = 3;
                    } else if (childCount > 1) {
                        column = 2;
                    } else if(childCount == 1) {
                        column = 1;
                    }
                    break;
                case DETAIL_TYPE: //前9张自适应显示 ，无张数限制
                    if (childCount == 1 || childCount == 3 || childCount == 4) {
                        heightSize = widthSize;
                    } else if (childCount == 2) {
                        heightSize = widthSize / 2;
                    } else if (childCount == 5 || childCount == 6) {
                        heightSize = widthSize * 2 / 3;
                    } else if (childCount > 6) { //有问题？？？？？？这里之前少除了3
                        heightSize = (childCount + 2) / 3 * widthSize/3;
                    }

                    if (childCount > 4) {
                        column = 3;
                    } else if (childCount > 1) {
                        column = 2;
                    } else if(childCount == 1) {
                        column = 1;
                    }
                    break;
                case PUBLISH_TYPE: //一行9张，无张数限制
                    column = 3;//一行三张图片
                    heightSize = (childCount + 2) / 3 * widthSize;
                    break;
            }

        }

        Log.d(TAG, "heightSize:" + heightSize + "widthSize" + widthSize+"childCount" + childCount);
        setMeasuredDimension(widthSize, heightSize);//该容器的尺寸

        for (int i = 0; i < childCount; i++) {//每个孩子的尺寸都相同
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec((widthSize - 2 * INTERVAL_DISTANCE)/column, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((widthSize - 2 * INTERVAL_DISTANCE)/column, MeasureSpec.EXACTLY));
        }


    }

    /**
     * 安排各孩子的具体位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        computeViewsLocation(); //计算每个孩子的位置信息
        // 循环集合中的各个菜单的位置信息,并让孩子到这个位置上
        for (int i = 0; i < getChildCount(); i++) {
            // 循环中的位置
            RectEntity e = rectEntityList.get(i);
            // 循环中的孩子
            View v = getChildAt(i);
            // 让孩子到指定的位置
            v.layout(e.leftX, e.leftY, e.rightX, e.rightY);
        }
    }



    /**
     * 用于计算孩子们的位置信息
     */
    private void computeViewsLocation() {
        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }
        if (childCount == rectEntityList.size()) {  //孩子数量没有变化
            return;
        }

        //重新设置孩子位置
        rectEntityList.clear();

        for (int i = 0; i < childCount && i < 9; i++) {
            RectEntity r = new RectEntity();
            r.leftX = (i % column) * (INTERVAL_DISTANCE + getChildAt(0).getMeasuredWidth());
            r.leftY = (i / column) * (INTERVAL_DISTANCE + getChildAt(0).getMeasuredHeight());
            r.rightX = r.leftX + getChildAt(0).getMeasuredWidth();
            r.rightY = r.leftY + getChildAt(0).getMeasuredHeight();
            rectEntityList.add(r);
        }
        if (HOME_TYPE != pictureType) {
            for (int i = 9; i < childCount; i++) {
                RectEntity r1 = new RectEntity();
                r1.leftX = (i % column) * (INTERVAL_DISTANCE + getChildAt(0).getMeasuredWidth());
                r1.leftY = (i / column) * (INTERVAL_DISTANCE + getChildAt(0).getMeasuredHeight());
                r1.rightX = r1.leftX + getChildAt(0).getMeasuredWidth();
                r1.rightY = r1.leftY + getChildAt(0).getMeasuredHeight();
                rectEntityList.add(r1);
            }
        }
    }

    /**
     * 填充父容器的布局对象
     */
    private LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

    /**
     * 在布局的最后添加孩子
     * @param v
     */
    public void addChildView(View v) {
        this.addView(v);
        //this.addView(v, getChildCount()-1);//加入孩子
        requestLayout();//给孩子重新布局
    }

    /**
     * 在布局中添加孩子
     * @param child
     * @param index   添加的位置
     */
    public void addChildView(View child, int index) {
        //this.addView(v, getChildCount()-1);//加入孩子
        this.addView(child, index);
        requestLayout();//给孩子重新布局
    }

    public void removeChildView(int index) {
        this.removeViewAt(index);
    }


}
