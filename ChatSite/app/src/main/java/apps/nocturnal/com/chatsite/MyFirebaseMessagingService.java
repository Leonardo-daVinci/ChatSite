package apps.nocturnal.com.chatsite;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        String nTitle = remoteMessage.getNotification().getTitle();
        String nMessage = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String from_user_id = remoteMessage.getData().get("from_User_id");

        //Look for new Notification service (look for channel ID)

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);

        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        String channelID = "Default";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,channelID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(nTitle)
                .setContentText(nMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);


        int mNotificationID = (int) System.currentTimeMillis();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
        manager.notify(mNotificationID, mBuilder.build());
    }

    }
