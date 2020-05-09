package rma.shivam.audiorecorder.model;

public class AppData {
    String appName;
    String appPackage;
    String appUri;
    boolean isSelected;

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public String getAppUri() {
        return appUri;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public AppData(String appName, String appPackage, String appUri, boolean isSelected) {
        this.appName = appName;
        this.appPackage = appPackage;
        this.appUri = appUri;
        this.isSelected = isSelected;
    }
}
