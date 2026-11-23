import React, { useEffect, useState } from 'react';
import { X, Users, Calendar, MapPin, Check, XCircle, Clock, Sparkles, Send, ShieldCheck, MessageSquare } from 'lucide-react';
import { apiClient, type ApiResponse } from '../api/client';
import { useAuth } from '../context/AuthContext';
import type { TripGroupDetail, TrekDeparture, MembershipStatus } from '../types/trek';
import type { ConnectionDto, ConnectionStatus } from '../types/connection';

interface GroupDetailsModalProps {
  groupId: string | null;
  onClose: () => void;
  onRefresh: () => void;
  onOpenChat?: (roomId?: string) => void;
}

export const GroupDetailsModal: React.FC<GroupDetailsModalProps> = ({ groupId, onClose, onRefresh, onOpenChat }) => {
  const { user } = useAuth();
  const [group, setGroup] = useState<TripGroupDetail | null>(null);
  const [matchingDepartures, setMatchingDepartures] = useState<TrekDeparture[]>([]);
  const [connections, setConnections] = useState<ConnectionDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<'MEMBERS' | 'MATCHES' | 'CONNECTIONS'>('MEMBERS');
  const [enquirySuccess, setEnquirySuccess] = useState<string | null>(null);

  useEffect(() => {
    if (groupId) {
      loadGroup();
    }
  }, [groupId]);

  const loadGroup = async () => {
    if (!groupId) return;
    setLoading(true);
    try {
      const res = await apiClient.get<ApiResponse<TripGroupDetail>>(`/groups/${groupId}`);
      if (res.data.success) {
        setGroup(res.data.data);
      }

      // Check for matching organizer departures
      const matchRes = await apiClient.get<ApiResponse<TrekDeparture[]>>(`/treks/matching-departures?groupId=${groupId}`);
      if (matchRes.data.success) {
        setMatchingDepartures(matchRes.data.data);
      }

      // Check for existing connections if member
      try {
        const connRes = await apiClient.get<ApiResponse<ConnectionDto[]>>(`/connections/groups/${groupId}`);
        if (connRes.data.success) {
          setConnections(connRes.data.data);
        }
      } catch {
        // Not a member or not authorized to see connections yet
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (!groupId || !group) return null;

  const handleJoinRequest = async () => {
    setActionLoading(true);
    try {
      await apiClient.post(`/groups/${groupId}/join-requests`);
      loadGroup();
      onRefresh();
    } catch (err) {
      console.error(err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleManageMember = async (membershipId: string, status: MembershipStatus) => {
    setActionLoading(true);
    try {
      await apiClient.patch(`/groups/${groupId}/memberships/${membershipId}`, { status });
      loadGroup();
      onRefresh();
    } catch (err) {
      console.error(err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleEnquireWithOrganizer = async (organizerId: string) => {
    setActionLoading(true);
    try {
      await apiClient.post(`/connections/groups/${groupId}/enquire/${organizerId}`, {
        message: `Hello! Our group of ${group.members.filter((m) => m.status === 'ACCEPTED').length} trekkers would like to enquire about your departure plans.`,
      });
      setEnquirySuccess(organizerId);
      setTimeout(() => setEnquirySuccess(null), 3000);
      loadGroup();
      setActiveTab('CONNECTIONS');
    } catch (err) {
      console.error(err);
    } finally {
      setActionLoading(false);
    }
  };

  const handleUpdateConnectionStatus = async (connectionId: string, status: ConnectionStatus) => {
    setActionLoading(true);
    try {
      await apiClient.patch(`/connections/${connectionId}/status`, { status });
      loadGroup();
    } catch (err) {
      console.error(err);
    } finally {
      setActionLoading(false);
    }
  };

  const acceptedMembers = group.members.filter((m) => m.status === 'ACCEPTED');
  const pendingMembers = group.members.filter((m) => m.status === 'PENDING');

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden shadow-2xl">
        {/* Header */}
        <div className="p-6 border-b border-slate-800 bg-slate-900/60">
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs bg-emerald-950 text-emerald-400 border border-emerald-800 px-2 py-0.5 rounded-full font-semibold">
                  {group.status}
                </span>
                <span className="text-xs text-slate-400">Created by {group.createdByName}</span>
              </div>
              <h2 className="text-2xl font-bold text-white">{group.name}</h2>
              <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400 mt-2">
                <span className="flex items-center gap-1">
                  <MapPin className="w-3.5 h-3.5 text-emerald-400" />
                  {group.destination}
                </span>
                <span className="flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5 text-emerald-400" />
                  {group.preferredStartDate || 'TBD'} to {group.preferredEndDate || 'TBD'}
                  {group.datesFlexible && ' (Flexible)'}
                </span>
                <span className="flex items-center gap-1">
                  <Users className="w-3.5 h-3.5 text-emerald-400" />
                  {acceptedMembers.length} / {group.maxMembers} Members
                </span>
              </div>
            </div>
            <button onClick={onClose} className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800">
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Tab Header */}
        <div className="flex border-b border-slate-800 px-6 bg-slate-950/40">
          <button
            onClick={() => setActiveTab('MEMBERS')}
            className={`py-3 px-4 text-xs font-semibold border-b-2 transition ${
              activeTab === 'MEMBERS'
                ? 'border-emerald-400 text-emerald-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            Companions ({acceptedMembers.length})
            {group.isCurrentUserAdmin && pendingMembers.length > 0 && (
              <span className="ml-1.5 bg-amber-500/20 text-amber-400 border border-amber-500/40 px-1.5 py-0.2 rounded-full text-[10px]">
                {pendingMembers.length} pending
              </span>
            )}
          </button>
          <button
            onClick={() => setActiveTab('MATCHES')}
            className={`py-3 px-4 text-xs font-semibold border-b-2 transition flex items-center gap-1.5 ${
              activeTab === 'MATCHES'
                ? 'border-emerald-400 text-emerald-400'
                : 'border-transparent text-slate-400 hover:text-slate-200'
            }`}
          >
            <Sparkles className="w-3.5 h-3.5 text-emerald-400" />
            <span>Matching Departures ({matchingDepartures.length})</span>
          </button>
          {connections.length > 0 && (
            <button
              onClick={() => setActiveTab('CONNECTIONS')}
              className={`py-3 px-4 text-xs font-semibold border-b-2 transition flex items-center gap-1.5 ${
                activeTab === 'CONNECTIONS'
                  ? 'border-emerald-400 text-emerald-400'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              <span>Connections ({connections.length})</span>
            </button>
          )}
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {loading ? (
            <div className="text-center py-12 text-slate-400">Loading details...</div>
          ) : activeTab === 'MEMBERS' ? (
            <>
              {/* Description */}
              {group.description && (
                <div className="bg-slate-800/40 border border-slate-800 rounded-xl p-4 text-sm text-slate-300">
                  <p className="whitespace-pre-line leading-relaxed">{group.description}</p>
                </div>
              )}

              {/* Pending Join Requests (Visible to Admin only) */}
              {group.isCurrentUserAdmin && pendingMembers.length > 0 && (
                <div className="space-y-2">
                  <h4 className="text-xs font-semibold text-amber-400 uppercase tracking-wider flex items-center gap-1">
                    <Clock className="w-3.5 h-3.5" />
                    Pending Join Requests
                  </h4>
                  <div className="space-y-2">
                    {pendingMembers.map((m) => (
                      <div
                        key={m.membershipId}
                        className="flex items-center justify-between bg-amber-950/20 border border-amber-900/40 p-3 rounded-xl"
                      >
                        <div>
                          <span className="font-semibold text-white text-sm">{m.displayName}</span>
                          <span className="text-xs text-slate-400 ml-2">({m.experienceLevel})</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => handleManageMember(m.membershipId, 'ACCEPTED')}
                            disabled={actionLoading}
                            className="bg-emerald-600 hover:bg-emerald-500 text-white p-1.5 rounded-lg text-xs flex items-center gap-1"
                          >
                            <Check className="w-4 h-4" />
                            <span>Accept</span>
                          </button>
                          <button
                            onClick={() => handleManageMember(m.membershipId, 'REJECTED')}
                            disabled={actionLoading}
                            className="bg-slate-800 hover:bg-rose-950/60 border border-slate-700 text-slate-400 hover:text-rose-400 p-1.5 rounded-lg text-xs"
                          >
                            <XCircle className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Accepted Member Roster */}
              <div className="space-y-3">
                <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  Confirmed Trekkers
                </h4>
                <div className="grid sm:grid-cols-2 gap-3">
                  {acceptedMembers.map((m) => (
                    <div
                      key={m.membershipId}
                      className="flex items-center gap-3 bg-slate-800/60 border border-slate-700/60 p-3 rounded-xl"
                    >
                      <div className="w-9 h-9 rounded-full bg-emerald-950 border border-emerald-800 flex items-center justify-center font-bold text-emerald-400 text-sm">
                        {m.displayName.charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <div className="flex items-center gap-1.5">
                          <span className="font-medium text-white text-sm">{m.displayName}</span>
                          {m.role === 'ADMIN' && (
                            <span className="text-[10px] bg-emerald-950 text-emerald-400 border border-emerald-800 px-1.5 py-0.2 rounded font-semibold">
                              Admin
                            </span>
                          )}
                        </div>
                        <span className="text-xs text-slate-400">{m.experienceLevel}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </>
          ) : activeTab === 'MATCHES' ? (
            /* Matching Organizer Departures */
            <div className="space-y-3">
              <p className="text-xs text-slate-400">
                The platform detected these scheduled departures from verified organizers whose dates overlap with your target window:
              </p>
              {matchingDepartures.length > 0 ? (
                <div className="space-y-3">
                  {matchingDepartures.map((d) => (
                    <div
                      key={d.id}
                      className="bg-slate-800/60 border border-emerald-900/50 hover:border-emerald-500/80 p-4 rounded-xl transition flex flex-col sm:flex-row sm:items-center justify-between gap-3"
                    >
                      <div className="space-y-1">
                        <span className="text-xs text-emerald-400 font-semibold">{d.organizerName}</span>
                        <h4 className="text-sm font-bold text-white">{d.trekTitle}</h4>
                        <div className="flex items-center gap-3 text-xs text-slate-400">
                          <span>📅 {d.startDate} to {d.endDate}</span>
                          <span>👥 {d.availableSlots} slots left</span>
                        </div>
                      </div>
                      <div className="text-right flex sm:flex-col items-center sm:items-end justify-between sm:justify-center gap-2">
                        <div>
                          <span className="text-base font-extrabold text-white">₹{d.price.toLocaleString()}</span>
                          <span className="text-[10px] text-slate-400 ml-1">/ person</span>
                        </div>
                        {group.isCurrentUserAdmin && (
                          <button
                            onClick={() => handleEnquireWithOrganizer(d.trekListingId)}
                            disabled={actionLoading || enquirySuccess === d.trekListingId}
                            className="bg-emerald-600 hover:bg-emerald-500 text-white px-3 py-1.5 rounded-lg text-xs font-medium transition flex items-center gap-1.5 disabled:opacity-50"
                          >
                            <Send className="w-3.5 h-3.5" />
                            <span>{enquirySuccess === d.trekListingId ? 'Sent!' : 'Enquire'}</span>
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-10 bg-slate-900/40 border border-dashed border-slate-800 rounded-xl">
                  <p className="text-sm text-slate-400">No organizer departures currently overlap with these exact dates.</p>
                </div>
              )}
            </div>
          ) : (
            /* Connections Tab */
            <div className="space-y-3">
              <p className="text-xs text-slate-400">
                Negotiations and connections between your companion group and interested trekking organizers:
              </p>
              <div className="space-y-3">
                {connections.map((c) => (
                  <div
                    key={c.id}
                    className="bg-slate-800/60 border border-slate-700/60 p-4 rounded-xl space-y-3"
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
                                : 'bg-amber-950 text-amber-400 border-amber-800'
                            }`}
                          >
                            {c.status}
                          </span>
                          <span className="text-xs text-slate-400">
                            {c.initiatedBy === 'GROUP' ? 'Enquiry sent by Group' : 'Pitch from Organizer'}
                          </span>
                        </div>
                        <h4 className="text-base font-bold text-white">{c.organizerName}</h4>
                      </div>

                      {group.isCurrentUserAdmin && (
                        <div className="flex items-center gap-2">
                          {c.status === 'PENDING' && c.initiatedBy === 'ORGANIZER' && (
                            <>
                              <button
                                onClick={() => handleUpdateConnectionStatus(c.id, 'ACCEPTED')}
                                disabled={actionLoading}
                                className="bg-emerald-600 hover:bg-emerald-500 text-white px-2.5 py-1 rounded-lg text-xs font-medium"
                              >
                                Accept Pitch
                              </button>
                              <button
                                onClick={() => handleUpdateConnectionStatus(c.id, 'REJECTED')}
                                disabled={actionLoading}
                                className="bg-slate-800 hover:bg-rose-950/60 text-slate-400 hover:text-rose-400 px-2 py-1 rounded-lg text-xs"
                              >
                                Reject
                              </button>
                            </>
                          )}
                          {c.status === 'ACCEPTED' && (
                            <button
                              onClick={() => handleUpdateConnectionStatus(c.id, 'CONFIRMED')}
                              disabled={actionLoading}
                              className="bg-emerald-600 hover:bg-emerald-500 text-white px-3 py-1 rounded-lg text-xs font-semibold flex items-center gap-1 shadow-md shadow-emerald-950"
                            >
                              <ShieldCheck className="w-3.5 h-3.5" />
                              <span>Confirm Organizer</span>
                            </button>
                          )}
                        </div>
                      )}
                    </div>

                    {c.message && (
                      <p className="text-xs text-slate-300 italic bg-slate-900/60 p-2.5 rounded-lg border border-slate-800">
                        "{c.message}"
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Footer Actions */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/60 flex items-center justify-between">
          <span className="text-xs text-slate-400">
            {group.isCurrentUserAdmin
              ? 'You are the Admin of this group'
              : group.currentUserMembershipStatus === 'ACCEPTED'
              ? 'You are a companion in this group'
              : group.currentUserMembershipStatus === 'PENDING'
              ? 'Join request pending approval'
              : 'Looking for a companion group?'}
          </span>

          {!group.isCurrentUserAdmin && !group.currentUserMembershipStatus && user && (
            <button
              onClick={handleJoinRequest}
              disabled={actionLoading || acceptedMembers.length >= group.maxMembers}
              className="py-2 px-4 bg-emerald-600 hover:bg-emerald-500 text-white font-medium rounded-lg text-xs transition flex items-center gap-1.5 shadow-md shadow-emerald-950 disabled:opacity-50"
            >
              <Users className="w-4 h-4" />
              <span>Request to Join Group</span>
            </button>
          )}

          {group.currentUserMembershipStatus === 'ACCEPTED' && (
            <button
              onClick={() => {
                onClose();
                onOpenChat?.();
              }}
              className="py-2 px-4 bg-emerald-600 hover:bg-emerald-500 text-white font-medium rounded-lg text-xs transition flex items-center gap-1.5 shadow-md shadow-emerald-950"
            >
              <MessageSquare className="w-4 h-4" />
              <span>Open Group Chat</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
