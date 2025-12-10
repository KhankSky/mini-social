import React, { useState, useEffect } from 'react';
import ConversationList from "../components/ConversationList";
import ChatWindow from "../components/ChatWindow";
import NewMessageModal from "../components/NewMessageModal";
import IncomingCallModal from "../components/IncomingCallModal";
import ActiveCallWindow from "../components/ActiveCallWindow";
import { getConversations, getConversationMessages, sendMessage, markAllAsRead } from "../services/message";
import { getOnlineUsers } from "../services/presence";
import { connectWebSocket, disconnectWebSocket } from "../config/websocket";
import webrtcService from "../services/webrtc";
import httpClient from "../config/HttpClient";

const Messages = () => {
    const [conversations, setConversations] = useState([]);
    const [activeConversation, setActiveConversation] = useState([]);
    const [messages, setMessages] = useState([]);
    const [currentUser, setCurrentUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showNewMessageModal, setShowNewMessageModal] = useState(false);
    const [onlineUsers, setOnlineUsers] = useState([]);

    // Call states
    const [incomingCall, setIncomingCall] = useState(null);
    const [activeCall, setActiveCall] = useState(null);
    const [isMuted, setIsMuted] = useState(false);
    const [isVideoOff, setIsVideoOff] = useState(false);

    useEffect(() => {
        const fetchInitialData = async () => {
            let user = localStorage.getItem("user");

            if (!user) {
                try {
                    const res = await httpClient.get('/auth/me');
                    const userData = res.data;
                    localStorage.setItem('user', JSON.stringify(userData));
                    setCurrentUser(userData);

                    await loadConversations();
                    await loadOnlineUsers();
                    setupWebSocket(userData);
                } catch (error) {
                    console.error("Failed to fetch user", error);
                }
            } else {
                const userData = JSON.parse(user);
                setCurrentUser(userData);

                await loadConversations();
                await loadOnlineUsers();
                setupWebSocket(userData);
            }
            setLoading(false);
        };

        fetchInitialData();

        const interval = setInterval(loadOnlineUsers, 30000);

        return () => {
            disconnectWebSocket();
            clearInterval(interval);
            webrtcService.disconnect();
        };
    }, []);

    const setupWebSocket = (userData) => {
        connectWebSocket(
            (newMessage) => handleNewMessage(newMessage, userData.id),
            () => {
                console.log("Connected to WS");
                loadOnlineUsers();
            },
            (err) => console.error(err)
        );

        // Setup WebRTC signaling
        webrtcService.connectSignaling(
            handleCallOffer,
            handleCallAnswer,
            handleIceCandidate,
            handleCallReject,
            handleCallEnd
        );

        webrtcService.onRemoteStream = (stream) => {
            console.log('Got remote stream');
        };
    };

    const loadConversations = async () => {
        try {
            const res = await getConversations();
            setConversations(res.data);
        } catch (error) {
            console.error("Failed to load conversations", error);
        }
    };

    const loadOnlineUsers = async () => {
        try {
            const users = await getOnlineUsers();
            setOnlineUsers(users);
        } catch (error) {
            console.error("Failed to load online users", error);
        }
    };

    const handleSelectUser = async (conversation) => {
        setActiveConversation(conversation);
        try {
            const res = await getConversationMessages(conversation.userId);
            setMessages(res.data);

            if (conversation.unreadCount > 0) {
                await markAllAsRead(conversation.userId);
                setConversations(prev => prev.map(c =>
                    c.userId === conversation.userId ? { ...c, unreadCount: 0 } : c
                ));
            }
        } catch (error) {
            console.error("Failed to load messages", error);
            setMessages([]);
        }
    };

    const handleNewMessage = (newMessage, currentUserId) => {
        setActiveConversation(prevActive => {
            if (prevActive && (newMessage.senderId === prevActive.userId || newMessage.receiverId === prevActive.userId)) {
                setMessages(prev => [...prev, newMessage]);
            }
            return prevActive;
        });
        loadConversations();
    };

    const handleSendMessage = async (content, files) => {
        if (!activeConversation || !currentUser) return;

        const tempData = {
            receiverId: activeConversation.userId,
            content: content,
            files: files
        };

        try {
            const res = await sendMessage(tempData);
            setMessages(prev => [...prev, res.data]);
            loadConversations();
        } catch (error) {
            console.error("Failed to send message", error);
        }
    };

    const handleNewMessageClick = () => {
        setShowNewMessageModal(true);
    };

    const handleSelectNewUser = (user) => {
        const existingConv = conversations.find(c => c.userId === user.userId);
        if (existingConv) {
            handleSelectUser(existingConv);
        } else {
            setConversations(prev => [user, ...prev]);
            handleSelectUser(user);
        }
    };

    // ===== Call Handlers =====
    const handleVoiceCall = async () => {
        if (!activeConversation) return;
        try {
            await webrtcService.getMediaStream(true, false);
            await webrtcService.createOffer(activeConversation.username, false);
            setActiveCall({ peer: activeConversation, isVideo: false });
        } catch (error) {
            console.error('Failed to start voice call:', error);
            alert('Cannot access microphone. Please check permissions.');
        }
    };

    const handleVideoCall = async () => {
        if (!activeConversation) return;
        try {
            await webrtcService.getMediaStream(true, true);
            await webrtcService.createOffer(activeConversation.username, true);
            setActiveCall({ peer: activeConversation, isVideo: true });
        } catch (error) {
            console.error('Failed to start video call:', error);
            alert('Cannot access camera/microphone. Please check permissions.');
        }
    };

    const handleCallOffer = async (data) => {
        const caller = conversations.find(c => c.username === data.from);
        if (!caller) {
            // Find user info if not in conversations
            const user = onlineUsers.find(u => u.username === data.from);
            if (user) {
                setIncomingCall({
                    caller: user,
                    isVideo: data.isVideo,
                    sdp: data.sdp
                });
            }
        } else {
            setIncomingCall({
                caller,
                isVideo: data.isVideo,
                sdp: data.sdp
            });
        }
    };

    const handleAcceptCall = async () => {
        if (!incomingCall) return;
        try {
            await webrtcService.getMediaStream(true, incomingCall.isVideo);
            await webrtcService.createAnswer(incomingCall.caller.username, incomingCall.sdp);
            setActiveCall({ peer: incomingCall.caller, isVideo: incomingCall.isVideo });
            setIncomingCall(null);
        } catch (error) {
            console.error('Failed to accept call:', error);
            alert('Cannot access media devices.');
        }
    };

    const handleRejectCall = () => {
        if (incomingCall) {
            webrtcService.rejectCall(incomingCall.caller.username);
            setIncomingCall(null);
        }
    };

    const handleCallAnswer = async (data) => {
        await webrtcService.handleAnswer(data.sdp);
    };

    const handleIceCandidate = async (data) => {
        if (data.candidate) {
            await webrtcService.addIceCandidate(data.candidate);
        }
    };

    const handleCallReject = () => {
        setActiveCall(null);
        webrtcService.cleanup();
    };

    const handleCallEnd = () => {
        setActiveCall(null);
        webrtcService.cleanup();
    };

    const handleToggleMute = () => {
        const enabled = webrtcService.toggleAudio();
        setIsMuted(!enabled);
    };

    const handleToggleVideo = () => {
        const enabled = webrtcService.toggleVideo();
        setIsVideoOff(!enabled);
    };

    const handleEndCall = () => {
        if (activeCall) {
            webrtcService.endCall(activeCall.peer.username);
            setActiveCall(null);
            setIsMuted(false);
            setIsVideoOff(false);
        }
    };

    if (loading) {
        return (
            <div className="flex h-screen items-center justify-center bg-gray-50">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-gray-600">Loading messages...</p>
                </div>
            </div>
        );
    }

    return (
        <>
            <div className="flex bg-white h-[calc(100vh-64px)] overflow-hidden">
                <div className="w-[380px] flex-shrink-0 border-r border-gray-100 flex flex-col bg-white">
                    <ConversationList
                        conversations={conversations}
                        selectedUserId={activeConversation?.userId}
                        onSelectUser={handleSelectUser}
                        onNewMessage={handleNewMessageClick}
                        onlineUsers={onlineUsers}
                    />
                </div>

                <div className="flex-1 flex flex-col bg-gray-50 relative">
                    <ChatWindow
                        activeConversation={activeConversation}
                        messages={messages}
                        onSendMessage={handleSendMessage}
                        currentUser={currentUser}
                        onVoiceCall={handleVoiceCall}
                        onVideoCall={handleVideoCall}
                    />
                </div>
            </div>

            <NewMessageModal
                isOpen={showNewMessageModal}
                onClose={() => setShowNewMessageModal(false)}
                onSelectUser={handleSelectNewUser}
            />

            <IncomingCallModal
                caller={incomingCall?.caller}
                isVideo={incomingCall?.isVideo}
                onAccept={handleAcceptCall}
                onReject={handleRejectCall}
            />

            {activeCall && (
                <ActiveCallWindow
                    peer={activeCall.peer}
                    isVideo={activeCall.isVideo}
                    localStream={webrtcService.localStream}
                    remoteStream={webrtcService.remoteStream}
                    isMuted={isMuted}
                    isVideoOff={isVideoOff}
                    onToggleMute={handleToggleMute}
                    onToggleVideo={handleToggleVideo}
                    onEndCall={handleEndCall}
                />
            )}
        </>
    );
};

export default Messages;
