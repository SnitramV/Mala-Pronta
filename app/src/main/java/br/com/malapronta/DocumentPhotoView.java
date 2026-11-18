package br.com.malapronta;
import android.content.Context;
import android.graphics.*;
import android.view.*;

/** Bounded pan, pinch and double-tap zoom for offline document consultation. */
public final class DocumentPhotoView extends View {
    private Bitmap bitmap;private final Matrix matrix=new Matrix();private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    private final ScaleGestureDetector scale;private final GestureDetector gestures;private float zoom=1;
    public DocumentPhotoView(Context context){
        super(context);setContentDescription("Foto do documento. Use dois dedos para ampliar e arraste para mover.");
        scale=new ScaleGestureDetector(context,new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override public boolean onScale(ScaleGestureDetector d){float next=Math.max(1,Math.min(8,zoom*d.getScaleFactor()));matrix.postScale(next/zoom,next/zoom,d.getFocusX(),d.getFocusY());zoom=next;constrain();invalidate();return true;}
        });
        gestures=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onDown(android.view.MotionEvent e){return true;}
            @Override public boolean onScroll(android.view.MotionEvent a,android.view.MotionEvent b,float dx,float dy){if(!scale.isInProgress()){matrix.postTranslate(-dx,-dy);constrain();invalidate();}return true;}
            @Override public boolean onDoubleTap(android.view.MotionEvent e){if(zoom>1)fit();else{matrix.postScale(3,3,e.getX(),e.getY());zoom=3;constrain();invalidate();}return true;}
        });
    }
    public void setPhoto(Bitmap image){bitmap=image;fit();}
    public void fit(){if(bitmap==null||getWidth()==0||getHeight()==0)return;matrix.setRectToRect(new RectF(0,0,bitmap.getWidth(),bitmap.getHeight()),new RectF(0,0,getWidth(),getHeight()),Matrix.ScaleToFit.CENTER);zoom=1;invalidate();}
    void constrain(){if(bitmap==null)return;RectF r=new RectF(0,0,bitmap.getWidth(),bitmap.getHeight());matrix.mapRect(r);float dx=r.width()<getWidth()?(getWidth()-r.width())/2-r.left:r.left>0?-r.left:r.right<getWidth()?getWidth()-r.right:0;float dy=r.height()<getHeight()?(getHeight()-r.height())/2-r.top:r.top>0?-r.top:r.bottom<getHeight()?getHeight()-r.bottom:0;matrix.postTranslate(dx,dy);}
    @Override protected void onSizeChanged(int w,int h,int ow,int oh){fit();}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(bitmap!=null)canvas.drawBitmap(bitmap,matrix,paint);}
    @Override public boolean onTouchEvent(MotionEvent event){scale.onTouchEvent(event);gestures.onTouchEvent(event);return true;}
}
