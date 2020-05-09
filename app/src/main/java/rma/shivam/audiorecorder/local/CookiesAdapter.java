package rma.shivam.audiorecorder.local;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.model.History;

public class CookiesAdapter
{
    protected static final String TAG = "DataAdapter";

    private final Context mContext;
    private SQLiteDatabase cookiesDb;
    private CookiesHelper cookiesHelper;

    public CookiesAdapter(Context context)
    {
        this.mContext = context;
        cookiesHelper = new CookiesHelper(mContext);
    }

    public CookiesAdapter createDatabase() throws SQLException
    {
        try
        {
            Utils.logPrint(getClass(),"cookies","creating triggered");
            cookiesHelper.createDataBase();
        }
        catch (IOException e)
        {
            Utils.logPrint(getClass(),"Error creating db",Log.getStackTraceString(e));
//            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public CookiesAdapter openReadable() throws SQLException
    {
        try
        {
            cookiesHelper.openDataBase();
            cookiesHelper.close();
            cookiesDb = cookiesHelper.getReadableDatabase();
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }

    public CookiesAdapter openWritable() throws SQLException
    {
        try
        {
            cookiesHelper.openDataBase();
            cookiesHelper.close();
            cookiesDb = cookiesHelper.getWritableDatabase();
        }
        catch (SQLException mSQLException)
        {
            Log.e(TAG, "open >>"+ mSQLException.toString());
            throw mSQLException;
        }
        return this;
    }


    public void close()
    {
        cookiesHelper.close();
    }

    public void addHisory(History history){
        try {
            String sql = "INSERT INTO HISTORY  VALUES(NULL,'"+history.getDate_time()+"','"+history.getSession_id()+"','"+
                    history.getCircle_session_id()+"','"+history.getApp_name()+"','"+history.getAudio_uploaded()+"','"+history.getCsv_uploaded()+"')";
            cookiesDb.execSQL(sql);
//            Utils.logPrint(getClass(),"History added",sql);
        } catch (SQLException mSQLException) {
            Utils.logPrint(getClass(),"addHistoryData",Log.getStackTraceString(mSQLException));
        }
    }

    public String getFromHistory(String sessionId, String attribute) throws NullPointerException
    {
        try {
            String sql ="SELECT " + attribute +
                    " FROM " + CookiesAttribute.TABLE_HISTORY +
                    " WHERE " + CookiesAttribute.history_session_id + " = '" + sessionId + "'";
//            Utils.logPrint(getClass(),"query",sql);
            Cursor mCur = cookiesDb.rawQuery(sql, null);
            if (mCur!=null)
            {
                mCur.moveToNext();
            }else {
                Utils.logPrint(getClass(),"mCur","null");
            }
            try {
                return mCur.getString(0);
            }catch (Exception e){
                Utils.logPrint(getClass(),"Error fetching value1",Log.getStackTraceString(e));
                return  null;
            }
        }catch (Exception e) {
            Utils.logPrint(getClass(),"Error fetching value2",Log.getStackTraceString(e));
            return null;
        }
    }

    public void updateHistory(String attribute, String value, String sessionId)
    {
        try
        {
            String sql = "UPDATE "+ CookiesAttribute.TABLE_HISTORY+" SET "+attribute+" = '"+value+"'" +
                    " WHERE "+ CookiesAttribute.history_session_id+" = '"+sessionId+"'";
            cookiesDb.execSQL(sql);
//            Utils.logPrint(getClass(),"History updated",sql);
        }
        catch (SQLException mSQLException)
        {
            Utils.logPrint(getClass(), "error update", Log.getStackTraceString(mSQLException));
        }
    }

    public void deleteHistory(String sessionId)
    {
        try
        {//DELETE FROM STUDENT WHERE student_id = 'ddd'
            String sql = "DELETE FROM "+CookiesAttribute.TABLE_HISTORY+" WHERE "+CookiesAttribute.history_session_id+" = '"+sessionId+"'";
            cookiesDb.execSQL(sql);
//            Utils.logPrint(getClass(),"History deleted",sql);
        }
        catch (SQLException mSQLException)
        {
            Utils.logPrint(getClass(), "error update", Log.getStackTraceString(mSQLException));
        }
    }

    public ArrayList<History> getHistoryList()
    {
        try
        {
            String sql ="SELECT * FROM "+ CookiesAttribute.TABLE_HISTORY + " ORDER BY "+CookiesAttribute.history_date_time+" DESC";
            ArrayList<History> histories = null;
            History history;
            Cursor cr = cookiesDb.rawQuery(sql, null);
            if (cr!=null)
            {
                histories = new ArrayList<>();
                while (cr.moveToNext()) {
                    String date_time = cr.getString(cr.getColumnIndex(CookiesAttribute.history_date_time));
                    String session_id = cr.getString(cr.getColumnIndex(CookiesAttribute.history_session_id));
                    String circle_session_id = cr.getString(cr.getColumnIndex(CookiesAttribute.history_circle_session_id));
                    String app_name = cr.getString(cr.getColumnIndex(CookiesAttribute.history_app_name));
                    String audio_uploaded = cr.getString(cr.getColumnIndex(CookiesAttribute.history_audio_uploaded));
                    String csv_uploaded = cr.getString(cr.getColumnIndex(CookiesAttribute.history_csv_uploaded));
                    history = new History(date_time,session_id,circle_session_id,app_name,audio_uploaded,csv_uploaded);
                    histories.add(history);
                }
            }
            return histories;
        }
        catch (SQLException mSQLException)
        {
            Utils.logPrint(getClass(),"SQLException",Log.getStackTraceString(mSQLException));
            return null;
        }
    }

//    public String getFromTeacher(String attribute) throws NullPointerException
//    {
//        try {
//            String sql ="SELECT " + attribute + " FROM " + CookiesAttribute.TABLE_TEACHER;
//
//            Cursor mCur = cookiesDb.rawQuery(sql, null);
//            if (mCur!=null)
//            {
//                mCur.moveToNext();
//            }
////            Utils.logPrint(getClass(),"QUERY",sql);
//            try {
//                return mCur.getString(0);
//            }catch (Exception e){
//                return  null;
//            }
//        }catch (Exception e) {
//            Utils.logPrint(getClass(),"Error fetching value",Log.getStackTraceString(e));
//            return null;
//        }
//    }
//
//
//    public void addTeacherData(String name, String email, String phone)
//    {
//        try {
//            String sql = "INSERT INTO TEACHER(_id, name, email, phone, image_uri) " +
//                    "VALUES (NULL,'"+name+"','"+email+"','"+phone+"',NULL)";
//            cookiesDb.execSQL(sql);
//        } catch (SQLException mSQLException) {
//            Utils.logPrint(getClass(),"addTeacherData",Log.getStackTraceString(mSQLException));
//        }
//    }
//
//    public void addStudentData(String studentId, String name, String roll, String klass, String section)
//    {
//        try {
//            String sql = "INSERT INTO STUDENT (student_id, name, roll, class, section) " +
//                    "VALUES ('"+studentId+"','"+name+"','"+roll+"','"+klass+"','"+section+"')";
//            cookiesDb.execSQL(sql);
//            Utils.logPrint(getClass(),"Student",studentId+" added");
//        } catch (SQLException mSQLException) {
//            Utils.logPrint(getClass(),"addStudentData",Log.getStackTraceString(mSQLException));
//        }
//    }
//
//    public void updateTeacher(String attribute, String value)
//    {
//        try
//        {
//            String sql = "UPDATE "+ CookiesAttribute.TABLE_TEACHER+" SET "+attribute+" = '"+value+"'";
//            cookiesDb.execSQL(sql);
//        }
//        catch (SQLException mSQLException)
//        {
//            Utils.logPrint(getClass(), "error update", Log.getStackTraceString(mSQLException));
//        }
//    }
//
//    public void updateStudent(String attribute, String value, String studentId)
//    {
//        try
//        {
//            String sql = "UPDATE "+ CookiesAttribute.TABLE_STUDENT+" SET "+attribute+" = '"+value+"'" +
//                    " WHERE "+ CookiesAttribute.student_id+" = '"+studentId+"'";
//            cookiesDb.execSQL(sql);
//        }
//        catch (SQLException mSQLException)
//        {
//            Utils.logPrint(getClass(), "error update", Log.getStackTraceString(mSQLException));
//        }
//    }
//
//    public void delete(String tableName){
//        try
//        {
//            String sql = "DELETE FROM "+tableName;
//            cookiesDb.execSQL(sql);
//        }
//        catch (SQLException mSQLException)
//        {
//            Utils.logPrint(getClass(), "error update", Log.getStackTraceString(mSQLException));
//        }
//    }
//
//
//
//    public ArrayList<Student> getStudentList()
//    {
//        try
//        {
//            String sql ="SELECT * FROM STUDENT";
//            ArrayList<Student> students = null;
//            Student student;
//            Cursor cr = cookiesDb.rawQuery(sql, null);
//            if (cr!=null)
//            {
//                students = new ArrayList<>();
//                while (cr.moveToNext()) {
//                    String id = cr.getString(cr.getColumnIndex(CookiesAttribute.student_id));
//                    String name = cr.getString(cr.getColumnIndex(CookiesAttribute.student_name));
//                    String roll = cr.getString(cr.getColumnIndex(CookiesAttribute.student_roll));
//                    String klass = cr.getString(cr.getColumnIndex(CookiesAttribute.student_class));
//                    String section = cr.getString(cr.getColumnIndex(CookiesAttribute.student_section));
//                    String imageUri = cr.getString(cr.getColumnIndex(CookiesAttribute.student_image_uri));
//                    student = new Student(id,name,roll,klass,section,imageUri);
//                    students.add(student);
//                }
//            }
//            return students;
//        }
//        catch (SQLException mSQLException)
//        {
//            Utils.logPrint(getClass(),"SQLException",Log.getStackTraceString(mSQLException));
//            return null;
//        }
//    }
//
//    public Teacher getTeacher() throws NullPointerException
//    {
//        try
//        {
//            String sql ="SELECT * FROM "+CookiesAttribute.TABLE_TEACHER;
//            Teacher teacher = null ;
//            Cursor cr = cookiesDb.rawQuery(sql, null);
//            if (cr!=null)
//            {
//                while (cr.moveToNext()) {
//                    String userName = Utils.getString(mContext, Constant.KEY_USER_ID,null);
//                    String name = cr.getString(cr.getColumnIndex(CookiesAttribute.teacher_name));
//                    String email = cr.getString(cr.getColumnIndex(CookiesAttribute.teacher_email));
//                    String phone = cr.getString(cr.getColumnIndex(CookiesAttribute.teacher_phone));
//                    String image_uri = cr.getString(cr.getColumnIndex(CookiesAttribute.teacher_image_uri));
//                    teacher = new Teacher(userName,name,email,phone,image_uri);
//                }
//            }
//            return teacher;
//        }
//        catch (SQLException mSQLException)
//        {
//            Utils.logPrint(getClass(),"SQLException",Log.getStackTraceString(mSQLException));
//            return null;
//        }
//    }

}