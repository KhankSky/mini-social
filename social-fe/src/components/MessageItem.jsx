import React from 'react';
import dayjs from "dayjs";
import { FiCheck } from "react-icons/fi";

const MessageItem = ({ message, isOwn, userAvatar, myAvatar }) => {
    return (
        <div className={`flex w-full ${isOwn ? "justify-end" : "justify-start"} group`}>
            <div className={`flex max-w-[75%] gap-3 ${isOwn ? "flex-row-reverse" : "flex-row"}`}>
                {/* Avatar */}
                <div className="flex-shrink-0 self-end">
                    <img
                        src={isOwn ? myAvatar : userAvatar}
                        alt="avatar"
                        className="w-8 h-8 rounded-full object-cover shadow-sm bg-gray-200"
                    />
                </div>

                {/* Bubble Container */}
                <div className={`flex flex-col ${isOwn ? "items-end" : "items-start"}`}>
                    {/* Name (Optional, maybe for groups, skip for 1-1 to keep clean) */}
                    {/* <span className="text-xs text-gray-400 mb-1 ml-1">{isOwn ? "You" : message.senderName}</span> */}

                    {/* Bubble */}
                    <div
                        className={`relative px-5 py-3 shadow-sm text-[15px] leading-relaxed break-words ${isOwn
                                ? "bg-gradient-to-tr from-blue-600 to-indigo-600 text-white rounded-2xl rounded-br-none"
                                : "bg-gray-100 text-gray-800 rounded-2xl rounded-bl-none"
                            }`}
                    >
                        {message.content}

                        {/* Attachments */}
                        {message.attachments && message.attachments.length > 0 && (
                            <div className="mt-3 flex flex-col gap-2">
                                {message.attachments.map((url, index) => (
                                    <img
                                        key={index}
                                        src={`http://localhost:9090${url}`}
                                        alt="attachment"
                                        className="max-w-full rounded-lg border border-white/20"
                                    />
                                ))}
                            </div>
                        )}
                    </div>

                    {/* Meta (Time + Status) */}
                    <div className={`flex items-center gap-1 mt-1.5 ${isOwn ? "mr-1" : "ml-1"}`}>
                        <span className="text-[11px] font-medium text-gray-400">
                            {dayjs(message.sentAt).format("hh:mm A")}
                        </span>
                        {isOwn && (
                            <span className={`text-[12px] flex items-center ${message.isRead ? "text-blue-500" : "text-gray-300"}`}>
                                {message.isRead ? <div className="flex -space-x-1"><FiCheck /><FiCheck /></div> : <FiCheck />}
                            </span>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default MessageItem;
