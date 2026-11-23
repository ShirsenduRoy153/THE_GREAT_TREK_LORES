import React, { useEffect, useState } from 'react';
import { X, User, Plus, Trash2, Save, Check } from 'lucide-react';
import { apiClient, type ApiResponse } from '../api/client';
import { useAuth } from '../context/AuthContext';
import type { UserProfileData, ExperienceLevel, SocialPlatform, VisibilityLevel } from '../types/auth';

interface ProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ProfileModal: React.FC<ProfileModalProps> = ({ isOpen, onClose }) => {
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserProfileData | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);

  // Form states
  const [displayName, setDisplayName] = useState('');
  const [location, setLocation] = useState('');
  const [occupation, setOccupation] = useState('');
  const [bio, setBio] = useState('');
  const [experienceLevel, setExperienceLevel] = useState<ExperienceLevel>('BEGINNER');

  // New social link state
  const [newPlatform, setNewPlatform] = useState<SocialPlatform>('INSTAGRAM');
  const [newUrl, setNewUrl] = useState('');
  const [newVisibility, setNewVisibility] = useState<VisibilityLevel>('PUBLIC');

  // New trek history state
  const [newTrekName, setNewTrekName] = useState('');
  const [newTrekLocation, setNewTrekLocation] = useState('');
  const [newStartDate, setNewStartDate] = useState('');
  const [newEndDate, setNewEndDate] = useState('');

  useEffect(() => {
    if (isOpen && user) {
      loadProfile();
    }
  }, [isOpen, user]);

  const loadProfile = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get<ApiResponse<UserProfileData>>('/users/me/profile');
      if (res.data.success) {
        const data = res.data.data;
        setProfile(data);
        setDisplayName(data.displayName || '');
        setLocation(data.location || '');
        setOccupation(data.occupation || '');
        setBio(data.bio || '');
        setExperienceLevel(data.trekkingExperienceLevel || 'BEGINNER');
      }
    } catch (err) {
      console.error('Error loading profile', err);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await apiClient.put('/users/me/profile', {
        displayName,
        location,
        occupation,
        bio,
        trekkingExperienceLevel: experienceLevel,
      });
      setSavedSuccess(true);
      setTimeout(() => setSavedSuccess(false), 2500);
      loadProfile();
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  const handleAddSocialLink = async () => {
    if (!newUrl) return;
    try {
      await apiClient.post('/users/me/social-links', {
        platform: newPlatform,
        url: newUrl,
        visibility: newVisibility,
      });
      setNewUrl('');
      loadProfile();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDeleteSocialLink = async (id?: string) => {
    if (!id) return;
    try {
      await apiClient.delete(`/users/me/social-links/${id}`);
      loadProfile();
    } catch (err) {
      console.error(err);
    }
  };

  const handleAddTrek = async () => {
    if (!newTrekName) return;
    try {
      await apiClient.post('/users/me/trek-history', {
        trekName: newTrekName,
        location: newTrekLocation,
        startDate: newStartDate || null,
        endDate: newEndDate || null,
      });
      setNewTrekName('');
      setNewTrekLocation('');
      setNewStartDate('');
      setNewEndDate('');
      loadProfile();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800">
          <div className="flex items-center gap-2">
            <User className="w-5 h-5 text-emerald-400" />
            <h3 className="font-semibold text-lg text-white">Your Trekker Profile & Privacy</h3>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {loading ? (
            <div className="text-center py-12 text-slate-400">Loading profile...</div>
          ) : (
            <>
              {/* Profile Bio & Details */}
              <form onSubmit={handleUpdateProfile} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-medium text-slate-400 mb-1">Display Name</label>
                    <input
                      type="text"
                      value={displayName}
                      onChange={(e) => setDisplayName(e.target.value)}
                      className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-emerald-500"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-slate-400 mb-1">Experience Level</label>
                    <select
                      value={experienceLevel}
                      onChange={(e) => setExperienceLevel(e.target.value as ExperienceLevel)}
                      className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-emerald-500"
                    >
                      <option value="BEGINNER">Beginner (1-2 easy treks)</option>
                      <option value="INTERMEDIATE">Intermediate (Moderate altitudes)</option>
                      <option value="ADVANCED">Advanced (High pass / Snow)</option>
                      <option value="EXPERT">Expert (Mountaineer / Technical)</option>
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-medium text-slate-400 mb-1">Location</label>
                    <input
                      type="text"
                      placeholder="e.g. Dehradun, India"
                      value={location}
                      onChange={(e) => setLocation(e.target.value)}
                      className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-emerald-500"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-slate-400 mb-1">Occupation</label>
                    <input
                      type="text"
                      placeholder="e.g. Outdoor Photographer"
                      value={occupation}
                      onChange={(e) => setOccupation(e.target.value)}
                      className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-emerald-500"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-400 mb-1">Bio / Trekking Style</label>
                  <textarea
                    rows={2}
                    placeholder="Tell companion trekkers about your pace, gear, and travel preferences..."
                    value={bio}
                    onChange={(e) => setBio(e.target.value)}
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-emerald-500"
                  />
                </div>

                <div className="flex items-center justify-between pt-2">
                  <span className="text-xs text-slate-400">
                    Role: <span className="text-emerald-400 font-semibold">{profile?.role}</span>
                  </span>
                  <button
                    type="submit"
                    disabled={saving}
                    className="py-1.5 px-4 bg-emerald-600 hover:bg-emerald-500 text-white font-medium rounded-lg text-xs transition flex items-center gap-1.5"
                  >
                    {savedSuccess ? <Check className="w-3.5 h-3.5" /> : <Save className="w-3.5 h-3.5" />}
                    <span>{savedSuccess ? 'Saved!' : 'Save Profile Changes'}</span>
                  </button>
                </div>
              </form>

              {/* Social Links & Privacy Settings */}
              <div className="border-t border-slate-800 pt-5 space-y-3">
                <div className="flex items-center justify-between">
                  <h4 className="font-semibold text-sm text-white flex items-center gap-2">
                    <span>Social & Contact Links</span>
                    <span className="text-[10px] bg-slate-800 text-slate-400 px-2 py-0.5 rounded-full font-normal">
                      Field-Level Privacy
                    </span>
                  </h4>
                </div>

                {/* List existing links */}
                <div className="space-y-2">
                  {profile?.socialLinks && profile.socialLinks.length > 0 ? (
                    profile.socialLinks.map((link) => (
                      <div
                        key={link.id}
                        className="flex items-center justify-between bg-slate-800/60 border border-slate-700/60 px-3 py-2 rounded-lg text-xs"
                      >
                        <div className="flex items-center gap-2">
                          <span className="font-semibold text-emerald-400">{link.platform}:</span>
                          <span className="text-slate-300 truncate max-w-xs">{link.url}</span>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className="text-[10px] bg-slate-700/80 text-slate-300 px-2 py-0.5 rounded">
                            {link.visibility === 'PUBLIC'
                              ? '🌐 Public'
                              : link.visibility === 'GROUP_MEMBERS'
                              ? '👥 Companions Only'
                              : '🔒 Private'}
                          </span>
                          <button
                            onClick={() => handleDeleteSocialLink(link.id)}
                            className="text-slate-400 hover:text-rose-400 transition"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>
                    ))
                  ) : (
                    <p className="text-xs text-slate-500 italic">No social/contact links added yet.</p>
                  )}
                </div>

                {/* Add new link */}
                <div className="grid grid-cols-12 gap-2 pt-1">
                  <select
                    value={newPlatform}
                    onChange={(e) => setNewPlatform(e.target.value as SocialPlatform)}
                    className="col-span-3 bg-slate-800 border border-slate-700 rounded-lg px-2 py-1.5 text-xs text-white"
                  >
                    <option value="WHATSAPP">WhatsApp</option>
                    <option value="INSTAGRAM">Instagram</option>
                    <option value="LINKEDIN">LinkedIn</option>
                    <option value="OTHER">Other</option>
                  </select>
                  <input
                    type="text"
                    placeholder="URL or Phone number"
                    value={newUrl}
                    onChange={(e) => setNewUrl(e.target.value)}
                    className="col-span-5 bg-slate-800 border border-slate-700 rounded-lg px-2 py-1.5 text-xs text-white"
                  />
                  <select
                    value={newVisibility}
                    onChange={(e) => setNewVisibility(e.target.value as VisibilityLevel)}
                    className="col-span-3 bg-slate-800 border border-slate-700 rounded-lg px-2 py-1.5 text-xs text-white"
                  >
                    <option value="GROUP_MEMBERS">Companions Only</option>
                    <option value="PUBLIC">Public</option>
                    <option value="PRIVATE">Private</option>
                  </select>
                  <button
                    onClick={handleAddSocialLink}
                    className="col-span-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg flex items-center justify-center transition"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                </div>
              </div>

              {/* Trek History */}
              <div className="border-t border-slate-800 pt-5 space-y-3">
                <h4 className="font-semibold text-sm text-white">Completed Trek History</h4>
                <div className="space-y-2">
                  {profile?.trekHistory && profile.trekHistory.length > 0 ? (
                    profile.trekHistory.map((trek) => (
                      <div
                        key={trek.id}
                        className="bg-slate-800/60 border border-slate-700/60 px-3 py-2 rounded-lg text-xs flex justify-between"
                      >
                        <div>
                          <span className="font-medium text-white">{trek.trekName}</span>
                          {trek.location && <span className="text-slate-400"> • {trek.location}</span>}
                        </div>
                        {trek.startDate && (
                          <span className="text-slate-400 text-[10px]">{trek.startDate}</span>
                        )}
                      </div>
                    ))
                  ) : (
                    <p className="text-xs text-slate-500 italic">No past treks logged yet.</p>
                  )}
                </div>

                <div className="grid grid-cols-12 gap-2 pt-1">
                  <input
                    type="text"
                    placeholder="Trek name (e.g. Kedarkantha)"
                    value={newTrekName}
                    onChange={(e) => setNewTrekName(e.target.value)}
                    className="col-span-5 bg-slate-800 border border-slate-700 rounded-lg px-2 py-1.5 text-xs text-white"
                  />
                  <input
                    type="text"
                    placeholder="Location (e.g. Uttarakhand)"
                    value={newTrekLocation}
                    onChange={(e) => setNewTrekLocation(e.target.value)}
                    className="col-span-4 bg-slate-800 border border-slate-700 rounded-lg px-2 py-1.5 text-xs text-white"
                  />
                  <input
                    type="date"
                    value={newStartDate}
                    onChange={(e) => setNewStartDate(e.target.value)}
                    className="col-span-2 bg-slate-800 border border-slate-700 rounded-lg px-1 py-1.5 text-xs text-white"
                  />
                  <button
                    onClick={handleAddTrek}
                    className="col-span-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg flex items-center justify-center transition"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
