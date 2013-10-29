package uk.co.digitalbrainswitch.dbsblobodiary.list_models;

/**
 * Created by mingkichong on 25/10/2013.
 */
public class CalendarListModel {
    private String displayText = "";
    private String fileNameString = "";

    public CalendarListModel (String displayText, String fileNameString){
        setDisplayText(displayText);
        setFileNameString(fileNameString);
    }

    public void setDisplayText(String displayText){
        this.displayText = displayText;
    }

    public void setFileNameString(String fileNameString){
        this.fileNameString = fileNameString;
    }

    public String getDisplayText(){
        return this.displayText;
    }

    public String getFileNameString(){
        return this.fileNameString;
    }
}
