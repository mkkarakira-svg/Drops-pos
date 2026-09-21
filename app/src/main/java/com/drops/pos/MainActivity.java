package com.drops.pos;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.Color;\nimport android.graphics.Typeface;\nimport android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
  DB db; LinearLayout root, body; TextView title;
  int green=Color.rgb(7,96,55), pale=Color.rgb(239,248,243), ink=Color.rgb(22,36,30), navy=Color.rgb(18,40,72);
  @Override public void onCreate(Bundle b){super.onCreate(b);db=new DB(this); dashboard();}

  TextView tv(String s,int sp,boolean bold){ TextView v=new TextView(this); v.setText(s);v.setTextSize(sp);v.setTextColor(Color.rgb(30,40,35));v.setPadding(18,14,18,14); if(bold)v.setTypeface(null,1); return v;}
  Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(16);b.setTextColor(ink);b.setAllCaps(false);b.setGravity(Gravity.CENTER);b.setMinHeight(64);b.setPadding(16,12,16,12);GradientDrawable g=new GradientDrawable();g.setColor(Color.WHITE);g.setCornerRadius(22);g.setStroke(1,Color.rgb(220,228,224));b.setBackground(g);return b;}
  LinearLayout box(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(14,12,14,12);x.setBackgroundColor(Color.WHITE);return x;}
  void shell(String t){
    root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(Color.rgb(246,248,247));
    LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(12,8,12,8);
    Button back=btn("‹");back.setOnClickListener(v->dashboard());head.addView(back,new LinearLayout.LayoutParams(70,60));
    title=tv(t,21,true);title.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);head.addView(title,new LinearLayout.LayoutParams(0,64,1));root.addView(head);
    ScrollView sv=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(14,6,14,20);sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
  }
  void dashboard(){
    shell("DROPS POS FINAL 4.1"); ((ViewGroup)title.getParent()).getChildAt(0).setVisibility(View.INVISIBLE);
    ((ViewGroup)title.getParent()).setBackgroundColor(navy); title.setTextColor(Color.WHITE); title.setGravity(Gravity.CENTER);
    TextView brand=tv("DROPS DETERGENTS",22,true);brand.setTextColor(navy);brand.setGravity(Gravity.CENTER);body.addView(brand);
    TextView sub=tv("نقطة البيع وإدارة المصنع",14,false);sub.setGravity(Gravity.CENTER);body.addView(sub);
    LinearLayout stats=new LinearLayout(this);stats.setOrientation(LinearLayout.HORIZONTAL);stats.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    TextView sales=tv("مبيعات اليوم\n$"+money(db.one("SELECT COALESCE(SUM(total),0) FROM sales WHERE created>=?",startDay())),16,true);sales.setGravity(Gravity.CENTER);
    TextView inv=tv("الفواتير\n"+(int)db.one("SELECT COUNT(*) FROM invoices WHERE created>=?",startDay()),16,true);inv.setGravity(Gravity.CENTER);
    stats.addView(sales,new LinearLayout.LayoutParams(0,110,1));stats.addView(inv,new LinearLayout.LayoutParams(0,110,1));body.addView(stats);
    String[] a={"بيع","إنتاج","مشتريات","مخزون","فواتير","تقارير","مواد خام","وصفات"};
    GridLayout grid=new GridLayout(this);grid.setColumnCount(2);grid.setUseDefaultMargins(true);
    for(String z:a){Button b=btn(z);b.setOnClickListener(v->open(((Button)v).getText().toString()));GridLayout.LayoutParams p=new GridLayout.LayoutParams();p.width=0;p.height=145;p.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);p.setMargins(8,8,8,8);grid.addView(b,p);}body.addView(grid);
  }
  void refreshStats(){
    LinearLayout c=box(); c.addView(tv("اليوم",17,true));
    c.addView(tv("المبيعات: $"+money(db.one("SELECT COALESCE(SUM(total),0) FROM sales WHERE created>=?",startDay()))+"   |   الفواتير: "+(int)db.one("SELECT COUNT(*) FROM invoices WHERE created>=?",startDay()),15,false));
    c.addView(tv("قيمة مخزون المنتجات: $"+money(db.one("SELECT COALESCE(SUM(price*qty),0) FROM products")),14,false));
    body.addView(c,0);
  }
  void open(String s){switch(s){case "بيع":sale();break;case "إنتاج":production();break;case "مشتريات":purchases();break;case "مخزون":stock();break;case "فواتير":invoices();break;case "تقارير":reports();break;case "مواد خام":raw();break;case "وصفات":recipes();break;}}
  long startDay(){Calendar c=Calendar.getInstance();c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c.getTimeInMillis();}
  String money(double d){return String.format(Locale.US,"%.2f",d);}
  EditText input(String hint){EditText e=new EditText(this);e.setHint(hint);e.setPadding(14,8,14,8);return e;}
  void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}

  void sale(){
    shell("بيع"); Cursor c=db.q("SELECT id,name,size,price,qty FROM products ORDER BY name,size");
    while(c.moveToNext()){int id=c.getInt(0), q=c.getInt(4);double price=c.getDouble(3);LinearLayout r=box();r.addView(tv(c.getString(1)+"  "+c.getString(2),16,true));r.addView(tv("السعر $"+money(price)+"  •  المخزون "+q,14,false));Button b=btn("إضافة للبيع");b.setEnabled(q>0);b.setOnClickListener(v->saleDialog(id));r.addView(b);body.addView(r);}c.close();
  }
  void saleDialog(int pid){
    EditText qty=input("الكمية"); qty.setInputType(2); EditText paid=input("المدفوع");paid.setInputType(8194);
    LinearLayout l=box();l.addView(qty);l.addView(paid);
    new AlertDialog.Builder(this).setTitle("فاتورة بيع").setView(l).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{
      try{int n=Integer.parseInt(qty.getText().toString());double p=Double.parseDouble(paid.getText().toString());db.sell(pid,n,p);toast("تم حفظ الفاتورة وخصم المخزون");sale();}catch(Exception e){toast(e.getMessage());}
    }).show();
  }

  void production(){
    shell("الإنتاج / الطبخات");
    body.addView(tv("الطبخة تخصم المواد الخام تلقائياً وتزيد مخزون المنتج الجاهز.",14,false));
    Cursor c=db.q("SELECT DISTINCT p.id,p.name,p.size FROM recipes r JOIN products p ON p.id=r.product_id ORDER BY p.name");
    while(c.moveToNext()){int id=c.getInt(0);Button b=btn(c.getString(1)+"  "+c.getString(2));b.setOnClickListener(v->batchDialog(id));body.addView(b,new LinearLayout.LayoutParams(-1,70));}c.close();
    body.addView(tv("آخر الطبخات",17,true));
    c=db.q("SELECT b.id,p.name,b.liters,b.cost,b.created FROM batches b JOIN products p ON p.id=b.product_id ORDER BY b.id DESC LIMIT 30");
    while(c.moveToNext())body.addView(tv("#"+c.getInt(0)+"  "+c.getString(1)+"  "+money(c.getDouble(2))+" L  •  $"+money(c.getDouble(3)),14,false));c.close();
  }
  void batchDialog(int pid){
    EditText liters=input("حجم الطبخة بالليتر");liters.setInputType(8194);
    new AlertDialog.Builder(this).setTitle("تسجيل طبخة").setView(liters).setNegativeButton("إلغاء",null).setPositiveButton("تنفيذ",(d,w)->{
      try{double l=Double.parseDouble(liters.getText().toString());DB.BatchResult r=db.makeBatch(pid,l);toast("تمت الطبخة #"+r.id+"\nالتكلفة $"+money(r.cost)+" • $"+money(r.cost/l)+"/L");production();}catch(Exception e){toast(e.getMessage());}
    }).show();
  }

  void stock(){shell("مخزون المنتجات");Cursor c=db.q("SELECT name,size,qty,price FROM products ORDER BY name,size");while(c.moveToNext())body.addView(tv(c.getString(0)+"  "+c.getString(1)+"\nالمخزون: "+c.getInt(2)+"  •  $"+money(c.getDouble(3)),15,true));c.close();}
  void raw(){shell("المواد الخام");Cursor c=db.q("SELECT name,qty,unit,cost FROM raw_materials ORDER BY name");while(c.moveToNext())body.addView(tv(c.getString(0)+"\n"+money(c.getDouble(1))+" "+c.getString(2)+"  •  $"+money(c.getDouble(3))+"/unit",15,true));c.close();}
  void purchases(){shell("المشتريات");body.addView(tv("تُسجّل المشتريات داخل مخزون المواد الخام وتحدّث متوسط التكلفة.",14,false));Cursor c=db.q("SELECT rm.name,p.qty,rm.unit,p.total FROM purchases p JOIN raw_materials rm ON rm.id=p.raw_id ORDER BY p.id DESC LIMIT 100");while(c.moveToNext())body.addView(tv(c.getString(0)+"  +"+money(c.getDouble(1))+" "+c.getString(2)+"  •  $"+money(c.getDouble(3)),14,false));c.close();}
  void invoices(){shell("الفواتير");Cursor c=db.q("SELECT id,total,paid,created FROM invoices ORDER BY id DESC LIMIT 100");DateFormat f=new SimpleDateFormat("dd/MM HH:mm",Locale.US);while(c.moveToNext())body.addView(tv("فاتورة #"+c.getInt(0)+"  •  $"+money(c.getDouble(1))+"\nمدفوع $"+money(c.getDouble(2))+"  •  "+f.format(new Date(c.getLong(3))),14,true));c.close();}
  void reports(){shell("التقارير");long now=System.currentTimeMillis(),month=now-30L*86400000L;body.addView(tv("مبيعات اليوم: $"+money(db.one("SELECT COALESCE(SUM(total),0) FROM sales WHERE created>=?",startDay())),18,true));body.addView(tv("آخر 30 يوم: $"+money(db.one("SELECT COALESCE(SUM(total),0) FROM sales WHERE created>=?",month)),18,true));body.addView(tv("قيمة المواد الخام: $"+money(db.one("SELECT COALESCE(SUM(qty*cost),0) FROM raw_materials")),18,true));body.addView(tv("عدد الطبخات: "+(int)db.one("SELECT COUNT(*) FROM batches"),18,true));}
  void recipes(){shell("الوصفات");Cursor c=db.q("SELECT p.name,p.size,rm.name,r.qty_per_liter,rm.unit FROM recipes r JOIN products p ON p.id=r.product_id JOIN raw_materials rm ON rm.id=r.raw_id ORDER BY p.name,rm.name");while(c.moveToNext())body.addView(tv(c.getString(0)+" "+c.getString(1)+"\n"+c.getString(2)+": "+money(c.getDouble(3))+" "+c.getString(4)+"/L",14,false));c.close();}

  public static class DB extends SQLiteOpenHelper {
    static final int VER=5; SQLiteDatabase x;
    DB(Context c){super(c,"drops.db",null,VER);x=getWritableDatabase();}
    public void onCreate(SQLiteDatabase d){schema(d);}
    void schema(SQLiteDatabase d){
      d.execSQL("CREATE TABLE IF NOT EXISTS products(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,size TEXT,price REAL,qty INTEGER DEFAULT 0)");
      d.execSQL("CREATE TABLE IF NOT EXISTS invoices(id INTEGER PRIMARY KEY AUTOINCREMENT,total REAL,paid REAL,created INTEGER)");
      d.execSQL("CREATE TABLE IF NOT EXISTS sales(id INTEGER PRIMARY KEY AUTOINCREMENT,invoice_id INTEGER,product_id INTEGER,qty INTEGER,total REAL,created INTEGER)");
      d.execSQL("CREATE TABLE IF NOT EXISTS stock_moves(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,qty INTEGER,type TEXT,created INTEGER)");
      d.execSQL("CREATE TABLE IF NOT EXISTS raw_materials(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,qty REAL,unit TEXT,cost REAL)");
      d.execSQL("CREATE TABLE IF NOT EXISTS purchases(id INTEGER PRIMARY KEY AUTOINCREMENT,raw_id INTEGER,qty REAL,unit_cost REAL,total REAL,created INTEGER)");
      d.execSQL("CREATE TABLE IF NOT EXISTS recipes(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,raw_id INTEGER,qty_per_liter REAL,UNIQUE(product_id,raw_id))");
      d.execSQL("CREATE TABLE IF NOT EXISTS batches(id INTEGER PRIMARY KEY AUTOINCREMENT,product_id INTEGER,liters REAL,cost REAL,created INTEGER)");
      d.execSQL("CREATE TABLE IF NOT EXISTS raw_moves(id INTEGER PRIMARY KEY AUTOINCREMENT,raw_id INTEGER,qty REAL,type TEXT,batch_id INTEGER,created INTEGER)");
      d.execSQL("CREATE TABLE IF NOT EXISTS product_costs(product_id INTEGER PRIMARY KEY,cost_per_unit REAL DEFAULT 0,updated INTEGER)");
    }
    public void onUpgrade(SQLiteDatabase d,int o,int n){schema(d);}
    Cursor q(String s,Object...a){String[] z=new String[a.length];for(int i=0;i<a.length;i++)z[i]=String.valueOf(a[i]);return x.rawQuery(s,z);}
    double one(String s,Object...a){Cursor c=q(s,a);double v=c.moveToFirst()?c.getDouble(0):0;c.close();return v;}
    void sell(int pid,int qty,double paid){
      if(qty<=0)throw new IllegalArgumentException("الكمية غير صحيحة");
      x.beginTransaction();try{
        Cursor c=q("SELECT price,qty FROM products WHERE id=?",pid);if(!c.moveToFirst())throw new IllegalArgumentException("المنتج غير موجود");double price=c.getDouble(0);int have=c.getInt(1);c.close();if(have<qty)throw new IllegalArgumentException("المخزون غير كافٍ");
        long t=System.currentTimeMillis();ContentValues i=new ContentValues();i.put("total",price*qty);i.put("paid",paid);i.put("created",t);long inv=x.insertOrThrow("invoices",null,i);
        ContentValues s=new ContentValues();s.put("invoice_id",inv);s.put("product_id",pid);s.put("qty",qty);s.put("total",price*qty);s.put("created",t);x.insertOrThrow("sales",null,s);
        x.execSQL("UPDATE products SET qty=qty-? WHERE id=?",new Object[]{qty,pid});ContentValues m=new ContentValues();m.put("product_id",pid);m.put("qty",-qty);m.put("type","SALE");m.put("created",t);x.insert("stock_moves",null,m);x.setTransactionSuccessful();
      }finally{x.endTransaction();}
    }
    static class BatchResult{long id;double cost;BatchResult(long i,double c){id=i;cost=c;}}
    BatchResult makeBatch(int pid,double liters){
      if(liters<=0)throw new IllegalArgumentException("حجم الطبخة غير صحيح");
      x.beginTransaction();try{
        Cursor r=q("SELECT r.raw_id,r.qty_per_liter,rm.qty,rm.cost,rm.name FROM recipes r JOIN raw_materials rm ON rm.id=r.raw_id WHERE r.product_id=?",pid);
        ArrayList<double[]> rows=new ArrayList<>();ArrayList<String> names=new ArrayList<>();double cost=0;int count=0;
        while(r.moveToNext()){count++;double need=r.getDouble(1)*liters,have=r.getDouble(2);if(have+0.000001<need)throw new IllegalArgumentException("المادة غير كافية: "+r.getString(4)+"\nالمطلوب "+need+" والمتوفر "+have);rows.add(new double[]{r.getInt(0),need,r.getDouble(3)});names.add(r.getString(4));cost+=need*r.getDouble(3);}r.close();
        if(count==0)throw new IllegalArgumentException("لا توجد وصفة لهذا المنتج");
        long t=System.currentTimeMillis();ContentValues b=new ContentValues();b.put("product_id",pid);b.put("liters",liters);b.put("cost",cost);b.put("created",t);long bid=x.insertOrThrow("batches",null,b);
        for(double[] z:rows){x.execSQL("UPDATE raw_materials SET qty=qty-? WHERE id=?",new Object[]{z[1],(int)z[0]});ContentValues m=new ContentValues();m.put("raw_id",(int)z[0]);m.put("qty",-z[1]);m.put("type","BATCH");m.put("batch_id",bid);m.put("created",t);x.insertOrThrow("raw_moves",null,m);}
        int units=(int)Math.round(liters);x.execSQL("UPDATE products SET qty=qty+? WHERE id=?",new Object[]{units,pid});ContentValues sm=new ContentValues();sm.put("product_id",pid);sm.put("qty",units);sm.put("type","PRODUCTION");sm.put("created",t);x.insertOrThrow("stock_moves",null,sm);
        ContentValues pc=new ContentValues();pc.put("product_id",pid);pc.put("cost_per_unit",cost/liters);pc.put("updated",t);x.insertWithOnConflict("product_costs",null,pc,SQLiteDatabase.CONFLICT_REPLACE);
        x.setTransactionSuccessful();return new BatchResult(bid,cost);
      }finally{x.endTransaction();}
    }
  }
}