import React, { useEffect, useState } from 'react';
import { X, Check, XCircle, Clock, AlertCircle } from 'lucide-react';
import { apiClient, type ApiResponse } from '../api/client';
import type { ConnectionDto, ConnectionStatus } from '../types/connection';

interface OrganizerInboxModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const OrganizerInboxModal: React.FC<OrganizerInboxModalProps> = ({ isOpen, onClose }) => {
  const [connections, setConnections] = useState<ConnectionDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      loadConnections();
    }
  }, [isOpen]);

  const loadConnections = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.get<ApiResponse<ConnectionDto[]>>('/connections/organizer/me');
      if (res.data.success) {
        setConnections(res.data.data);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load enquiries');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async (connectionId: string, status: ConnectionStatus) => {
    setActionLoading(true);
    try {
      await apiClient.patch(`/connections/${connectionId}/status`, { status });
      loadConnections();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to update status');
    } finally {
      setActionLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl max-h-[85vh] flex flex-col overflow-hidden shadow-2xl">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
          <div>
            <h3 className="font-semibold text-lg text-white">Organizer Inquiries & Connections</h3>
            <p className="text-xs text-slate-400">Incoming inquiries from companion groups and your active pitches</p>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-6 space-y-4">
          {error && (
            <div className="p-3 rounded-lg bg-rose-950/50 border border-rose-800/50 text-rose-300 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {loading ? (
            <div className="text-center py-12 text-slate-500">Loading inquiries...</div>
          ) : connections.length > 0 ? (
            <div className="space-y-3">
              {connections.map((c) => (
                <div
                  key={c.id}
                  className="bg-slate-800/50 border border-slate-700/60 p-4 rounded-xl space-y-3"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <span
                          className={`text-[10px] px-2 py-0.5 rounded-full font-semibold border ${
                            c.status === 'CONFIRMED'
                              ? 'bg-emerald-950 text-emerald-400 border-emerald-800'
                              : c.status === 'ACCEPTED'
                              ? 'bg-sky-950 text-sky-400 border-sky-800'
                              : c.status === 'PENDING'
                              ? 'bg-amber-950 text-amber-400 border-amber-800'
                              : 'bg-slate-800 text-slate-400 border-slate-700'
                          }`}
                        >
                          {c.status}
                        </span>
                        <span className="text-xs text-slate-400">
                          {c.initiatedBy === 'GROUP' ? 'Inquiry from Group' : 'Pitched by You'}
                        </span>
                      </div>
                      <h4 className="text-base font-bold text-white">{c.groupName}</h4>
                      <span className="text-xs text-emerald-400 font-medium">📍 {c.groupDestination}</span>
                    </div>

                    {c.status === 'PENDING' && c.initiatedBy === 'GROUP' && (
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handleUpdateStatus(c.id, 'ACCEPTED')}
                          disabled={actionLoading}
                          className="bg-emerald-600 hover:bg-emerald-500 text-white p-1.5 rounded-lg text-xs flex items-center gap-1 font-medium"
                        >
                          <Check className="w-4 h-4" />
                          <span>Accept</span>
                        </button>
                        <button
                          onClick={() => handleUpdateStatus(c.id, 'REJECTED')}
                          disabled={actionLoading}
                          className="bg-slate-800 hover:bg-rose-950/60 border border-slate-700 text-slate-400 hover:text-rose-400 p-1.5 rounded-lg text-xs"
                        >
                          <XCircle className="w-4 h-4" />
                        </button>
                      </div>
                    )}
                  </div>

                  {c.message && (
                    <div className="bg-slate-900/60 p-3 rounded-lg text-xs text-slate-300 border border-slate-800">
                      <p className="italic">"{c.message}"</p>
                    </div>
                  )}

                  <div className="flex items-center justify-between text-[11px] text-slate-500 pt-1">
                    <span>Target: {c.groupStartDate || 'TBD'} to {c.groupEndDate || 'TBD'}</span>
                    <span>Received: {new Date(c.createdAt).toLocaleDateString()}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-16 bg-slate-900/30 border border-dashed border-slate-800 rounded-xl">
              <Clock className="w-8 h-8 text-slate-600 mx-auto mb-2" />
              <p className="text-sm text-slate-400 font-medium">No active inquiries or pitches yet.</p>
              <p className="text-xs text-slate-500 mt-1">
                Browse companion groups and pitch departures directly to interested trekkers!
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
