package dajver.draggblelist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

public class DragListView extends ListView {

    private ImageView dragImageView;
    private int dragSrcPosition;
    private int dragPosition;

    private int dragPoint;
    private int dragOffset;

    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;

    private int scaledTouchSlop;
    private int upScrollBounce;
    private int downScrollBounce;

    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // захыватываем наш палец который коснулся экрана, и следим за ним
        if(ev.getAction()==MotionEvent.ACTION_DOWN){
            //запоминаем координаты пальца
            int x = (int)ev.getX();
            int y = (int)ev.getY();

            // переводим их в точки на экране, и проверяем не вылезли ли мы за пределы
            dragSrcPosition = dragPosition = pointToPosition(x, y);
            if(dragPosition== AdapterView.INVALID_POSITION){
                return super.onInterceptTouchEvent(ev);
            }

            //берем ту позицию на которую мы надавили и берем этот айтем и отслеживаем перемещения пальца или чем вы там тыкаете в экран
            ViewGroup itemView = (ViewGroup) getChildAt(dragPosition-getFirstVisiblePosition());
            dragPoint = y - itemView.getTop();
            dragOffset = (int) (ev.getRawY() - y);

            //магия
            View dragger = itemView.findViewById(R.id.radioButton);
            if(dragger!=null&&x>dragger.getLeft()-20){
                upScrollBounce = Math.min(y-scaledTouchSlop, getHeight()/3);
                downScrollBounce = Math.max(y+scaledTouchSlop, getHeight()*2/3);

                itemView.setDrawingCacheEnabled(true);
                Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache());
                startDrag(bm, y);
            }
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     *
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(dragImageView!=null&&dragPosition!=INVALID_POSITION){
            int action = ev.getAction();
            switch(action){
                case MotionEvent.ACTION_UP:
                    int upY = (int)ev.getY();
                    stopDrag();
                    onDrop(upY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int moveY = (int)ev.getY();
                    onDrag(moveY);
                    break;
                default:break;
            }
            return true;
        }
        //
        return super.onTouchEvent(ev);
    }

    /**
     * Метод обрабатывающий перетаскивание айтемов
     * @param bm
     * @param y
     */
    public void startDrag(Bitmap bm ,int y){
        stopDrag();

        //из кода меняем позици елемента который мы схватили на координаты которые мы получили в onInterceptTouchEvent()
        windowParams = new WindowManager.LayoutParams();
        windowParams.gravity = Gravity.TOP;
        windowParams.x = 0;
        windowParams.y = y - dragPoint + dragOffset;
        windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;

        ImageView imageView = new ImageView(getContext());
        imageView.setImageBitmap(bm);
        windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(imageView, windowParams);
        dragImageView = imageView;
    }

    /**
     * удаляем лишний вью и вставляем его ниже
     */
    public void stopDrag(){
        if(dragImageView!=null){
            windowManager.removeView(dragImageView);
            dragImageView = null;
        }
    }

    /**
     * перемещаем по экрану айтемы
     * снова куча магии
     * @param y
     */
    public void onDrag(int y){
        if(dragImageView!=null){
            windowParams.alpha = 0.8f;
            windowParams.y = y - dragPoint + dragOffset;
            windowManager.updateViewLayout(dragImageView, windowParams);
        }
        int tempPosition = pointToPosition(0, y);
        if(tempPosition!=INVALID_POSITION){
            dragPosition = tempPosition;
        }

        int scrollHeight = 0;
        if(y<upScrollBounce){
            scrollHeight = 8;
        }else if(y>downScrollBounce){
            scrollHeight = -8;
        }

        if(scrollHeight!=0){
            setSelectionFromTop(dragPosition, getChildAt(dragPosition-getFirstVisiblePosition()).getTop()+scrollHeight);
        }
    }

    /**
     * что же делать чкогда бросили? верно сохраняем позицию если она валидна
     * куча магии
     * @param y
     */
    public void onDrop(int y){
        //здесь мы замещаем айтем который на данный момент находится
        // на своей позиции вниз что бы вставить тот что тащили и удаляем тот что был вместо него
        // и ставим его ниже
        int tempPosition = pointToPosition(0, y);
        if(tempPosition!=INVALID_POSITION){
            dragPosition = tempPosition;
        }
        if(y<getChildAt(1).getTop()){
            dragPosition = 1;
        }else if(y>getChildAt(getChildCount()-1).getBottom()){
            dragPosition = getAdapter().getCount()-1;
        }

        //
        if(dragPosition>0&&dragPosition<getAdapter().getCount()){
            DragListAdapter adapter = (DragListAdapter)getAdapter();
            String dragItem = adapter.getItem(dragSrcPosition);
            adapter.remove(dragItem);
            adapter.insert(dragItem, dragPosition);
        }

    }
}