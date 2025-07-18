package rs.ac.uns.eventplanner.team7.utils;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.dto.notification.PersonalNotificationDTO;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebSocketService {

    private static final String tag = "WebSocketService";
    private static final String webSocketUrl =
            String.format("ws://%s:8080/api/websocket/websocket", BuildConfig.IP_ADDR);
    private final StompClient stompClient;
    private CompositeDisposable compositeDisposable;
    private final Context context;
    private final Gson gson = new GsonBuilder().create();

    public WebSocketService(Context context) {
        this.context = context;
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketUrl);
        resetSubscriptions();
    }

    public void connect(String bearerToken) {
        List<StompHeader> headers = List.of(new StompHeader("Authorization", bearerToken));
        stompClient.withClientHeartbeat(1000).withServerHeartbeat(1000);
        resetSubscriptions();

        Disposable dispLifecycle = stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d(tag, "WebSocket Connection Opened");
                    Disposable dispTopic = stompClient.topic("/user/queue/notifications")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(stompMessage -> {
                                Log.d(tag, "New notification: " + stompMessage.getPayload());
                                var dto = gson.fromJson(stompMessage.getPayload(), PersonalNotificationDTO.class);
                                NotificationUtils.showNotification(context, dto);
                            }, throwable -> Log.e(tag, "Error in WebSocket: ", throwable));
                    compositeDisposable.add(dispTopic);
                    break;
                case ERROR:
                    Log.e(tag, "WebSocket Error", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.d(tag, "WebSocket Connection Closed");
                    break;
            }
        });
        compositeDisposable.add(dispLifecycle);

        stompClient.connect(headers);

    }

    public void disconnect() {
        if (stompClient != null) stompClient.disconnect();
        if (compositeDisposable != null) compositeDisposable.dispose();
    }

    private void resetSubscriptions() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        compositeDisposable = new CompositeDisposable();
    }

}
