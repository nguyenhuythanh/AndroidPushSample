package mbass.android.pushsample;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.nifcloud.mbaas.core.NCMBDialogPushConfiguration;
import com.nifcloud.mbaas.core.NCMBFirebaseMessagingService;
import com.nifcloud.mbaas.core.NCMBPush;

import java.util.Map;
import java.util.Random;

/**
 * Created by ThanhNH on 4/19/18.
 */

public class MyCustomService extends NCMBFirebaseMessagingService {
    static final String NOTIFICATION_OVERLAP_KEY = "notificationOverlap";
    static NCMBDialogPushConfiguration dialogPushConfiguration = new NCMBDialogPushConfiguration();

    @Override
    public void onNewToken(String token){
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage != null && remoteMessage.getData() != null){

            Bundle bundle = getBundleFromRemoteMessage(remoteMessage);
            Map<String, String> data = remoteMessage.getData();

            if(bundle.containsKey("com.nifty.Dialog")){

                //ダイアログを設定しないタイプ
                //dialogPushConfiguration.setDisplayType(NCMBDialogPushConfiguration.DIALOG_DISPLAY_NONE);

                //標準的なダイアログを表示するタイプ
                dialogPushConfiguration.setDisplayType(NCMBDialogPushConfiguration.DIALOG_DISPLAY_DIALOG);

                //背景画像を設定するタイプ
                //dialogPushConfiguration.setDisplayType(NCMBDialogPushConfiguration.DIALOG_DISPLAY_BACKGROUND);

                //オリジナルのレイアウトを設定するタイプ
                //dialogPushConfiguration.setDisplayType(NCMBDialogPushConfiguration.DIALOG_DISPLAY_ORIGINAL);

                //ダイアログ表示のハンドリング
                NCMBPush.dialogPushHandler(this, bundle, dialogPushConfiguration);

            } else {
                sendNotification(bundle);
            }

        }
    }

    private void sendNotification(Bundle pushData) {

        //サイレントプッシュ
        if ((!pushData.containsKey("message")) && (!pushData.containsKey("title"))) {
            return;
        }

        NotificationCompat.Builder notificationBuilder = notificationSettings(pushData);

        /*
         * 通知重複設定
         * 0:常に最新の通知のみ表示
         * 1:最新以外の通知も複数表示
         */
        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        boolean containsKey = appInfo.metaData.containsKey(NOTIFICATION_OVERLAP_KEY);
        int overlap = appInfo.metaData.getInt(NOTIFICATION_OVERLAP_KEY);

        //デフォルト複数表示
        int notificationId = new Random().nextInt();

        if (overlap == 0 && containsKey) {
            //最新のみ表示
            notificationId = 0;
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
