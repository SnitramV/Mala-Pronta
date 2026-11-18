package br.com.malapronta;

import android.content.Context;
import android.graphics.*;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.util.*;

/** Original images and metadata stay inside Android's private, non-backup directory. */
public final class PhotoStore {
    public static final long MAX_BYTES=25L*1024*1024;
    private final File directory;
    public static final class Photo {
        public final String id,title; public final long created,size; public final File file;
        Photo(String id,String title,long created,File file){this.id=id;this.title=title;this.created=created;this.file=file;this.size=file.length();}
    }
    public PhotoStore(Context context) {
        directory=new File(context.getNoBackupFilesDir(),"documents");
    }
    public synchronized List<Photo> list() {
        List<Photo> photos=new ArrayList<>(); File[] entries=directory.listFiles();if(entries==null)return photos;
        for(File meta:entries) if(meta.getName().matches("[a-f0-9-]{36}\\.json")) {
            try(FileInputStream input=new AtomicFile(meta).openRead()) {
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[1024];int n;
                while((n=input.read(buffer))!=-1){bytes.write(buffer,0,n);if(bytes.size()>8192)throw new IOException("Metadados inválidos");}
                JSONObject o=new JSONObject(new String(bytes.toByteArray(),"UTF-8"));
                String id=meta.getName().substring(0,36);File image=new File(directory,id+".photo");
                if(image.isFile())photos.add(new Photo(id,o.getString("title"),o.getLong("created"),image));
            }catch(IOException|JSONException ignored){}
        }
        Collections.sort(photos,(a,b)->Long.compare(b.created,a.created));return photos;
    }
    public synchronized Photo importImage(Context context,Uri uri,String title) throws IOException {
        if(!directory.isDirectory()&&!directory.mkdirs())throw new IOException("Não foi possível criar o armazenamento privado.");
        String id=UUID.randomUUID().toString();File target=new File(directory,id+".photo");
        AtomicFile metadata=new AtomicFile(new File(directory,id+".json"));
        try {
            try(InputStream input=context.getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(target)) {
                if(input==null)throw new IOException("Não foi possível abrir a imagem.");
                byte[] buffer=new byte[32768];long size=0;int n;
                while((n=input.read(buffer))!=-1){size+=n;if(size>MAX_BYTES)throw new IOException("Escolha uma imagem de até 25 MB.");out.write(buffer,0,n);}
            }
            BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeFile(target.getPath(),bounds);
            if(bounds.outWidth<=0||bounds.outHeight<=0)throw new IOException("Formato não suportado. Escolha uma foto JPEG, PNG ou WebP.");
            long now=System.currentTimeMillis();JSONObject o=new JSONObject();o.put("title",title);o.put("created",now);
            FileOutputStream output=null;
            try {output=metadata.startWrite();output.write(o.toString().getBytes("UTF-8"));metadata.finishWrite(output);}
            catch(IOException e){if(output!=null)metadata.failWrite(output);throw e;}
            return new Photo(id,title,now,target);
        }catch(IOException|JSONException|SecurityException e){target.delete();metadata.delete();throw new IOException(e instanceof SecurityException?"Acesso à imagem indisponível. Selecione a foto novamente.":e.getMessage(),e);}
    }
    public synchronized void delete(Photo photo) throws IOException {
        if(photo.file.exists()&&!photo.file.delete())throw new IOException("Não foi possível excluir a foto. Tente novamente.");
        new AtomicFile(new File(directory,photo.id+".json")).delete();
    }
    public static Bitmap decode(File file,int maxSide) throws IOException {
        BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeFile(file.getPath(),options);
        int sample=1;while(Math.max(options.outWidth,options.outHeight)/sample>maxSide)sample*=2;
        options=new BitmapFactory.Options();options.inSampleSize=sample;
        Bitmap bitmap=BitmapFactory.decodeFile(file.getPath(),options);if(bitmap==null)throw new IOException("Não foi possível exibir esta imagem.");
        Matrix matrix=new Matrix();int orientation=ExifInterface.ORIENTATION_NORMAL;
        try {orientation=new ExifInterface(file.getPath()).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);}catch(IOException ignored){}
        switch(orientation){
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:matrix.setScale(-1,1);break;
            case ExifInterface.ORIENTATION_ROTATE_180:matrix.setRotate(180);break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:matrix.setScale(1,-1);break;
            case ExifInterface.ORIENTATION_TRANSPOSE:matrix.setRotate(90);matrix.postScale(-1,1);break;
            case ExifInterface.ORIENTATION_ROTATE_90:matrix.setRotate(90);break;
            case ExifInterface.ORIENTATION_TRANSVERSE:matrix.setRotate(-90);matrix.postScale(-1,1);break;
            case ExifInterface.ORIENTATION_ROTATE_270:matrix.setRotate(-90);break;
        }
        if(!matrix.isIdentity()){Bitmap rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);if(rotated!=bitmap)bitmap.recycle();bitmap=rotated;}
        return bitmap;
    }
}
