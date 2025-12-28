// WebRTC Service for voice and video calls
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class WebRTCService {
    constructor() {
        this.peerConnection = null;
        this.localStream = null;
        this.remoteStream = null;
        this.stompClient = null;
        this.onRemoteStream = null;
        this.onCallEnd = null;

        this.config = {
            iceServers: [
                { urls: 'stun:stun.l.google.com:19302' },
                { urls: 'stun:stun1.l.google.com:19302' }
            ]
        };
    }

    // Connect to WebSocket for signaling
    connectSignaling(onCallOffer, onCallAnswer, onIceCandidate, onCallReject, onCallEnd) {
        const socket = new SockJS('http://152.42.186.105:8080/ws');
        this.stompClient = Stomp.over(socket);

        const token = localStorage.getItem('social_app_token');

        this.stompClient.connect(
            { Authorization: `Bearer ${token}` },
            () => {
                console.log('WebRTC signaling connected');

                // Subscribe to call queue
                this.stompClient.subscribe('/user/queue/call', (message) => {
                    const data = JSON.parse(message.body);
                    console.log('Received call signal:', data);

                    switch (data.type) {
                        case 'CALL_OFFER':
                            onCallOffer && onCallOffer(data);
                            break;
                        case 'CALL_ANSWER':
                            onCallAnswer && onCallAnswer(data);
                            break;
                        case 'ICE_CANDIDATE':
                            onIceCandidate && onIceCandidate(data);
                            break;
                        case 'CALL_REJECT':
                            onCallReject && onCallReject(data);
                            break;
                        case 'CALL_END':
                            onCallEnd && onCallEnd(data);
                            break;
                    }
                });
            },
            (error) => {
                console.error('WebRTC signaling error:', error);
            }
        );
    }

    // Initialize media stream
    async getMediaStream(audio = true, video = false) {
        try {
            this.localStream = await navigator.mediaDevices.getUserMedia({ audio, video });
            return this.localStream;
        } catch (error) {
            console.error('Error accessing media devices:', error);
            throw error;
        }
    }

    // Create peer connection
    createPeerConnection() {
        this.peerConnection = new RTCPeerConnection(this.config);

        // Add local stream tracks
        if (this.localStream) {
            this.localStream.getTracks().forEach(track => {
                this.peerConnection.addTrack(track, this.localStream);
            });
        }

        // Handle remote stream
        this.peerConnection.ontrack = (event) => {
            console.log('Received remote track');
            this.remoteStream = event.streams[0];
            this.onRemoteStream && this.onRemoteStream(this.remoteStream);
        };

        // Handle ICE candidates
        this.peerConnection.onicecandidate = (event) => {
            if (event.candidate) {
                console.log('New ICE candidate');
                // Will be sent via sendIceCandidate method
            }
        };

        return this.peerConnection;
    }

    // Create and send offer
    async createOffer(toUsername, isVideo = false) {
        if (!this.peerConnection) {
            this.createPeerConnection();
        }

        const offer = await this.peerConnection.createOffer();
        await this.peerConnection.setLocalDescription(offer);

        this.stompClient.send('/app/call.offer', {}, JSON.stringify({
            type: 'CALL_OFFER',
            to: toUsername,
            sdp: offer.sdp,
            isVideo: isVideo
        }));
    }

    // Create and send answer
    async createAnswer(toUsername, offerSdp) {
        if (!this.peerConnection) {
            this.createPeerConnection();
        }

        await this.peerConnection.setRemoteDescription(new RTCSessionDescription({ type: 'offer', sdp: offerSdp }));
        const answer = await this.peerConnection.createAnswer();
        await this.peerConnection.setLocalDescription(answer);

        this.stompClient.send('/app/call.answer', {}, JSON.stringify({
            type: 'CALL_ANSWER',
            to: toUsername,
            sdp: answer.sdp
        }));
    }

    // Handle received answer
    async handleAnswer(answerSdp) {
        await this.peerConnection.setRemoteDescription(new RTCSessionDescription({ type: 'answer', sdp: answerSdp }));
    }

    // Send ICE candidate
    sendIceCandidate(toUsername, candidate) {
        this.stompClient.send('/app/call.ice-candidate', {}, JSON.stringify({
            type: 'ICE_CANDIDATE',
            to: toUsername,
            candidate: candidate
        }));
    }

    // Add ICE candidate
    async addIceCandidate(candidate) {
        if (this.peerConnection) {
            await this.peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
        }
    }

    // Reject call
    rejectCall(toUsername) {
        this.stompClient.send('/app/call.reject', {}, JSON.stringify({
            to: toUsername
        }));
        this.cleanup();
    }

    // End call
    endCall(toUsername) {
        this.stompClient.send('/app/call.end', {}, JSON.stringify({
            to: toUsername
        }));
        this.cleanup();
    }

    // Toggle audio
    toggleAudio() {
        if (this.localStream) {
            const audioTrack = this.localStream.getAudioTracks()[0];
            if (audioTrack) {
                audioTrack.enabled = !audioTrack.enabled;
                return audioTrack.enabled;
            }
        }
        return false;
    }

    // Toggle video
    toggleVideo() {
        if (this.localStream) {
            const videoTrack = this.localStream.getVideoTracks()[0];
            if (videoTrack) {
                videoTrack.enabled = !videoTrack.enabled;
                return videoTrack.enabled;
            }
        }
        return false;
    }

    // Cleanup
    cleanup() {
        if (this.peerConnection) {
            this.peerConnection.close();
            this.peerConnection = null;
        }

        if (this.localStream) {
            this.localStream.getTracks().forEach(track => track.stop());
            this.localStream = null;
        }

        this.remoteStream = null;
    }

    disconnect() {
        this.cleanup();
        if (this.stompClient) {
            this.stompClient.disconnect();
            this.stompClient = null;
        }
    }
}

export default new WebRTCService();
