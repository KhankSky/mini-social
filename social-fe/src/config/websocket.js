import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

let stompClient = null;

export const connectWebSocket = (onMessageReceived, onConnected, onError) => {
    const socket = new SockJS('http://152.42.186.105:8080/ws');
    const token = localStorage.getItem('social_app_token');
    const socket = new SockJS(`${API_ORIGIN}/ws?token=${token}`);
    stompClient = Stomp.over(socket);

    // Disable debug logs
    stompClient.debug = () => { };

    stompClient.connect(
        { Authorization: `Bearer ${token}` },
        () => {
            if (onConnected) onConnected();

            // Subscribe to private user queue
            stompClient.subscribe('/user/queue/messages', (payload) => {
                const message = JSON.parse(payload.body);
                if (onMessageReceived) onMessageReceived(message);
            });
        },
        (error) => {
            console.error('WebSocket error:', error);
            if (onError) onError(error);
            // Reconnect attempt in 5 seconds
            setTimeout(() => {
                connectWebSocket(onMessageReceived, onConnected, onError);
            }, 5000);
        }
    );
};

export const disconnectWebSocket = () => {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
};

export const sendMessageWS = (destination, payload) => {
    if (stompClient && stompClient.connected) {
        stompClient.send(destination, {}, JSON.stringify(payload));
    } else {
        console.error("WebSocket is not connected");
    }
};
