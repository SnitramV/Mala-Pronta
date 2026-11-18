package br.com.malapronta;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
    final int INK=Color.rgb(28,49,41), GREEN=Color.rgb(32,91,69), MUTED=Color.rgb(106,117,109), BG=Color.rgb(246,245,240), LIME=Color.rgb(221,247,159);
    SharedPreferences prefs; Checklist.Trip trip=new Checklist.Trip();
    Set<String> checked=new HashSet<>(), collapsed=new HashSet<>();
    List<Checklist.Item> custom=new ArrayList<>();
    LinearLayout root, content, categoryList; ScrollView scroll; TextView progressCount, progressSub; ProgressBar progress;
    Map<Integer,TextView> counters=new HashMap<>(); int tab=0, filter=0;
    PhotoStore photoStore;
    final java.util.concurrent.ExecutorService photoWorker=java.util.concurrent.Executors.newSingleThreadExecutor();
    String pendingPhotoTitle=""; boolean importingPhoto=false;
    static final int PICK_DOCUMENT_PHOTO=410;
    String tripName="Minha viagem universitária";

    @Override public void onCreate(Bundle b) { super.onCreate(b); prefs=getSharedPreferences("mala",MODE_PRIVATE); load(); photoStore=new PhotoStore(this); if(b!=null){tab=b.getInt("tab",0);pendingPhotoTitle=b.getString("pendingPhotoTitle","");} render(); }
    int dp(float x) { return (int)(x*getResources().getDisplayMetrics().density+.5f); }
    GradientDrawable shape(int color,int radius) { GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    TextView text(String s,int size,int color,boolean bold) { TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); if(bold) v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); v.setLineSpacing(dp(2),1); return v; }
    LinearLayout column() { LinearLayout l=new LinearLayout(this); l.setOrientation(1); return l; }
    LinearLayout row() { LinearLayout l=new LinearLayout(this); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    void pad(View v,int a) { v.setPadding(dp(a),dp(a),dp(a),dp(a)); }
    void gap(LinearLayout l,int h) { View v=new View(this); l.addView(v,new LinearLayout.LayoutParams(1,dp(h))); }
    void add(LinearLayout l,View v,int bottom) { LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(bottom); l.addView(v,p); }
    TextView button(String s,boolean primary,Runnable action) {
        TextView v=text(s,14,primary?Color.WHITE:GREEN,true); v.setGravity(Gravity.CENTER); v.setMinHeight(dp(50)); v.setPadding(dp(12),dp(12),dp(12),dp(12)); v.setBackground(shape(primary?GREEN:Color.rgb(231,237,225),16)); v.setOnClickListener(w->action.run()); v.setFocusable(true); return v;
    }
    List<Checklist.Item> items() { List<Checklist.Item> all=Checklist.items(trip); all.addAll(custom); return all; }
    void load() {
        checked=new HashSet<>(prefs.getStringSet("checked",new HashSet<>())); tripName=prefs.getString("name",tripName);
        trip.days=prefs.getInt("days",3); trip.stay=prefs.getInt("stay",0); trip.transport=prefs.getInt("transport",0); trip.activity=prefs.getInt("activity",0); trip.personalized=prefs.getBoolean("personalized",false);
        try { JSONArray arr=new JSONArray(prefs.getString("custom","[]")); for(int i=0;i<arr.length();i++) { JSONObject o=arr.getJSONObject(i); custom.add(new Checklist.Item(o.getString("id"),o.getString("label"),7)); } } catch(JSONException e) { Toast.makeText(this,"Não foi possível ler os itens extras.",Toast.LENGTH_LONG).show(); }
    }
    void save() {
        JSONArray arr=new JSONArray(); for(Checklist.Item i:custom) { JSONObject o=new JSONObject(); try { o.put("id",i.id);o.put("label",i.label);arr.put(o); } catch(JSONException ignored) {} }
        prefs.edit().putStringSet("checked",new HashSet<>(checked)).putString("name",tripName).putInt("days",trip.days).putInt("stay",trip.stay).putInt("transport",trip.transport).putInt("activity",trip.activity).putBoolean("personalized",trip.personalized).putString("custom",arr.toString()).apply();
    }
    void render() {
        if(tab==2)getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        counters.clear(); root=column(); root.setBackgroundColor(BG);
        // Insets protect controls from system bars, including Android 15 edge-to-edge.
        root.setOnApplyWindowInsetsListener((v,insets)-> { v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom()); return insets.consumeSystemWindowInsets(); });
        setContentView(root); root.requestApplyInsets();
        LinearLayout top=row(); top.setPadding(dp(22),dp(14),dp(22),dp(10));
        LinearLayout brandBlock=column();TextView brand=text("MALA PRONTA",15,INK,true);brand.setLetterSpacing(.10f);brandBlock.addView(brand);brandBlock.addView(text("Sua mala • offline",11,MUTED,false));top.addView(brandBlock,new LinearLayout.LayoutParams(0,-2,1));
        ImageView lieu=new ImageView(this){
            @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);android.graphics.Matrix m=new android.graphics.Matrix();m.setRectToRect(new android.graphics.RectF(13,50,141,94),new android.graphics.RectF(0,0,w-getPaddingLeft()-getPaddingRight(),h-getPaddingTop()-getPaddingBottom()),android.graphics.Matrix.ScaleToFit.CENTER);setImageMatrix(m);}
        };
        lieu.setImageResource(getResources().getIdentifier("lieu_transparent","drawable",getPackageName()));lieu.setScaleType(ImageView.ScaleType.MATRIX);lieu.setContentDescription("Logo Lieu");pad(lieu,8);lieu.setBackground(shape(INK,12));top.addView(lieu,new LinearLayout.LayoutParams(dp(88),dp(40)));root.addView(top);
        scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        content=column(); content.setPadding(dp(20),dp(10),dp(20),dp(24)); scroll.addView(content);
        if(tab==0) checklistPage(); else if(tab==1) tripPage(); else if(tab==2) documentsPage(); else tipsPage();
        LinearLayout nav=row(); nav.setBackgroundColor(Color.WHITE); nav.setPadding(dp(10),dp(8),dp(10),dp(8));
        String[] names={"✓\nChecklist","⌁\nViagem","▣\nDocumentos","✦\nDicas"};
        for(int n=0;n<4;n++) { final int dest=n; TextView b=text(names[n],12,tab==n?GREEN:MUTED,tab==n); b.setGravity(Gravity.CENTER); b.setMinHeight(dp(48)); if(tab==n)b.setBackground(shape(0xffedf2e8,14)); b.setOnClickListener(v->{tab=dest;render();}); b.setContentDescription(names[n]+(tab==n?", selecionado":"")); nav.addView(b,new LinearLayout.LayoutParams(0,-2,1)); }
        root.addView(nav);
    }
    void title(String eyebrow,String heading,String sub) {
        TextView e=text(eyebrow,11,GREEN,true); e.setLetterSpacing(.12f); add(content,e,8); add(content,text(heading,30,INK,true),8); add(content,text(sub,14,MUTED,false),22);
    }
    ImageView logo(String resource,String description){ImageView image=new ImageView(this);image.setImageResource(getResources().getIdentifier(resource,"drawable",getPackageName()));image.setContentDescription(description);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setAdjustViewBounds(true);return image;}
    View mascot(String resource,String description,int left,int top,int right,int bottom){return new MascotView(this,resource,description,left,top,right,bottom);}
    void sportsMascots(){
        add(content,text("No clima dos jogos",19,INK,true),6);add(content,text("Os mascotes das modalidades também estão na viagem.",13,MUTED,false),14);
        HorizontalScrollView gallery=new HorizontalScrollView(this);gallery.setHorizontalScrollBarEnabled(false);LinearLayout strip=row();
        String[] resources={"sport_atletismo","sport_basquete","sport_futsal_futebol","sport_handebol","sport_lutas","sport_tenisdecampo","sport_tenisdemesa","sport_volei"};
        String[] names={"Atletismo","Basquete","Futebol / futsal","Handebol","Lutas","Tênis de campo","Tênis de mesa","Vôlei"};
        int[][] bounds={{34,103,413,395},{65,51,376,452},{91,63,356,464},{104,48,433,449},{74,36,392,437},{71,49,377,450},{69,48,379,449},{45,53,366,454}};
        for(int i=0;i<resources.length;i++){LinearLayout card=column();pad(card,12);card.setBackground(shape(Color.WHITE,18));int[] b=bounds[i];card.addView(mascot(resources[i],"Mascote de "+names[i],b[0],b[1],b[2],b[3]),new LinearLayout.LayoutParams(-1,dp(90)));TextView label=text(names[i],11,INK,true);label.setGravity(Gravity.CENTER);label.setMinHeight(dp(36));card.addView(label);LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(dp(112),-2);params.rightMargin=dp(10);strip.addView(card,params);}
        gallery.addView(strip);add(content,gallery,20);
    }
    void checklistPage() {
        ImageView inter=logo("inter_2026","Logo Inter Prudente 2026");LinearLayout.LayoutParams banner=new LinearLayout.LayoutParams(-1,dp(84));banner.bottomMargin=dp(12);content.addView(inter,banner);
        LinearLayout welcome=row();welcome.addView(text("Bora fazer a mala?",24,INK,true),new LinearLayout.LayoutParams(0,-2,1));welcome.addView(mascot("mascot_travel","Chaminha com mochila pronta para viajar",187,33,894,1034),new LinearLayout.LayoutParams(dp(56),dp(72)));add(content,welcome,16);
        LinearLayout hero=column(); pad(hero,20); hero.setBackground(shape(INK,24));
        TextView name=text(tripName+"  ↗",14,LIME,true); name.setOnClickListener(v->{tab=1;render();}); add(hero,name,14);
        progressCount=text("",32,Color.WHITE,true); add(hero,progressCount,4);
        progressSub=text("",13,0xffd5e1d5,false); add(hero,progressSub,16);
        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); progress.setMax(100); progress.setProgressTintList(ColorStateList.valueOf(LIME)); progress.setProgressBackgroundTintList(ColorStateList.valueOf(0xff486154)); hero.addView(progress,new LinearLayout.LayoutParams(-1,dp(7))); add(content,hero,16);
        if(trip.personalized) add(content,text(trip.days+" dias • "+STAYS[trip.stay]+" • lista personalizada",12,GREEN,true),16);
        LinearLayout filters=row(); filters.setBackground(shape(0xffe9e9e1,14)); pad(filters,4); String[] f={"Todos","Pendentes","Na mala"};
        for(int n=0;n<3;n++) { final int val=n; TextView b=text(f[n],12,filter==n?INK:MUTED,filter==n); b.setGravity(Gravity.CENTER); b.setMinHeight(dp(42)); if(filter==n)b.setBackground(shape(Color.WHITE,10)); b.setOnClickListener(v->{filter=val;render();}); filters.addView(b,new LinearLayout.LayoutParams(0,-2,1)); } add(content,filters,20);
        categoryList=column(); add(content,categoryList,8); renderCategories(); updateProgress();
        add(content,button("+  Adicionar item à mala",false,()->addCustom()),12);
        add(content,text("Salvo automaticamente neste celular.",12,MUTED,false),20);
        LinearLayout actions=row(); TextView share=button("Compartilhar lista",false,()->share()); TextView reset=button("Limpar marcações",false,()->reset());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.rightMargin=dp(6);actions.addView(share,p);actions.addView(reset,new LinearLayout.LayoutParams(0,-2,1));add(content,actions,8);
    }
    void updateProgress() {
        List<Checklist.Item> all=items(); int done=0; for(Checklist.Item i:all)if(checked.contains(i.id))done++;
        progressCount.setText(done+" / "+all.size()+" itens"); int percent=all.isEmpty()?0:done*100/all.size(); progress.setProgress(percent);
        progressSub.setText(done==all.size()?"Mala pronta. Agora é só aproveitar!":"Seu checklist • "+percent+"% pronto");
        progress.setContentDescription(percent+"% da mala pronta");
        for(Map.Entry<Integer,TextView> entry:counters.entrySet()) {int total=0,marked=0;for(Checklist.Item i:all)if(i.category==entry.getKey()){total++;if(checked.contains(i.id))marked++;}entry.getValue().setText(marked+"/"+total);}
    }
    boolean visible(Checklist.Item i) { return filter==0 || (filter==1&&!checked.contains(i.id)) || (filter==2&&checked.contains(i.id)); }
    void renderCategories() {
        categoryList.removeAllViews();counters.clear(); int shown=0;
        for(int c=0;c<8;c++) {
            final int cat=c; List<Checklist.Item> group=new ArrayList<>();for(Checklist.Item i:items())if(i.category==c&&visible(i))group.add(i);if(group.isEmpty())continue;shown+=group.size();
            LinearLayout card=column(); card.setBackground(shape(Color.WHITE,18));
            LinearLayout head=row();head.setPadding(dp(16),dp(16),dp(14),dp(16));
            TextView icon=text(Checklist.ICONS[c],23,GREEN,true);icon.setGravity(Gravity.CENTER);icon.setBackground(shape(0xffeef2e6,12));head.addView(icon,new LinearLayout.LayoutParams(dp(38),dp(38)));
            TextView name=text(Checklist.CATEGORIES[c],15,INK,true); name.setPadding(dp(12),0,dp(6),0);head.addView(name,new LinearLayout.LayoutParams(0,-2,1));
            TextView count=text("",12,MUTED,true);counters.put(c,count);head.addView(count);
            TextView chevron=text(collapsed.contains(""+c)?"  +":"  −",18,GREEN,true);head.addView(chevron);head.setContentDescription(Checklist.CATEGORIES[c]+", "+(collapsed.contains(""+c)?"expandir":"recolher"));
            head.setOnClickListener(v->{if(!collapsed.add(""+cat))collapsed.remove(""+cat);renderCategories();updateProgress();});card.addView(head);
            if(!collapsed.contains(""+c)) for(Checklist.Item i:group) {
                View line=new View(this);line.setBackgroundColor(0xfff1f2ec);card.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
                CheckBox box=new CheckBox(this);box.setText(i.label);box.setTextSize(14);box.setTextColor(checked.contains(i.id)?MUTED:INK);box.setButtonTintList(ColorStateList.valueOf(GREEN));box.setPadding(dp(14),dp(10),dp(16),dp(10));box.setMinHeight(dp(56));box.setChecked(checked.contains(i.id));
                if(box.isChecked())box.setPaintFlags(box.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
                box.setOnCheckedChangeListener((b,on)->{if(on)checked.add(i.id);else checked.remove(i.id);save();box.setTextColor(on?MUTED:INK);box.setPaintFlags(on?box.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG:box.getPaintFlags()&~Paint.STRIKE_THRU_TEXT_FLAG);updateProgress();if(filter!=0){renderCategories();updateProgress();}});
                if(i.id.startsWith("custom-"))box.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("Excluir item extra?").setMessage(i.label).setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(d,w)->{custom.remove(i);checked.remove(i.id);save();render();}).show();return true;});
                card.addView(box,new LinearLayout.LayoutParams(-1,-2));
            }
            add(categoryList,card,12);
        }
        if(shown==0) { TextView empty=text(filter==1?"Tudo na mala! ✓":"Nenhum item marcado ainda.",18,GREEN,true);pad(empty,24);add(categoryList,empty,18); }
    }
    void addCustom() {
        EditText input=new EditText(this);input.setHint("Ex.: bandeira da atlética");input.setSingleLine();input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(120)});
        LinearLayout wrap=column();pad(wrap,20);wrap.addView(input);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("O que mais vai na mala?").setMessage("O item aparecerá em Extras que salvam. Toque e segure para excluí-lo.").setView(wrap).setNegativeButton("Cancelar",null).setPositiveButton("Adicionar",null).create();
        dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{String label=input.getText().toString().trim();if(label.isEmpty()){input.setError("Digite o nome do item");return;}custom.add(new Checklist.Item("custom-"+UUID.randomUUID(),label,7));collapsed.remove("7");filter=0;save();dialog.dismiss();render();Toast.makeText(this,"Item adicionado em Extras que salvam",Toast.LENGTH_SHORT).show();}));dialog.show();
    }
    void reset() { new AlertDialog.Builder(this).setTitle("Começar a mala de novo?").setMessage("Todas as marcações serão desfeitas. Sua viagem e seus itens extras serão mantidos.").setNegativeButton("Cancelar",null).setPositiveButton("Limpar marcações",(d,w)->{checked.clear();save();render();}).show(); }
    void share() { StringBuilder s=new StringBuilder("🎒 "+tripName+"\n\n");for(int c=0;c<8;c++){s.append(Checklist.CATEGORIES[c]).append("\n");for(Checklist.Item i:items())if(i.category==c)s.append(checked.contains(i.id)?"[x] ":"[ ] ").append(i.label).append("\n");s.append("\n");}Intent intent=new Intent(Intent.ACTION_SEND);intent.setType("text/plain");intent.putExtra(Intent.EXTRA_TEXT,s.toString());startActivity(Intent.createChooser(intent,"Compartilhar checklist")); }
    static final String[] STAYS={"Alojamento universitário","Hotel","Casa alugada","Camping"};
    static final String[] TRANSPORT={"Ônibus / excursão","Carro","Avião"};
    static final String[] ACTIVITIES={"Festas + jogos + passeios","Principalmente festas","Festas + praia","Jogos + competições"};
    Spinner picker(String label,String[] values,int selected) {add(content,text(label,15,INK,true),8); Spinner sp=new Spinner(this);ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,values);sp.setAdapter(a);sp.setSelection(selected);sp.setBackground(shape(Color.WHITE,12));sp.setPadding(dp(12),dp(4),dp(12),dp(4));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(56));p.bottomMargin=dp(22);content.addView(sp,p);return sp;}
    void tripPage() {
        title("DO SEU JEITO", "Qual é o próximo rolê?", "Adapte sua mala para o Inter, TUSCA, jogos ou uma excursão da atlética.");
        add(content,text("Nome da viagem",15,INK,true),8);EditText name=new EditText(this);name.setText(tripName);name.setSingleLine();name.setTextSize(16);name.setBackground(shape(Color.WHITE,12));pad(name,16);name.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(60)});add(content,name,22);
        Spinner duration=picker("Duração",new String[]{"2 a 3 dias","4 a 5 dias","6 a 7 dias","Mais de uma semana"},trip.days<=3?0:trip.days<=5?1:trip.days<=7?2:3);
        add(content,text("Quantidade exata de dias",15,INK,true),8);EditText days=new EditText(this);days.setInputType(2);days.setSingleLine();days.setText(""+trip.days);days.setBackground(shape(Color.WHITE,12));pad(days,16);add(content,days,22);
        duration.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){boolean first=true;public void onNothingSelected(android.widget.AdapterView<?> a){} public void onItemSelected(android.widget.AdapterView<?> a,View v,int p,long id){if(first){first=false;return;}days.setText(""+new int[]{3,5,7,10}[p]);}});
        Spinner stay=picker("Hospedagem",STAYS,trip.stay),transport=picker("Transporte",TRANSPORT,trip.transport),activity=picker("O que pretende fazer?",ACTIVITIES,trip.activity);
        LinearLayout info=column();pad(info,16);info.setBackground(shape(0xffebefdf,16));add(info,text("Uma mala com a sua cara",15,GREEN,true),6);add(info,text("Sugerimos camisetas por dia e 2 extras de roupas íntimas e meias. Hotel oculta 5 itens de cama: confirme o que a hospedagem oferece. Camping, praia, competição e transporte podem acrescentar itens.",13,INK,false),0);add(content,info,20);
        add(content,button("Personalizar meu checklist  →",true,()->{
            int n;try{n=Integer.parseInt(days.getText().toString());}catch(NumberFormatException e){n=0;}if(n<1||n>60){days.setError("Informe de 1 a 60 dias");days.requestFocus();return;}
            String value=name.getText().toString().trim();if(value.isEmpty()){name.setError("Dê um nome à viagem");return;}
            tripName=value;trip.days=n;trip.stay=stay.getSelectedItemPosition();trip.transport=transport.getSelectedItemPosition();trip.activity=activity.getSelectedItemPosition();trip.personalized=true;save();((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(name.getWindowToken(),0);tab=0;filter=0;render();Toast.makeText(this,"Checklist adaptado! Suas marcações foram mantidas.",Toast.LENGTH_LONG).show();
        }),12);
        if(trip.personalized)add(content,button("Usar lista original de 78 itens",false,()->{new AlertDialog.Builder(this).setTitle("Restaurar a lista original?").setMessage("A personalização será desativada. As marcações e os itens extras serão preservados.").setNegativeButton("Cancelar",null).setPositiveButton("Restaurar",(d,w)->{trip.personalized=false;save();tab=0;filter=0;render();}).show();}),8);
        gap(content,18);sportsMascots();
    }

    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putInt("tab",tab);out.putString("pendingPhotoTitle",pendingPhotoTitle);}
    @Override protected void onDestroy(){photoWorker.shutdown();super.onDestroy();}
    void documentsPage(){
        title("TUDO À MÃO", "Seus documentos", "Fotos de RG, CNH e ingressos para consultar mesmo sem internet.");
        add(content,button(importingPhoto?"Salvando imagem…":"+  Guardar uma foto",true,()->{if(!importingPhoto)namePhoto();}),16);
        LinearLayout note=column();pad(note,16);note.setBackground(shape(0xffebefdf,16));
        add(note,text("Guardadas só neste celular",14,GREEN,true),6);
        add(note,text("As imagens ficam no armazenamento privado do app. Elas permanecem ao limpar o cache. Desinstalar o app ou limpar seus dados apaga as fotos. Mantenha seu documento físico na mala.",13,INK,false),0);add(content,note,20);
        List<PhotoStore.Photo> photos=photoStore.list();add(content,text(photos.size()+ (photos.size()==1?" foto guardada":" fotos guardadas"),13,MUTED,true),12);
        if(photos.isEmpty()){
            LinearLayout empty=row();pad(empty,20);empty.setBackground(shape(Color.WHITE,18));LinearLayout words=column();add(words,text("Sua pasta de viagem",18,GREEN,true),10);add(words,text("Guarde frente e verso do documento ou uma imagem de cada ingresso. Dê um nome para encontrar tudo com facilidade.",14,MUTED,false),0);empty.addView(words,new LinearLayout.LayoutParams(0,-2,1));View chaminha=mascot("mascot_phone","Chaminha com celular",709,494,3147,3486);LinearLayout.LayoutParams character=new LinearLayout.LayoutParams(dp(84),dp(116));character.leftMargin=dp(12);empty.addView(chaminha,character);add(content,empty,12);
        }
        for(PhotoStore.Photo photo:photos){
            LinearLayout card=column();pad(card,16);card.setBackground(shape(Color.WHITE,18));LinearLayout line=row();
            ImageView thumb=new ImageView(this);thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);thumb.setBackground(shape(0xffedf2e8,12));thumb.setContentDescription("Miniatura: "+photo.title);line.addView(thumb,new LinearLayout.LayoutParams(dp(72),dp(88)));
            LinearLayout details=column();details.setPadding(dp(14),0,0,0);add(details,text(photo.title,16,INK,true),6);add(details,text(android.text.format.Formatter.formatShortFileSize(this,photo.size)+" • disponível offline",12,MUTED,false),0);line.addView(details,new LinearLayout.LayoutParams(0,-2,1));add(card,line,14);
            line.setOnClickListener(v->viewPhoto(photo));
            LinearLayout actions=row();TextView open=button("Abrir foto",false,()->viewPhoto(photo));LinearLayout.LayoutParams w=new LinearLayout.LayoutParams(0,-2,1);w.rightMargin=dp(8);actions.addView(open,w);actions.addView(button("Excluir",false,()->deletePhoto(photo)),new LinearLayout.LayoutParams(0,-2,1));add(card,actions,0);add(content,card,12);
            photoWorker.execute(()->{try{android.graphics.Bitmap image=PhotoStore.decode(photo.file,256);runOnUiThread(()->{if(!isDestroyed())thumb.setImageBitmap(image);});}catch(Exception|OutOfMemoryError ignored){}});
        }
    }
    void namePhoto(){
        EditText input=new EditText(this);input.setSingleLine();input.setHint("Ex.: RG — frente ou Ingresso Inter");input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(80)});
        LinearLayout wrap=column();pad(wrap,20);wrap.addView(input);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Nome da foto").setMessage("Escolha depois uma imagem da galeria ou dos arquivos. Até 25 MB por foto.").setView(wrap).setNegativeButton("Cancelar",null).setPositiveButton("Escolher imagem",null).create();
        dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{String title=input.getText().toString().trim();if(title.isEmpty()){input.setError("Dê um nome à foto");return;}pendingPhotoTitle=title;
            Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT);pick.setType("image/*");pick.addCategory(Intent.CATEGORY_OPENABLE);pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try{startActivityForResult(pick,PICK_DOCUMENT_PHOTO);dialog.dismiss();}catch(ActivityNotFoundException e){Toast.makeText(this,"Nenhum seletor de imagens disponível neste aparelho.",Toast.LENGTH_LONG).show();}
        }));dialog.show();
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);if(request!=PICK_DOCUMENT_PHOTO)return;
        if(result!=RESULT_OK||data==null||data.getData()==null){pendingPhotoTitle="";return;}
        final android.net.Uri uri=data.getData();final String title=pendingPhotoTitle.isEmpty()?"Documento":pendingPhotoTitle;pendingPhotoTitle="";importingPhoto=true;tab=2;render();
        final Context app=getApplicationContext();photoWorker.execute(()->{
            String error=null;try{photoStore.importImage(app,uri,title);}catch(Exception e){error=e.getMessage();}
            final String problem=error;runOnUiThread(()->{if(isDestroyed())return;importingPhoto=false;if(tab==2)render();Toast.makeText(this,problem==null?"Foto guardada para consultar offline.":"Não foi possível guardar a foto. "+problem,Toast.LENGTH_LONG).show();});
        });
    }
    void deletePhoto(PhotoStore.Photo photo){
        new AlertDialog.Builder(this).setTitle("Excluir esta foto?").setMessage(photo.title+" será removida do app. A imagem original da galeria será mantida.").setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(d,w)->{
            try{photoStore.delete(photo);render();Toast.makeText(this,"Foto excluída do app.",Toast.LENGTH_SHORT).show();}catch(java.io.IOException e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}
        }).show();
    }
    void viewPhoto(PhotoStore.Photo photo){
        Dialog viewer=new Dialog(this,android.R.style.Theme_Material_Light_NoActionBar);LinearLayout page=column();page.setBackgroundColor(INK);
        page.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets.consumeSystemWindowInsets();});
        LinearLayout heading=row();pad(heading,16);TextView back=button("← Voltar",false,()->viewer.dismiss());heading.addView(back);TextView label=text(photo.title,17,Color.WHITE,true);label.setPadding(dp(12),0,0,0);heading.addView(label,new LinearLayout.LayoutParams(0,-2,1));page.addView(heading);
        TextView hint=text("Carregando foto…",13,LIME,false);hint.setGravity(Gravity.CENTER);pad(hint,10);page.addView(hint);
        DocumentPhotoView image=new DocumentPhotoView(this);page.addView(image,new LinearLayout.LayoutParams(-1,0,1));
        TextView fit=button("Ajustar à tela",false,()->image.fit());LinearLayout footer=column();pad(footer,16);footer.addView(fit);page.addView(footer);
        viewer.setContentView(page);viewer.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);viewer.show();viewer.getWindow().setLayout(-1,-1);page.requestApplyInsets();
        photoWorker.execute(()->{try{android.graphics.Bitmap bitmap=PhotoStore.decode(photo.file,3000);runOnUiThread(()->{if(!isDestroyed()&&viewer.isShowing()){image.setPhoto(bitmap);hint.setText("Use a pinça para ampliar e arraste para mover.");}else bitmap.recycle();});}catch(Exception|OutOfMemoryError e){runOnUiThread(()->{if(viewer.isShowing())hint.setText("Não foi possível exibir esta foto. Feche e tente novamente.");});}});
    }

    void tipsPage() {
        title("PRA CURTIR SEM PERRENGUE", "O rolê passa.\nAs histórias ficam.", "Alguns lembretes para o Inter e qualquer outra viagem universitária.");
        LinearLayout cheer=row();pad(cheer,16);cheer.setBackground(shape(Color.WHITE,18));cheer.addView(mascot("mascot_thumb","Chaminha fazendo sinal de positivo",791,598,3141,3617),new LinearLayout.LayoutParams(dp(76),dp(104)));TextView greeting=text("Cuide de você e curta o rolê!",20,GREEN,true);greeting.setPadding(dp(16),0,0,0);cheer.addView(greeting,new LinearLayout.LayoutParams(0,-2,1));add(content,cheer,16);
        String[][] tips={{"01","Dois tênis, menos perrengue","Leve um que possa sujar ou molhar e outro confortável para o dia a dia."},{"02","Uma troca sempre por perto","Separe uma troca de roupa na mochila para o trajeto. Leve roupas íntimas extras."},{"03","Seu nome em tudo","Identifique carregadores, toalhas e malas. Uma etiqueta pode salvar o dia."},{"04","O essencial vai com você","Pochete ou shoulder bag facilita guardar celular, documento e dinheiro nas festas."},{"05","Cada um com seu kit","Leve seus próprios itens de higiene e deixe o que mais usa em uma nécessaire fácil de alcançar."},{"06","Água também faz parte do rolê","Cuide da hidratação em festas longas e no calor, especialmente se consumir álcool. Não misture álcool com medicamentos ou outras substâncias."},{"07","Confira antes de sair","Confirme roupa de cama, regras da hospedagem e previsão do tempo. Quantidades são sugestões: ajuste ao seu uso e à possibilidade de lavar roupas."}};
        for(String[] tip:tips){LinearLayout card=column();pad(card,20);card.setBackground(shape(Color.WHITE,18));add(card,text(tip[0]+"  /  "+tip[1],16,GREEN,true),8);add(card,text(tip[2],14,MUTED,false),0);add(content,card,12);}
        add(content,text("Mala Pronta • versão 1.5\nSeus dados ficam neste celular. Sem conta e sem internet.",12,MUTED,false),12);
    }
}
