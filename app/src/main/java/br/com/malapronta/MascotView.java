package br.com.malapronta;
import android.content.Context;
import android.graphics.*;
import android.view.View;
/** Draws provided artwork within its alpha bounds, sampled to avoid loading 4K bitmaps. */
public final class MascotView extends View {
    final Bitmap bitmap;final RectF source;final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    public MascotView(Context context,String resource,String description,int left,int top,int right,int bottom){
        super(context);setContentDescription(description);int id=getResources().getIdentifier(resource,"drawable",context.getPackageName());
        BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeResource(getResources(),id,bounds);int sample=1;while(Math.max(bounds.outWidth,bounds.outHeight)/sample>1024)sample*=2;
        BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=sample;options.inScaled=false;bitmap=BitmapFactory.decodeResource(getResources(),id,options);
        float sx=bitmap.getWidth()/(float)bounds.outWidth,sy=bitmap.getHeight()/(float)bounds.outHeight;source=new RectF(left*sx,top*sy,right*sx,bottom*sy);
    }
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(bitmap==null)return;Matrix matrix=new Matrix();matrix.setRectToRect(source,new RectF(0,0,getWidth(),getHeight()),Matrix.ScaleToFit.CENTER);canvas.drawBitmap(bitmap,matrix,paint);}
}
