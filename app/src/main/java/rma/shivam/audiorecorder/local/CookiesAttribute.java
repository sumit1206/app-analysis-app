package rma.shivam.audiorecorder.local;

public class CookiesAttribute {
    /**
     *CREATE TABLE HISTORY (
     *     _id               INTEGER PRIMARY KEY AUTOINCREMENT,
     *     date_time,
     *     session_id,
     *     circle_session_id,
     *     app_name,
     *     audio_uploaded,
     *     csv_uploaded
     * );
     * */
    public static String TABLE_HISTORY = "HISTORY";
    public static String history_date_time = "date_time";
    public static String history_session_id = "session_id";
    public static String history_circle_session_id = "circle_session_id";
    public static String history_app_name = "app_name";
    public static String history_audio_uploaded = "audio_uploaded";
    public static String history_csv_uploaded = "csv_uploaded";
}
