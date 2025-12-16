import React, { useRef, useEffect, useState } from 'react';
import { FiPhone, FiVideo, FiMoreVertical, FiSmile, FiPaperclip, FiSend, FiImage, FiX } from "react-icons/fi";
import MessageItem from "./MessageItem";

const ChatWindow = ({ activeConversation, messages, onSendMessage, currentUser, onVoiceCall, onVideoCall, onEditMessage }) => {
    const [inputValue, setInputValue] = useState("");
    const [selectedFiles, setSelectedFiles] = useState([]);
    const messagesEndRef = useRef(null);
    const fileInputRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleFileSelect = (e) => {
        const files = Array.from(e.target.files);
        setSelectedFiles(prev => [...prev, ...files]);
    };

    const removeFile = (index) => {
        setSelectedFiles(prev => prev.filter((_, i) => i !== index));
    };

    const handleSend = () => {
        if (!inputValue.trim() && selectedFiles.length === 0) return;

        onSendMessage(inputValue, selectedFiles);
        setInputValue("");
        setSelectedFiles([]);
    };

    if (!activeConversation) {
        return (
            <div className="flex flex-col items-center justify-center h-full bg-white/50">
                <div className="w-24 h-24 bg-blue-50 rounded-full flex items-center justify-center mb-4">
                    <span className="text-4xl">👋</span>
                </div>
                <h3 className="text-xl font-bold text-gray-800">Welcome to Messages</h3>
                <p className="text-gray-500 mt-2">Select a conversation from the left to start chatting.</p>
            </div>
        );
    }

    return (
        <div className="flex flex-col h-full bg-white relative">
            {/* Header */}
            <div className="h-20 px-6 border-b border-gray-100 flex items-center justify-between bg-white/80 backdrop-blur-sm z-10 sticky top-0">
                <div className="flex items-center gap-4">
                    <div className="relative">
                        <img
                            src={activeConversation.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${activeConversation.username}`}
                            alt=""
                            className="w-10 h-10 rounded-full object-cover shadow-sm bg-gray-100"
                        />
                        <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 border-2 border-white rounded-full"></span>
                    </div>
                    <div>
                        <h2 className="text-lg font-bold text-gray-800 leading-tight">{activeConversation.username}</h2>
                        <div className="flex items-center gap-2">
                            <span className="w-1.5 h-1.5 rounded-full bg-green-500"></span>
                            <span className="text-xs text-gray-500 font-medium">Online</span>
                        </div>
                    </div>
                </div>

                <div className="flex items-center gap-4 text-gray-400">
                    <button
                        onClick={onVoiceCall}
                        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                        title="Voice call"
                    >
                        <FiPhone size={20} />
                    </button>
                    <button
                        onClick={onVideoCall}
                        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                        title="Video call"
                    >
                        <FiVideo size={20} />
                    </button>
                    <button className="p-2 hover:bg-gray-100 rounded-full transition-colors"><FiMoreVertical size={20} /></button>
                </div>
            </div>

            {/* Messages Area */}
            <div className="flex-1 overflow-y-auto px-6 py-6 bg-white space-y-6 custom-scrollbar">
                {/* Date Separator */}
                <div className="flex justify-center">
                    <span className="px-4 py-1 bg-gray-100 rounded-full text-xs font-semibold text-gray-500">Today</span>
                </div>

                {messages.map((msg, index) => (
                    <MessageItem
                        key={msg.id || index}
                        message={msg}
                        isOwn={msg.senderId === currentUser.id}
                        userAvatar={activeConversation.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${activeConversation.username}`}
                        myAvatar={currentUser.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${currentUser.username}`}
                        onEditMessage={onEditMessage}
                    />
                ))}
                <div ref={messagesEndRef} />
            </div>

            {/* Input Area */}
            <div className="p-6 bg-white pt-2">
                {/* File Preview */}
                {selectedFiles.length > 0 && (
                    <div className="mb-3 flex flex-wrap gap-2">
                        {selectedFiles.map((file, index) => (
                            <div key={index} className="relative group">
                                <div className="flex items-center gap-2 bg-blue-50 border border-blue-200 rounded-lg px-3 py-2 pr-8">
                                    <FiPaperclip className="text-blue-600" size={16} />
                                    <span className="text-sm text-blue-900 max-w-[150px] truncate">{file.name}</span>
                                </div>
                                <button
                                    onClick={() => removeFile(index)}
                                    className="absolute -top-2 -right-2 w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 transition-colors shadow-md"
                                >
                                    <FiX size={14} />
                                </button>
                            </div>
                        ))}
                    </div>
                )}

                <div className="bg-gray-50 rounded-3xl p-2 pl-4 flex items-end gap-2 shadow-sm border border-gray-100 transition-shadow focus-within:shadow-md focus-within:ring-1 focus-within:ring-blue-100">
                    <button className="p-2 text-gray-400 hover:text-gray-600 mb-1"><FiSmile size={24} /></button>
                    <textarea
                        value={inputValue}
                        onChange={(e) => setInputValue(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter' && !e.shiftKey) {
                                e.preventDefault();
                                handleSend();
                            }
                        }}
                        placeholder="Type message..."
                        className="flex-1 bg-transparent border-none focus:ring-0 resize-none py-3 max-h-32 text-gray-700 placeholder-gray-400"
                        rows={1}
                        style={{ minHeight: '48px' }}
                    />
                    <div className="flex items-center gap-1 mb-1 pr-1">
                        <input
                            type="file"
                            ref={fileInputRef}
                            onChange={handleFileSelect}
                            className="hidden"
                            multiple
                            accept="image/*,application/pdf,.doc,.docx"
                        />
                        <button
                            onClick={() => fileInputRef.current?.click()}
                            className="p-2 text-gray-400 hover:text-gray-600 rounded-full hover:bg-gray-200"
                        >
                            <FiPaperclip size={20} />
                        </button>
                        <button
                            onClick={() => fileInputRef.current?.click()}
                            className="p-2 text-gray-400 hover:text-gray-600 rounded-full hover:bg-gray-200"
                        >
                            <FiImage size={20} />
                        </button>
                        <button
                            onClick={handleSend}
                            disabled={!inputValue.trim() && selectedFiles.length === 0}
                            className={`ml-1 w-10 h-10 flex items-center justify-center rounded-xl transition-all shadow-md ${inputValue.trim() || selectedFiles.length > 0
                                ? 'bg-gradient-to-tr from-blue-600 to-purple-600 text-white hover:shadow-lg transform hover:scale-105'
                                : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                                }`}
                        >
                            <FiSend size={18} className={(inputValue.trim() || selectedFiles.length > 0) ? "ml-0.5" : ""} />
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ChatWindow;
