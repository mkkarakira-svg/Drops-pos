package com.drops.pos;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
  LinearLayout root, body; TextView title, subtitle; final int NAVY=Color.rgb(15,23,42), BLUE=Color.rgb(37,99,235), BG=Color.rgb(245,247,250);
  int dp(float v){ return (int)(v*getResources().getDisplayMetrics().density+.5f); }
  TextView txt(String s,int size,int color,boolean bold){ TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(dp(14),dp(10),dp(14),dp(10));if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t; }
  public void onCreate(Bundle b){super.onCreate(b); showDashboard();}
  void shell(String page,String sub){
    root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
    LinearLayout head=new LinearLayout(this);head.setOrientation(LinearLayout.VERTICAL);head.setPadding(dp(12),dp(14),dp(12),dp(12));head.setBackgroundColor(NAVY);
    title=txt("DROPS POS  •  "+page,22,Color.WHITE,true);subtitle=txt(sub,12,0xffcbd5e1,false);head.addView(title);head.addView(subtitle);root.addView(head);
    ScrollView sv=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(12),dp(12),dp(12),dp(90));sv.addView(body);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setBackgroundColor(Color.WHITE);
    addNav(nav,"الرئيسية",()->showDashboard());addNav(nav,"بيع",()->showPOS());addNav(nav,"المخزون",()->showStock());addNav(nav,"التقارير",()->showReports());
    root.addView(nav,new LinearLayout.LayoutParams(-1,dp(64)));setContentView(root);
  }
  void addNav(LinearLayout p,String s,Runnable r){Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setAllCaps(false);b.setOnClickListener(v->r.run());p.addView(b,new LinearLayout.LayoutParams(0,-1,1));}
  void card(String h,String v,String foot){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(8),dp(8),dp(8),dp(8));c.setBackgroundColor(Color.WHITE);c.addView(txt(h,13,0xff64748b,false));c.addView(txt(v,25,NAVY,true));c.addView(txt(foot,12,0xff64748b,false));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(10));body.addView(c,lp);}
  void action(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setTextSize(15);b.setAllCaps(false);b.setOnClickListener(v->r.run());body.addView(b,new LinearLayout.LayoutParams(-1,dp(58)));}
  void showDashboard(){shell("لوحة التحكم","Drops Detergents • USD / LBP");card("مبيعات اليوم","$ 0.00","جاهز لتسجيل أول فاتورة");card("قيمة المخزون","$ 0.00","تتحدث مع حركات الستوك");card("تنبيه المخزون","0","مواد أو منتجات تحت الحد الأدنى");action("＋ فاتورة بيع جديدة",()->showPOS());action("المنتجات والمخزون",()->showStock());action("العملاء والموردون",()->simple("العملاء والموردون","إدارة الحسابات وجهات التعامل"));action("المصاريف",()->simple("المصاريف","سجل مصاريف التشغيل اليومية"));action("التصنيع",()->simple("التصنيع","إنتاج وتحويل المواد الخام إلى منتج جاهز"));}
  void showPOS(){shell("نقطة البيع","فاتورة سريعة • 1L / 4L / 5L");String[] p={"Dishwashing • 1 L","Dishwashing • 4 L","Dishwashing • 5 L","Laundry Gel • 1 L","Laundry Gel • 4 L","Hand Soap • 1 L","Hand Soap • 4 L","Floor Cleaner • 4 L"};for(String x:p)action("＋  "+x,()->Toast.makeText(this,"أضيف إلى السلة",Toast.LENGTH_SHORT).show());card("إجمالي الفاتورة","$ 0.00","الدفع: نقدي / دين");action("إتمام وحفظ البيع",()->Toast.makeText(this,"واجهة الحفظ جاهزة للربط بقاعدة البيانات",Toast.LENGTH_LONG).show());}
  void showStock(){shell("المخزون","منتجات جاهزة • مواد خام • حركة الستوك");card("Dishwashing","0 L","عبوات 1L / 4L / 5L");card("Laundry Gel","0 L","عبوات 1L / 4L / 5L");card("Hand Soap","0 L","عبوات 1L / 4L / 5L");card("Floor Cleaner","0 L","عبوات 4L");action("＋ إدخال مخزون",()->simple("إدخال مخزون","استلام إنتاج أو شراء"));action("− إخراج / تسوية مخزون",()->simple("تسوية مخزون","تلف، عينة، أو تصحيح"));action("المواد الخام",()->simple("المواد الخام","الكميات والتكلفة والمورد"));}
  void showReports(){shell("التقارير","مبيعات • مخزون • مصاريف • ربح");card("مبيعات اليوم","$ 0.00","0 فاتورة");card("مصاريف اليوم","$ 0.00","0 حركة");card("صافي الحركة","$ 0.00","قبل تكلفة البضاعة");action("تقرير المبيعات",()->simple("تقرير المبيعات","يومي / أسبوعي / شهري"));action("تقرير المخزون",()->simple("تقرير المخزون","الرصيد والحركة والتنبيهات"));action("تقرير الأرباح",()->simple("الأرباح","المبيعات - التكلفة - المصاريف"));}
  void simple(String p,String s){shell(p,s);card("القسم جاهز","Drops POS V1","سيتم ربطه بقاعدة البيانات المحلية");}
}
