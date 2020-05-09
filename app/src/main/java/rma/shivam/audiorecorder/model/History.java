package rma.shivam.audiorecorder.model;

public class History {
    String date_time;
    String session_id;
    String circle_session_id;
    String app_name;
    String audio_uploaded;
    String csv_uploaded;

    public String getDate_time() {
        return date_time;
    }

    public String getSession_id() {
        return session_id;
    }

    public String getCircle_session_id() {
        return circle_session_id;
    }

    public String getApp_name() {
        return app_name;
    }

    public String getAudio_uploaded() {
        return audio_uploaded;
    }

    public String getCsv_uploaded() {
        return csv_uploaded;
    }

    public History(String date_time, String session_id, String circle_session_id, String app_name, String audio_uploaded, String csv_uploaded) {
        this.date_time = date_time;
        this.session_id = session_id;
        this.circle_session_id = circle_session_id;
        this.app_name = app_name;
        this.audio_uploaded = audio_uploaded;
        this.csv_uploaded = csv_uploaded;
    }
}
