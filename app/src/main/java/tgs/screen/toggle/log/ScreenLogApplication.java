package tgs.screen.toggle.log;

import android.app.Application;

import io.realm.Realm;

public class ScreenLogApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
