package uk.co.digitalbrainswitch.dbsblobodiary.list_models;

/**
 * Created by mingkichong on 25/10/2013.
 */
public class EventDateListModel {
    private String dateString = "";
    private String fileNameString = "";

    public EventDateListModel(String dateString, String fileNameString){
        setDateString(dateString);
        setFileNameString(fileNameString);
    }

    public void setDateString(String dateString){
        this.dateString = dateString;
    }

    public void setFileNameString(String fileNameString){
        this.fileNameString = fileNameString;
    }

    public String getDateString(){
        return this.dateString;
    }

    public String getFileNameString(){
        return this.fileNameString;
    }
}
