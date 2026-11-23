import React, { useState } from 'react';
import { X, ShieldAlert, CheckCircle2, AlertCircle } from 'lucide-react';
import { apiClient } from '../api/client';

interface ReportModalProps {
  isOpen: boolean;
  onClose: () => void;
  targetType: 'USER' | 'GROUP' | 'ORGANIZER' | 'MESSAGE';
  targetId: string;
  targetTitle?: string;
}

export const ReportModal: React.FC<ReportModalProps> = ({
  isOpen,
  onClose,
  targetType,
  targetId,
  targetTitle,
}) => {
  const [reason, setReason] = useState('Inappropriate Content / Harassment');
  const [details, setDetails] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      await apiClient.post('/reports', {
        targetType,
        targetId,
        reason,
        details,
      });
      setSuccess(true);
      setTimeout(() => {
        setSuccess(false);
        onClose();
      }, 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit report');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-md overflow-hidden shadow-2xl">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-900/60">
          <div className="flex items-center gap-2">
            <ShieldAlert className="w-5 h-5 text-rose-400" />
            <h3 className="font-semibold text-lg text-white">Report Content or User</h3>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800">
            <X className="w-5 h-5" />
          </button>
        </div>

        {success ? (
          <div className="p-8 text-center space-y-3">
            <CheckCircle2 className="w-12 h-12 text-emerald-400 mx-auto" />
            <h4 className="font-bold text-white text-base">Report Submitted</h4>
            <p className="text-xs text-slate-400 max-w-xs mx-auto">
              Thank you for keeping our trekking community safe. A platform administrator will review this report.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="p-6 space-y-4">
            {error && (
              <div className="p-3 rounded-lg bg-rose-950/50 border border-rose-800/50 text-rose-300 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            {targetTitle && (
              <div className="text-xs text-slate-400">
                Reporting: <span className="font-semibold text-white">{targetTitle}</span> ({targetType})
              </div>
            )}

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1">Reason for report</label>
              <select
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:border-rose-500"
              >
                <option value="Inappropriate Content / Harassment">Inappropriate Content / Harassment</option>
                <option value="Spam / Unsolicited Promotion">Spam / Unsolicited Promotion</option>
                <option value="Misleading Dates / Trek Information">Misleading Dates / Trek Information</option>
                <option value="Fraudulent Organizer Profile">Fraudulent Organizer Profile</option>
                <option value="Other">Other</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1">Additional details (Optional)</label>
              <textarea
                rows={3}
                placeholder="Describe what occurred or paste relevant details..."
                value={details}
                onChange={(e) => setDetails(e.target.value)}
                className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-rose-500"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 bg-rose-600 hover:bg-rose-500 text-white font-medium rounded-lg text-xs transition flex items-center justify-center gap-1.5 shadow-md shadow-rose-950 disabled:opacity-50"
            >
              {loading ? 'Submitting...' : 'Submit Report for Moderation'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};
