package rs.ac.uns.eventplanner.team7.utils;


import android.util.Log;

import java.util.Collections;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;

public class WebSocketService {

    private static final String TAG = "WebSocketService";
    private StompClient stompClient;

    public void connect(String token) {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, ClientUtils.WEBSOCKET_URL);

        List<StompHeader> headers = Collections.singletonList(new StompHeader("Authorization", "Bearer " + token));
        stompClient.connect(headers);

        stompClient.topic("/user/queue/notifications")
                .subscribe(stompMessage -> {
                    Log.d(TAG, "New notification: " + stompMessage.getPayload());
                }, throwable -> {
                    Log.e(TAG, "Error in WebSocket: ", throwable);
                });

        stompClient.lifecycle()
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "WebSocket Connection Opened");
                            break;
                        case ERROR:
                            Log.e(TAG, "WebSocket Error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.d(TAG, "WebSocket Connection Closed");
                            break;
                    }
                });
    }

    public void disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }

}
