package rma.shivam.audiorecorder.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.ArrayList;

import rma.shivam.audiorecorder.ARapp;
import rma.shivam.audiorecorder.global.Utils;
import rma.shivam.audiorecorder.helper.Hashids;

public class AutoStartHandler {
    public static boolean isActivated = false;
    public static String session = "_XXXXXXXXXXXX";
    public static ArrayList<String> packages = new ArrayList<>();
    public static ArrayList<String> appNames = new ArrayList<>();
    public static ArrayList<String> appUris = new ArrayList<>();
    public static int appCount = 0;
    public static int runningAppNo = 0;
    public static int currentIteration = 0;
    public static int iteration = 0;

    private static final String KEY_IS_ACTIVATED = "is activated";
    private static final String REGEX = "@@@";

    public static void createSession(){
        long now = System.currentTimeMillis();
        session = Hashids.hash12(ARapp.IMEI+now+"ITERATION");
        session = "_" + session;
        currentIteration = 0;
        Utils.logPrint(AutoStartHandler.class,"session",session);
    }

    public static void resetSession() {
        AutoStartHandler.session = "_XXXXXXXXXXXX";
        Utils.logPrint(AutoStartHandler.class,"session",session);
    }

    public static void saveClass(){
        SharedPreferences prefs = ARapp.context.getSharedPreferences(AutoStartHandler.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putBoolean("isActivated",isActivated);
            editor.putString("packages",listToString(packages));
            editor.putString("appNames",listToString(appNames));
            editor.putString("appUris",listToString(appUris));
            editor.putInt("appCount",appCount);
            editor.putInt("iteration",iteration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    public static void retriveClass(){
        SharedPreferences prefs = ARapp.context.getSharedPreferences(AutoStartHandler.class.getName(), Context.MODE_PRIVATE);
        isActivated = prefs.getBoolean("isActivated",isActivated);
        String temp;
        temp = prefs.getString("packages",null);
        packages = temp==null?packages:listFromString(temp);
        temp = prefs.getString("appNames",null);
        appNames = temp==null?appNames:listFromString(temp);
        temp = prefs.getString("appUris",null);
        appUris = temp==null?appUris:listFromString(temp);
        appCount = prefs.getInt("appCount",appCount);
        iteration = prefs.getInt("iteration",iteration);
    }

    private static String listToString(ArrayList<String> list){
        if (list == null){
            return "null";
        }
        String listInString = list.toString().replace("[","")
                .replace("]","")
                .replaceAll(" ","")
                .replaceAll(",",REGEX);
        return listInString;
    }

    private static ArrayList<String> listFromString(String string){
        if (string == null){
            return null;
        }
        ArrayList<String> list = new ArrayList<>();
        String[] data = string.split(REGEX);
        for(String d: data){
            list.add(d);
        }
        return list;
    }

}
