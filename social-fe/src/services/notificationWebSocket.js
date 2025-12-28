import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { API_ORIGIN } from '../config/HttpClient';

class NotificationWebSocket {
    constructor() {
        this.stompClient = null;
    }

    connect(onNotificationReceived) {
        // Remove /api if present for WS URL
        const msgUrl = API_ORIGIN.replace('/api', '') + '/ws';
        const socket = new SockJS(msgUrl);
        this.stompClient = Stomp.over(socket);
        this.stompClient.debug = () => { }; // Disable debug logs

        const token = localStorage.getItem('social_app_token');

        this.stompClient.connect(
            { Authorization: `Bearer ${token}` },
            () => {
                this.stompClient.subscribe('/user/queue/notifications', (message) => {
                    const notification = JSON.parse(message.body);
                    onNotificationReceived(notification);
                });
            },
            (error) => {
                console.error('Notification WS error:', error);
            }
        );
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

export default new NotificationWebSocket();
