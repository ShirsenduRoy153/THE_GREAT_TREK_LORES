import React, { useEffect, useState } from 'react';
import { X, Calendar, MapPin } from 'lucide-react';
import { apiClient, type ApiResponse } from '../api/client';
import type { TrekListingDetail } from '../types/trek';

interface TrekDetailsModalProps {
  trekId: string | null;
  onClose: () => void;
}

export const TrekDetailsModal: React.FC<TrekDetailsModalProps> = ({ trekId, onClose }) => {
  const [trek, setTrek] = useState<TrekListingDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (trekId) {
      setLoading(true);
      apiClient
        .get<ApiResponse<TrekListingDetail>>(`/treks/${trekId}`)
        .then((res) => {
          if (res.data.success) {
            setTrek(res.data.data);
          }
        })
        .catch(console.error)
        .finally(() => setLoading(false));
    }
  }, [trekId]);

  if (!trekId || !trek) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden shadow-2xl">
        {/* Header */}
        <div className="p-6 border-b border-slate-800 bg-slate-900/60">
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs bg-emerald-950 text-emerald-400 border border-emerald-800 px-2 py-0.5 rounded-full font-semibold">
                  {trek.difficulty}
                </span>
                <span className="text-xs text-slate-400">By {trek.organizer.organizerName}</span>
              </div>
              <h2 className="text-2xl font-bold text-white">{trek.title}</h2>
              <div className="flex items-center gap-4 text-xs text-slate-400 mt-2">
                <span className="flex items-center gap-1">
                  <MapPin className="w-3.5 h-3.5 text-emerald-400" />
                  {trek.destination}
                </span>
                <span className="flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5 text-emerald-400" />
                  {trek.durationDays} Days Itinerary
                </span>
                <span className="flex items-center gap-1 text-emerald-400 font-semibold">
                  Starting at ₹{trek.startingPrice?.toLocaleString()}
                </span>
              </div>
            </div>
            <button onClick={onClose} className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800">
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {loading ? (
            <div className="text-center py-12 text-slate-400">Loading details...</div>
          ) : (
            <>
              {/* Description */}
              {trek.description && (
                <div className="bg-slate-800/40 border border-slate-800 rounded-xl p-4 text-sm text-slate-300">
                  <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Trek Overview & Route
                  </h4>
                  <p className="whitespace-pre-line leading-relaxed">{trek.description}</p>
                </div>
              )}

              {/* Scheduled Departure Batches */}
              <div className="space-y-3">
                <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                  Upcoming Scheduled Departures
                </h4>
                {trek.departures && trek.departures.length > 0 ? (
                  <div className="space-y-2.5">
                    {trek.departures.map((d) => (
                      <div
                        key={d.id}
                        className="bg-slate-800/60 border border-slate-700/60 p-4 rounded-xl flex items-center justify-between"
                      >
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <span className="text-sm font-semibold text-white">
                              {d.startDate} to {d.endDate}
                            </span>
                            <span className="text-[10px] bg-emerald-950 text-emerald-400 border border-emerald-800 px-1.5 py-0.2 rounded font-medium">
                              {d.departureStatus}
                            </span>
                          </div>
                          <span className="text-xs text-slate-400">
                            Available Capacity: {d.availableSlots} of {d.capacity} slots left
                          </span>
                        </div>
                        <div className="text-right">
                          <span className="text-lg font-bold text-white">₹{d.price.toLocaleString()}</span>
                          <span className="block text-[10px] text-slate-400">Per Person</span>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-slate-500 italic">No scheduled departures published yet.</p>
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
