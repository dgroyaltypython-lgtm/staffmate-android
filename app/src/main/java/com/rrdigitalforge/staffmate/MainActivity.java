package com.rrdigitalforge.staffmate;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    DB db; LinearLayout page, nav; String month, today; int ink=Color.rgb(232,240,255);
    int muted=Color.rgb(160,177,205), blue=Color.rgb(92,145,255), purple=Color.rgb(151,105,255);
    @Override public void onCreate(Bundle b){super.onCreate(b);db=new DB(this);
        SimpleDateFormat m=new SimpleDateFormat("yyyy-MM",Locale.US), d=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
        month=m.format(new Date());today=d.format(new Date()); getWindow().setStatusBarColor(Color.rgb(10,16,34));getWindow().setNavigationBarColor(Color.rgb(10,16,34));home();}
    GradientDrawable bg(int a,int b,int radius){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});g.setCornerRadius(radius);g.setStroke(1,Color.argb(55,255,255,255));return g;}
    TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextColor(ink);t.setTextSize(size);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(2,3,2,3);return t;}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    void base(String title,String subtitle){
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);LinearLayout outer=column();outer.setPadding(18,18,18,22);
        outer.setBackgroundColor(Color.rgb(10,16,34));sc.addView(outer);setContentView(sc);
        outer.addView(text("STAFFMATE  •  RR DIGITAL FORGE",12,true));
        TextView h=text(title,27,true);h.setPadding(0,16,0,2);outer.addView(h);
        TextView sub=text(subtitle,13,false);sub.setTextColor(muted);outer.addView(sub);
        View gap=new View(this);gap.setLayoutParams(new LinearLayout.LayoutParams(1,16));outer.addView(gap);
        page=outer;
    }
    void card(String title,String value,String detail){
        LinearLayout c=column();c.setPadding(16,13,16,13);c.setBackground(bg(Color.rgb(32,48,83),Color.rgb(24,29,58),24));
        c.addView(text(title,12,false));TextView v=text(value,25,true);v.setPadding(0,5,0,1);c.addView(v);TextView d=text(detail,11,false);d.setTextColor(muted);c.addView(d);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=10;page.addView(c,p);
    }
    void button(String label,Runnable action){
        Button b=new Button(this);b.setText(label);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setTextSize(14);b.setBackground(bg(Color.rgb(75,117,220),Color.rgb(111,79,194),18));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=9;page.addView(b,p);b.setOnClickListener(v->action.run());
    }
    EditText field(String hint,int type){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);e.setHintTextColor(muted);e.setTextColor(ink);e.setTextSize(15);e.setInputType(type);e.setPadding(14,10,14,10);e.setBackground(bg(Color.rgb(27,39,68),Color.rgb(24,30,56),14));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=9;page.addView(e,p);return e;}
    void heading(String s){TextView t=text(s,17,true);t.setPadding(0,14,0,10);page.addView(t);}
    void home(){
        base("Good evening 👋","Your staff operations at a glance");
        int staff=db.count("staff","active=1"), present=db.count("attendance","date='"+today+"' AND status IN ('Present','Half Day')");
        card("ACTIVE STAFF",String.valueOf(staff),"People in your team");
        card("MARKED TODAY",String.valueOf(present),"Present or half-day entries");
        card("MONTHLY BASE PAYROLL","₹ "+money(db.sumSalary()),"Combined basic salary before attendance adjustments");
        card("TODAY",today,"Attendance date");
        heading("QUICK ACTIONS");
        button("＋  Add new staff",()->addStaff());
        button("✓  Mark attendance",()->attendanceForm());
        button("₹  Record advance",()->moneyForm("advances","Record staff advance"));
        button("▤  Salary & payroll",()->payroll());
        button("☷  Staff directory",()->staffList());
        button("◷  Attendance history",()->history());
        button("⇩  Export attendance CSV",()->exportCsv());
        footer();
    }
    void footer(){TextView f=text("Offline-first • Data stays on this phone • StaffMate v1.0",11,false);f.setTextColor(muted);f.setGravity(Gravity.CENTER);f.setPadding(0,20,0,10);page.addView(f);}
    String money(double x){return String.format(Locale.US,"%,.2f",x);}
    void addStaff(){
        base("Add staff member","Enter employee details and monthly or daily wage");
        EditText n=field("Full name",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        EditText p=field("Mobile number (optional)",InputType.TYPE_CLASS_PHONE);
        EditText r=field("Role / designation",InputType.TYPE_CLASS_TEXT);
        EditText s=field("Salary amount (₹)",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        Spinner mode=new Spinner(this);String[] modes={"Monthly","Daily"};mode.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,modes));page.addView(mode);
        button("Save staff member",()->{if(n.getText().toString().trim().isEmpty()||s.getText().toString().trim().isEmpty()){msg("Name and salary are required");return;}
            db.addStaff(n.getText().toString().trim(),p.getText().toString(),r.getText().toString(),Double.parseDouble(s.getText().toString()),mode.getSelectedItem().toString());msg("Staff member saved");staffList();});
        button("← Back to dashboard",()->home());
    }
    void staffList(){
        base("Staff directory","Active employees stored on this device");
        Cursor c=db.staff();if(c.getCount()==0)page.addView(text("No staff added yet. Use Add new staff to begin.",14,false));
        while(c.moveToNext()){final int id=c.getInt(0);String name=c.getString(1),phone=c.getString(2),role=c.getString(3),mode=c.getString(5);double sal=c.getDouble(4);
            LinearLayout box=column();box.setPadding(14,12,14,12);box.setBackground(bg(Color.rgb(30,43,74),Color.rgb(22,29,54),18));
            box.addView(text(name,17,true));box.addView(text((role==null?"":role)+"  •  "+(phone==null?"":phone),12,false));box.addView(text("₹ "+money(sal)+" / "+mode,13,true));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=9;page.addView(box,lp);
            button("Edit "+name,()->editStaff(id,name,phone,role,sal,mode));
            button("Deactivate "+name,()->new AlertDialog.Builder(this).setMessage("Deactivate "+name+"? Past records will remain.").setNegativeButton("Cancel",null).setPositiveButton("Deactivate",(a,b)->{db.deactivate(id);staffList();}).show());
        }c.close();button("＋ Add staff",()->addStaff());button("← Dashboard",()->home());
    }
    void editStaff(int id,String name,String phone,String role,double salary,String mode0){
        base("Edit staff","Update employee profile");
        EditText n=field("Full name",InputType.TYPE_CLASS_TEXT);n.setText(name);
        EditText p=field("Mobile",InputType.TYPE_CLASS_PHONE);p.setText(phone);
        EditText r=field("Role",InputType.TYPE_CLASS_TEXT);r.setText(role);
        EditText s=field("Salary",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);s.setText(""+salary);
        Spinner sp=new Spinner(this);sp.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Monthly","Daily"}));sp.setSelection(mode0.equals("Daily")?1:0);page.addView(sp);
        button("Save changes",()->{try{db.updateStaff(id,n.getText().toString(),p.getText().toString(),r.getText().toString(),Double.parseDouble(s.getText().toString()),sp.getSelectedItem().toString());staffList();}catch(Exception e){msg("Check salary amount");}});
        button("← Back",()->staffList());
    }
    void attendanceForm(){
        base("Mark attendance","Choose a staff member and record the day");
        Cursor c=db.staff();ArrayList<Integer> ids=new ArrayList<>();ArrayList<String> names=new ArrayList<>();
        while(c.moveToNext()){ids.add(c.getInt(0));names.add(c.getString(1));}c.close();
        if(ids.size()==0){page.addView(text("Add staff first to record attendance.",14,false));button("Add staff",()->addStaff());return;}
        Spinner who=new Spinner(this);who.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,names));page.addView(who);
        EditText date=field("Date YYYY-MM-DD (default today)",InputType.TYPE_CLASS_DATETIME);date.setText(today);
        Spinner status=new Spinner(this);status.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Present","Half Day","Absent","Leave","Weekly Off"}));page.addView(status);
        EditText in=field("IN time e.g. 09:00 AM (optional)",InputType.TYPE_CLASS_TEXT);
        EditText out=field("OUT time e.g. 06:00 PM (optional)",InputType.TYPE_CLASS_TEXT);
        button("Save attendance",()->{String dt=date.getText().toString().trim();if(!dt.matches("\\d{4}-\\d{2}-\\d{2}")){msg("Use date format YYYY-MM-DD");return;}
            db.mark(ids.get(who.getSelectedItemPosition()),dt,status.getSelectedItem().toString(),in.getText().toString(),out.getText().toString());msg("Attendance saved");home();});
        button("← Dashboard",()->home());
    }
    void moneyForm(String table,String title){
        base(title,"Record an advance or deduction against a staff member");
        Cursor c=db.staff();ArrayList<Integer> ids=new ArrayList<>();ArrayList<String> names=new ArrayList<>();while(c.moveToNext()){ids.add(c.getInt(0));names.add(c.getString(1));}c.close();
        if(ids.size()==0){page.addView(text("Add staff first.",14,false));return;}
        Spinner who=new Spinner(this);who.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,names));page.addView(who);
        EditText date=field("Date YYYY-MM-DD",InputType.TYPE_CLASS_DATETIME);date.setText(today);
        EditText amount=field("Amount ₹",InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText note=field("Note / reason (optional)",InputType.TYPE_CLASS_TEXT);
        button("Save record",()->{try{double a=Double.parseDouble(amount.getText().toString());if(a<=0)throw new Exception();db.addMoney(table,ids.get(who.getSelectedItemPosition()),date.getText().toString(),a,note.getText().toString());msg("Record saved");home();}catch(Exception e){msg("Enter a valid amount");}});
        button("← Dashboard",()->home());
    }
    void payroll(){
        base("Salary & payroll","Estimated month: "+month+" • attendance-based calculation");
        Cursor c=db.staff();if(c.getCount()==0)page.addView(text("Add staff to calculate payroll.",14,false));
        while(c.moveToNext()){
            int id=c.getInt(0);String name=c.getString(1),mode=c.getString(5);double base=c.getDouble(4);
            Cursor a=db.attendanceFor(id,month);int present=0,half=0,absent=0,leave=0,off=0;
            while(a.moveToNext()){String st=a.getString(0);int count=a.getInt(1);if(st.equals("Present"))present=count;else if(st.equals("Half Day"))half=count;else if(st.equals("Absent"))absent=count;else if(st.equals("Leave"))leave=count;else if(st.equals("Weekly Off"))off=count;}a.close();
            double gross=mode.equals("Daily")?base*(present+half*.5):base*(present+half*.5+off)/30.0;
            double adv=db.moneyTotal("advances",id,month),ded=db.moneyTotal("deductions",id,month),net=Math.max(0,gross-adv-ded);
            LinearLayout box=column();box.setPadding(14,12,14,12);box.setBackground(bg(Color.rgb(30,43,74),Color.rgb(22,29,54),18));
            box.addView(text(name,17,true));box.addView(text("Present "+present+"  •  Half "+half+"  •  Absent "+absent+"  •  Leave "+leave+"  •  Off "+off,12,false));
            box.addView(text("Gross ₹ "+money(gross),13,false));box.addView(text("Advance ₹ "+money(adv)+"  •  Deductions ₹ "+money(ded),12,false));box.addView(text("NET PAYABLE  ₹ "+money(net),18,true));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=10;page.addView(box,lp);
        }c.close();
        TextView note=text("Calculation note: Monthly salary uses 30-day basis. Present = 1 day, Half Day = 0.5 day, Weekly Off = paid day. Leave and Absent are unpaid. Daily wage uses present + half-day only. Adjust this policy to match your workplace rules.",11,false);note.setTextColor(muted);page.addView(note);
        button("＋ Record advance",()->moneyForm("advances","Record advance"));
        button("＋ Record deduction",()->moneyForm("deductions","Record deduction"));
        button("← Dashboard",()->home());
    }
    void history(){
        base("Attendance history","Records for "+month);
        Cursor c=db.attendance(month);if(c.getCount()==0)page.addView(text("No attendance records for this month.",14,false));
        while(c.moveToNext()){page.addView(text(c.getString(0)+"  •  "+c.getString(1),15,true));page.addView(text(c.getString(2)+"   IN "+c.getString(3)+"   OUT "+c.getString(4),12,false));View v=new View(this);v.setBackgroundColor(Color.argb(50,255,255,255));v.setLayoutParams(new LinearLayout.LayoutParams(-1,1));page.addView(v);}
        c.close();button("← Dashboard",()->home());
    }
    void exportCsv(){
        try{
            Cursor c=db.raw("SELECT a.date,s.name,s.role,a.status,a.in_time,a.out_time FROM attendance a JOIN staff s ON s.id=a.staff_id ORDER BY a.date DESC,s.name",null);
            StringBuilder csv=new StringBuilder("Date,Staff,Role,Status,IN,OUT\n");
            while(c.moveToNext()){for(int i=0;i<6;i++){if(i>0)csv.append(',');String x=c.getString(i)==null?"":c.getString(i).replace("\"","\"\"");csv.append('"').append(x).append('"');}csv.append('\n');}c.close();
            android.content.Intent send=new android.content.Intent(android.content.Intent.ACTION_SEND);send.setType("text/csv");send.putExtra(android.content.Intent.EXTRA_SUBJECT,"StaffMate attendance export");send.putExtra(android.content.Intent.EXTRA_TEXT,csv.toString());startActivity(android.content.Intent.createChooser(send,"Share attendance CSV"));
        }catch(Exception e){msg("Export failed: "+e.getMessage());}
    }
    void msg(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public void onBackPressed(){home();}
}
