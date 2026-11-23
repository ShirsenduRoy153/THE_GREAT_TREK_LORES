import React, { useEffect, useState, useRef } from 'react';
import { X, Send, MessageSquare, Compass, Sparkles } from 'lucide-react';
import { apiClient, type ApiResponse } from '../api/client';
import { useAuth } from '../context/AuthContext';
import type { ChatRoomDto, ChatMessageDto } from '../types/chat';

interface ChatModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialRoomId?: string | null;
}

export const ChatModal: React.FC<ChatModalProps> = ({ isOpen, onClose, initialRoomId }) => {
  const { user } = useAuth();
  const [rooms, setRooms] = useState<ChatRoomDto[]>([]);
  const [activeRoomId, setActiveRoomId] = useState<string | null>(initialRoomId || null);
  const [messages, setMessages] = useState<ChatMessageDto[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loadingRooms, setLoadingRooms] = useState(true);
  const [sending, setSending] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isOpen) {
      loadRooms();
    }
  }, [isOpen]);

  useEffect(() => {
    if (initialRoomId) {
      setActiveRoomId(initialRoomId);
    }
  }, [initialRoomId]);

  useEffect(() => {
    if (activeRoomId) {
      loadMessages(activeRoomId);
      // Auto-poll messages every 3 seconds for lightweight, real-time fallback
      const interval = setInterval(() => {
        loadMessages(activeRoomId, true);
      }, 3000);
      return () => clearInterval(interval);
    }
  }, [activeRoomId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const loadRooms = async () => {
    setLoadingRooms(true);
    try {
      const res = await apiClient.get<ApiResponse<ChatRoomDto[]>>('/chat/rooms');
      if (res.data.success) {
        setRooms(res.data.data);
        if (!activeRoomId && res.data.data.length > 0) {
          setActiveRoomId(res.data.data[0].id);
        }
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingRooms(false);
    }
  };

  const loadMessages = async (roomId: string, isPolling = false) => {
    try {
      const res = await apiClient.get<ApiResponse<ChatMessageDto[]>>(`/chat/rooms/${roomId}/messages`);
      if (res.data.success) {
        setMessages(res.data.data);
      }
    } catch (err) {
      if (!isPolling) console.error(err);
    }
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim() || !activeRoomId || sending) return;

    setSending(true);
    const text = newMessage.trim();
    setNewMessage('');

    try {
      const res = await apiClient.post<ApiResponse<ChatMessageDto>>(`/chat/rooms/${activeRoomId}/messages`, {
        messageText: text,
        messageType: 'TEXT',
      });
      if (res.data.success) {
        setMessages((prev) => [...prev, res.data.data]);
      }
    } catch (err) {
      console.error('Failed to send message', err);
    } finally {
      setSending(false);
    }
  };

  if (!isOpen) return null;

  const currentRoom = rooms.find((r) => r.id === activeRoomId);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-4xl h-[85vh] flex overflow-hidden shadow-2xl">
        {/* Left Sidebar: Room List */}
        <div className="w-80 border-r border-slate-800 flex flex-col bg-slate-950/60">
          <div className="p-4 border-b border-slate-800 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <MessageSquare className="w-5 h-5 text-emerald-400" />
              <h3 className="font-bold text-white text-sm">Discussions</h3>
            </div>
            <span className="text-[10px] bg-slate-800 text-slate-400 px-2 py-0.5 rounded-full">
              {rooms.length} Rooms
            </span>
          </div>

          <div className="flex-1 overflow-y-auto p-2 space-y-1.5">
            {loadingRooms ? (
              <div className="text-center py-10 text-xs text-slate-500">Loading chats...</div>
            ) : rooms.length > 0 ? (
              rooms.map((room) => {
                const isActive = room.id === activeRoomId;
                const isGroupRoom = room.roomType === 'MAIN_GROUP';
                return (
                  <button
                    key={room.id}
                    onClick={() => setActiveRoomId(room.id)}
                    className={`w-full text-left p-3 rounded-xl transition flex flex-col gap-1 border ${
                      isActive
                        ? 'bg-emerald-950/40 border-emerald-800/80'
                        : 'bg-slate-900/40 border-slate-800/60 hover:bg-slate-800/50'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span
                        className={`text-[9px] px-1.5 py-0.2 rounded font-semibold border ${
                          isGroupRoom
                            ? 'bg-emerald-950 text-emerald-400 border-emerald-800'
                            : 'bg-sky-950 text-sky-400 border-sky-800'
                        }`}
                      >
                        {isGroupRoom ? '🔒 Group Chat' : '🤝 Organizer'}
                      </span>
                      <span className="text-[10px] text-slate-500">
                        👥 {room.participantCount}
                      </span>
                    </div>
                    <span className="text-xs font-bold text-white truncate">{room.title}</span>
                  </button>
                );
              })
            ) : (
              <div className="text-center py-12 px-4 text-xs text-slate-500">
                <Compass className="w-8 h-8 text-slate-600 mx-auto mb-2" />
                <p>No chat rooms yet.</p>
                <p className="text-[11px] text-slate-600 mt-1">
                  Join a companion group or accept an organizer connection to begin chatting!
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Right: Active Chat Room Window */}
        <div className="flex-1 flex flex-col bg-slate-900">
          {/* Header */}
          <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between bg-slate-900/80">
            <div>
              {currentRoom ? (
                <>
                  <div className="flex items-center gap-2">
                    <h3 className="font-bold text-white text-base">{currentRoom.title}</h3>
                    <span
                      className={`text-[10px] px-2 py-0.5 rounded font-semibold ${
                        currentRoom.roomType === 'MAIN_GROUP'
                          ? 'bg-emerald-950 text-emerald-400'
                          : 'bg-sky-950 text-sky-400'
                      }`}
                    >
                      {currentRoom.roomType === 'MAIN_GROUP' ? 'Private Members Chat' : 'Organizer Negotiation Room'}
                    </span>
                  </div>
                  <span className="text-xs text-slate-400">
                    {currentRoom.roomType === 'MAIN_GROUP'
                      ? 'Strictly companions only • Organizers blocked'
                      : 'Private channel between your group and this organizer'}
                  </span>
                </>
              ) : (
                <span className="text-sm text-slate-400">Select a chat room</span>
              )}
            </div>
            <button
              onClick={onClose}
              className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Message Stream */}
          <div className="flex-1 overflow-y-auto p-6 space-y-4">
            {messages.length > 0 ? (
              messages.map((m) => {
                const isMe = user?.userId === m.senderUserId;
                return (
                  <div
                    key={m.id}
                    className={`flex flex-col ${isMe ? 'items-end' : 'items-start'} space-y-1`}
                  >
                    <div className="flex items-center gap-1.5 text-[11px] text-slate-400">
                      <span className="font-semibold text-slate-300">{m.senderDisplayName}</span>
                      <span
                        className={`text-[9px] px-1.5 py-0.2 rounded font-medium ${
                          m.senderRole === 'ORGANIZER'
                            ? 'bg-sky-950 text-sky-400 border border-sky-800'
                            : m.senderRole === 'GROUP_ADMIN'
                            ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                            : 'bg-slate-800 text-slate-400'
                        }`}
                      >
                        {m.senderRole}
                      </span>
                      <span className="text-slate-500 text-[10px]">
                        {new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>

                    <div
                      className={`max-w-md p-3 rounded-2xl text-xs leading-relaxed shadow-sm ${
                        isMe
                          ? 'bg-emerald-600 text-white rounded-tr-none'
                          : 'bg-slate-800/80 text-slate-200 border border-slate-700/60 rounded-tl-none'
                      }`}
                    >
                      <p className="whitespace-pre-line">{m.messageText}</p>
                    </div>
                  </div>
                );
              })
            ) : currentRoom ? (
              <div className="text-center py-20 text-xs text-slate-500">
                <Sparkles className="w-8 h-8 text-emerald-500/50 mx-auto mb-2" />
                <p className="font-semibold text-slate-400">This is the start of the discussion</p>
                <p className="mt-1">Send a message to coordinate travel, gear, and itinerary details!</p>
              </div>
            ) : (
              <div className="text-center py-20 text-xs text-slate-500">Select a room to view messages</div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Input Box */}
          {currentRoom && (
            <form onSubmit={handleSendMessage} className="p-4 border-t border-slate-800 bg-slate-950/60 flex gap-2">
              <input
                type="text"
                placeholder={
                  currentRoom.roomType === 'MAIN_GROUP'
                    ? 'Message companions privately...'
                    : 'Ask organizer about itinerary, pickup, or pricing...'
                }
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                className="flex-1 bg-slate-900 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
              />
              <button
                type="submit"
                disabled={sending || !newMessage.trim()}
                className="bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition disabled:opacity-40 shadow-md shadow-emerald-950"
              >
                <Send className="w-3.5 h-3.5" />
                <span>Send</span>
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
