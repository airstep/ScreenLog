package tgs.screen.toggle.log.model;

import java.util.Date;

import io.realm.RealmObject;

public class ScreenState extends RealmObject {
    public Date date;
    public boolean toggle;
    public long diffOn;
    public long diffOff;

    public Date getDate() {
        return this.date;
    }

    public boolean getToggle() {
        return toggle;
    }

    public long getDiffOn() {
        return diffOn;
    }

    public long getDiffOff() {
        return diffOff;
    }
}
