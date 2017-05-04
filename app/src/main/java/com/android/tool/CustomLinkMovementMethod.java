package com.android.tool;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/4/29 0029.
 */
public class CustomLinkMovementMethod extends LinkMovementMethod {
/*    publicclassCustomLinkMovementMethodextendsLinkMovementMethod {

        staticCustomLinkMovementMethod sInstance;

        @Override

                publicbooleanonTouchEvent(TextView widget, Spannable buffer,

                MotionEvent event) {

            intaction = event.getAction();

            if(action == MotionEvent.ACTION_UP ||

                    action == MotionEvent.ACTION_DOWN) {

                intx = (int) event.getX();

                inty = (int) event.getY();

                x -= widget.getTotalPaddingLeft();

                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();

                y += widget.getScrollY();

                Layout layout = widget.getLayout();

                intline = layout.getLineForVertical(y);

                intoff = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                if(link.length !=0) {

                    if(action == MotionEvent.ACTION_UP) {

                        link[0].onClick(widget);

                    }elseif(action == MotionEvent.ACTION_DOWN) {

                        Selection.setSelection(buffer,

                                buffer.getSpanStart(link[0]),

                                buffer.getSpanEnd(link[0]));

                    }

                    if(widgetinstanceofCommentTextView){

                        ((CommentTextView)widget).linkHit =true;

                    }

                    returntrue;

                }else{

                    Selection.removeSelection(buffer);

                    super.onTouchEvent(widget, buffer, event);

                    returnfalse;

                }

            }

            returnTouch.onTouchEvent(widget, buffer, event);

        }

        publicstaticCustomLinkMovementMethod getInstance() {

            if(sInstance ==null){

                sInstance =newCustomLinkMovementMethod();

            }

            returnsInstance;

        }*/
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
            MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget);
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                }
                if(widget instanceof CustomTextView){

                    ((CustomTextView)widget).linkHit =true;

                }


                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    public static CustomLinkMovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new CustomLinkMovementMethod();

        return sInstance;
    }


    private static CustomLinkMovementMethod sInstance;

}