package br.com.malapronta;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/** Runs only in the separate instrumentation APK, never in the distributed app. */
public class PhotoInstrumentation extends Instrumentation {
    void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    boolean hasText(View v,String text){if(v instanceof TextView&&text.equals(((TextView)v).getText().toString()))return true;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)if(hasText(g.getChildAt(i),text))return true;}return false;}
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){
        Bundle results=new Bundle();MainActivity activity=null;
        try {
            Context context=getTargetContext();PhotoStore store=new PhotoStore(context);int baseline=store.list().size();
            Intent launch=new Intent(context,MainActivity.class);launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);activity=(MainActivity)startActivitySync(launch);
            final MainActivity main=activity;
            boolean wasChecked=main.checked.contains("0-0");boolean wasPersonalized=main.trip.personalized;
            runOnMainSync(()->{main.tab=2;main.render();});
            check((main.getWindow().getAttributes().flags&WindowManager.LayoutParams.FLAG_SECURE)!=0,"Documentos devem bloquear capturas");
            File original=new File(context.getCacheDir(),"ticket-test.png");Bitmap source=Bitmap.createBitmap(320,640,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(source);canvas.drawColor(Color.WHITE);Paint paint=new Paint();paint.setColor(Color.BLACK);paint.setTextSize(26);canvas.drawText("INGRESSO TESTE",24,64,paint);canvas.drawRect(40,100,260,320,paint);
            try(OutputStream out=new FileOutputStream(original)){source.compress(Bitmap.CompressFormat.PNG,100,out);}source.recycle();byte[] originalBytes=Files.readAllBytes(original.toPath());
            runOnMainSync(()->{main.pendingPhotoTitle="Ingresso de teste";main.onActivityResult(MainActivity.PICK_DOCUMENT_PHOTO,Activity.RESULT_OK,new Intent().setData(Uri.fromFile(original)));});
            long deadline=System.currentTimeMillis()+20000;while(main.importingPhoto&&System.currentTimeMillis()<deadline)SystemClock.sleep(100);
            waitForIdleSync();check(!main.importingPhoto,"Importação não terminou");check(store.list().size()==baseline+1,"Foto deve ser listada após importação");
            PhotoStore.Photo photo=null;for(PhotoStore.Photo p:store.list())if(p.title.equals("Ingresso de teste")){photo=p;break;}check(photo!=null,"Título da foto deve persistir");
            check(Arrays.equals(originalBytes,Files.readAllBytes(photo.file.toPath())),"Cópia deve preservar bytes originais para ingressos");
            check(photo.file.getCanonicalPath().startsWith(context.getNoBackupFilesDir().getCanonicalPath()+File.separator),"Foto deve ficar em armazenamento privado sem backup");
            check(hasText(main.root,"Ingresso de teste"),"Foto deve aparecer na interface");
            final PhotoStore.Photo savedPhoto=photo;runOnMainSync(()->main.viewPhoto(savedPhoto));waitForIdleSync();SystemClock.sleep(700);runOnMainSync(()->{});
            // Canceling the system picker must not create anything.
            runOnMainSync(()->main.onActivityResult(MainActivity.PICK_DOCUMENT_PHOTO,Activity.RESULT_CANCELED,null));check(store.list().size()==baseline+1,"Cancelar não pode criar foto");
            original.delete();PhotoStore reopened=new PhotoStore(context);check(reopened.list().size()==baseline+1,"Foto deve continuar após excluir fonte e reabrir armazenamento");Bitmap decoded=PhotoStore.decode(photo.file,1000);check(decoded.getWidth()==320&&decoded.getHeight()==640,"Imagem offline deve abrir com dimensões corretas");decoded.recycle();
            File invalid=new File(context.getCacheDir(),"invalid-image.txt");try(FileOutputStream out=new FileOutputStream(invalid)){out.write("not an image".getBytes("UTF-8"));}
            boolean rejected=false;try{store.importImage(context,Uri.fromFile(invalid),"Inválido");}catch(IOException e){rejected=true;}check(rejected,"Arquivos inválidos devem ser rejeitados");invalid.delete();
            File huge=new File(context.getCacheDir(),"oversized.bin");try(RandomAccessFile f=new RandomAccessFile(huge,"rw")){f.setLength(PhotoStore.MAX_BYTES+1);}
            rejected=false;try{store.importImage(context,Uri.fromFile(huge),"Grande");}catch(IOException e){rejected=true;}check(rejected,"Limite de 25 MB deve ser aplicado");huge.delete();check(store.list().size()==baseline+1,"Falhas não podem deixar fotos na lista");
            File jpeg=new File(context.getCacheDir(),"rotated.jpg");Bitmap landscape=Bitmap.createBitmap(120,80,Bitmap.Config.ARGB_8888);try(FileOutputStream out=new FileOutputStream(jpeg)){landscape.compress(Bitmap.CompressFormat.JPEG,90,out);}landscape.recycle();ExifInterface exif=new ExifInterface(jpeg.getPath());exif.setAttribute(ExifInterface.TAG_ORIENTATION,"6");exif.saveAttributes();PhotoStore.Photo rotated=store.importImage(context,Uri.fromFile(jpeg),"Rotação de teste");Bitmap portrait=PhotoStore.decode(rotated.file,256);check(portrait.getWidth()==80&&portrait.getHeight()==120,"Rotação EXIF deve ser respeitada");portrait.recycle();store.delete(rotated);check(jpeg.exists(),"Excluir cópia não pode apagar a origem");jpeg.delete();
            store.delete(photo);check(!photo.file.exists()&&store.list().size()==baseline,"Excluir deve remover arquivo e listagem");
            check(main.checked.contains("0-0")==wasChecked&&main.trip.personalized==wasPersonalized,"Fotos não podem alterar o checklist");
            runOnMainSync(()->{main.tab=0;main.render();});check((main.getWindow().getAttributes().flags&WindowManager.LayoutParams.FLAG_SECURE)==0,"Checklist deve restaurar comportamento normal de captura");
            results.putString("stream","OK: importação pela Activity, interface, bytes originais, offline, armazenamento privado, exclusão, cancelamento, arquivo inválido, limite de tamanho, orientação EXIF e checklist preservado.");finish(Activity.RESULT_OK,results);
        }catch(Throwable e){results.putString("stream",android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,results);}
    }
}
