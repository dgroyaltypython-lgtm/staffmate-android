package com.rrdigitalforge.staffmate;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.util.*;

public class DB extends SQLiteOpenHelper {
    public DB(Context c) { super(c, "staffmate.db", null, 1); }
    @Override public void onCreate(SQLiteDatabase d) {
        d.execSQL("CREATE TABLE staff(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT,role TEXT,salary REAL NOT NULL,mode TEXT DEFAULT 'Monthly',active INTEGER DEFAULT 1)");
        d.execSQL("CREATE TABLE attendance(id INTEGER PRIMARY KEY AUTOINCREMENT,staff_id INTEGER,date TEXT,status TEXT,in_time TEXT,out_time TEXT,FOREIGN KEY(staff_id) REFERENCES staff(id),UNIQUE(staff_id,date))");
        d.execSQL("CREATE TABLE advances(id INTEGER PRIMARY KEY AUTOINCREMENT,staff_id INTEGER,date TEXT,amount REAL,note TEXT)");
        d.execSQL("CREATE TABLE deductions(id INTEGER PRIMARY KEY AUTOINCREMENT,staff_id INTEGER,date TEXT,amount REAL,note TEXT)");
    }
    @Override public void onUpgrade(SQLiteDatabase d,int o,int n) {}
    public long addStaff(String n,String p,String r,double s,String mode) {
        ContentValues v=new ContentValues(); v.put("name",n);v.put("phone",p);v.put("role",r);v.put("salary",s);v.put("mode",mode);
        return getWritableDatabase().insert("staff",null,v);
    }
    public void updateStaff(int id,String n,String p,String r,double s,String mode) {
        ContentValues v=new ContentValues();v.put("name",n);v.put("phone",p);v.put("role",r);v.put("salary",s);v.put("mode",mode);
        getWritableDatabase().update("staff",v,"id=?",new String[]{""+id});
    }
    public void deactivate(int id) { ContentValues v=new ContentValues();v.put("active",0);getWritableDatabase().update("staff",v,"id=?",new String[]{""+id}); }
    public Cursor staff() { return getReadableDatabase().rawQuery("SELECT * FROM staff WHERE active=1 ORDER BY name",null); }
    public Cursor allStaff() { return getReadableDatabase().rawQuery("SELECT * FROM staff ORDER BY name",null); }
    public void mark(int sid,String date,String status,String in,String out) {
        ContentValues v=new ContentValues();v.put("staff_id",sid);v.put("date",date);v.put("status",status);v.put("in_time",in);v.put("out_time",out);
        getWritableDatabase().insertWithOnConflict("attendance",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    public Cursor attendance(String month) {
        return getReadableDatabase().rawQuery("SELECT a.date,s.name,a.status,a.in_time,a.out_time FROM attendance a JOIN staff s ON s.id=a.staff_id WHERE a.date LIKE ? ORDER BY a.date DESC,s.name",new String[]{month+"%"});
    }
    public Cursor attendanceFor(int sid,String month) {
        return getReadableDatabase().rawQuery("SELECT status,COUNT(*) FROM attendance WHERE staff_id=? AND date LIKE ? GROUP BY status",new String[]{""+sid,month+"%"});
    }
    public void addMoney(String table,int sid,String date,double amount,String note) {
        ContentValues v=new ContentValues();v.put("staff_id",sid);v.put("date",date);v.put("amount",amount);v.put("note",note);
        getWritableDatabase().insert(table,null,v);
    }
    public double moneyTotal(String table,int sid,String month) {
        Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM "+table+" WHERE staff_id=? AND date LIKE ?",new String[]{""+sid,month+"%"});
        double x=0;if(c.moveToFirst())x=c.getDouble(0);c.close();return x;
    }
    public double sumSalary() {
        Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(salary),0) FROM staff WHERE active=1",null);
        double x=0;if(c.moveToFirst())x=c.getDouble(0);c.close();return x;
    }
    public int count(String table,String where) {
        Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM "+table+(where==null?"":" WHERE "+where),null);
        int x=0;if(c.moveToFirst())x=c.getInt(0);c.close();return x;
    }
    public Cursor raw(String sql,String[] args) { return getReadableDatabase().rawQuery(sql,args); }
}
