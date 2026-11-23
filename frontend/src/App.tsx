import { useEffect, useState } from 'react';
import {
  Users,
  MapPin,
  CheckCircle2,
  AlertCircle,
  Mountain,
  User,
  LogOut,
  LogIn,
  Sparkles,
  Plus,
  Search,
  Calendar,
  Layers,
  Inbox,
  MessageSquare,
} from 'lucide-react';
import { checkHealth, apiClient, type ApiResponse, type HealthData } from './api/client';
import { useAuth } from './context/AuthContext';
import { AuthModal } from './components/AuthModal';
import { ProfileModal } from './components/ProfileModal';
import { CreateGroupModal } from './components/CreateGroupModal';
import { GroupDetailsModal } from './components/GroupDetailsModal';
import { TrekDetailsModal } from './components/TrekDetailsModal';
import { OrganizerInboxModal } from './components/OrganizerInboxModal';
import { ChatModal } from './components/ChatModal';
import { NotificationDropdown } from './components/NotificationDropdown';
import type { TripGroupSummary, TrekListingSummary } from './types/trek';

export function App() {
  const { user, isAuthenticated, logout, quickDemoLogin } = useAuth();
  const [health, setHealth] = useState<HealthData | null>(null);
  const [loadingHealth, setLoadingHealth] = useState<boolean>(true);

  // Tab State
  const [activeTab, setActiveTab] = useState<'GROUPS' | 'TREKS'>('GROUPS');
  const [searchDestination, setSearchDestination] = useState('');

  // Data states
  const [groups, setGroups] = useState<TripGroupSummary[]>([]);
  const [treks, setTreks] = useState<TrekListingSummary[]>([]);
  const [loadingData, setLoadingData] = useState<boolean>(false);

  // Modal States
  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [profileModalOpen, setProfileModalOpen] = useState(false);
  const [createGroupModalOpen, setCreateGroupModalOpen] = useState(false);
  const [organizerInboxOpen, setOrganizerInboxOpen] = useState(false);
  const [chatModalOpen, setChatModalOpen] = useState(false);
  const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null);
  const [selectedTrekId, setSelectedTrekId] = useState<string | null>(null);

  useEffect(() => {
    checkHealth()
      .then((res) => setHealth(res.data))
      .catch(console.error)
      .finally(() => setLoadingHealth(false));

    loadData();
  }, [searchDestination]);

  const loadData = async () => {
    setLoadingData(true);
    try {
      // Load Groups
      const groupRes = await apiClient.get<ApiResponse<TripGroupSummary[]>>('/groups', {
        params: { destination: searchDestination || undefined },
      });
      if (groupRes.data.success) {
        setGroups(groupRes.data.data);
      }

      // Load Treks
      const trekRes = await apiClient.get<ApiResponse<TrekListingSummary[]>>('/treks', {
        params: { destination: searchDestination || undefined },
      });
      if (trekRes.data.success) {
        setTreks(trekRes.data.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingData(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      {/* Header / Navbar */}
      <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Mountain className="w-8 h-8 text-emerald-400" />
            <span className="font-bold text-xl tracking-tight text-white">The Great Trek Lores</span>
            <span className="text-[10px] bg-emerald-950 text-emerald-400 border border-emerald-800 px-2 py-0.5 rounded-full font-medium">
              Demo MVP
            </span>
          </div>

          <div className="flex items-center gap-3">
            {/* Backend health status badge */}
            {loadingHealth ? (
              <span className="text-xs text-slate-400 animate-pulse hidden sm:inline">Connecting...</span>
            ) : health?.status === 'UP' ? (
              <div className="hidden sm:flex items-center gap-1.5 text-xs text-emerald-400 bg-emerald-950/60 border border-emerald-800/80 px-2.5 py-1 rounded-full">
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Backend Online ({health.profile})</span>
              </div>
            ) : (
              <div className="hidden sm:flex items-center gap-1.5 text-xs text-amber-400 bg-amber-950/60 border border-amber-800/80 px-2.5 py-1 rounded-full">
                <AlertCircle className="w-3.5 h-3.5" />
                <span>Backend Offline</span>
              </div>
            )}

            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noreferrer"
              className="text-xs text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 px-3 py-1.5 rounded-md transition hidden md:inline"
            >
              Swagger Docs ↗
            </a>

            {/* Auth Buttons */}
            {isAuthenticated && user ? (
              <div className="flex items-center gap-2">
                <NotificationDropdown />
                <button
                  onClick={() => setChatModalOpen(true)}
                  className="flex items-center gap-1.5 text-xs bg-slate-800 hover:bg-slate-700 border border-slate-700 text-emerald-400 px-3 py-1.5 rounded-lg transition"
                >
                  <MessageSquare className="w-3.5 h-3.5" />
                  <span>Chats</span>
                </button>
                {user.role === 'ORGANIZER' && (
                  <button
                    onClick={() => setOrganizerInboxOpen(true)}
                    className="flex items-center gap-1.5 text-xs bg-slate-800 hover:bg-slate-700 border border-slate-700 text-sky-400 px-3 py-1.5 rounded-lg transition"
                  >
                    <Inbox className="w-3.5 h-3.5" />
                    <span>Inquiries</span>
                  </button>
                )}
                <button
                  onClick={() => setProfileModalOpen(true)}
                  className="flex items-center gap-1.5 text-xs bg-slate-800 hover:bg-slate-700 border border-slate-700 text-white px-3 py-1.5 rounded-lg transition"
                >
                  <User className="w-3.5 h-3.5 text-emerald-400" />
                  <span>{user.displayName || user.email}</span>
                  <span className="text-[10px] bg-emerald-950 text-emerald-400 border border-emerald-800 px-1.5 py-0.2 rounded font-semibold ml-1">
                    {user.role}
                  </span>
                </button>
                <button
                  onClick={logout}
                  className="text-slate-400 hover:text-rose-400 p-1.5 rounded-lg hover:bg-slate-800 transition"
                  title="Logout"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <button
                onClick={() => setAuthModalOpen(true)}
                className="text-xs bg-emerald-600 hover:bg-emerald-500 text-white font-medium px-3.5 py-1.5 rounded-lg transition flex items-center gap-1.5 shadow-md shadow-emerald-950"
              >
                <LogIn className="w-3.5 h-3.5" />
                <span>Sign In / Register</span>
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 w-full space-y-8">
        {/* Recruiter 1-Click Banner */}
        {!isAuthenticated && (
          <div className="p-4 bg-emerald-950/20 border border-emerald-900/40 rounded-2xl flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center gap-2 text-xs text-emerald-300">
              <Sparkles className="w-4 h-4 text-emerald-400 shrink-0" />
              <span>
                <strong>Instant Recruiter Mode:</strong> Test companion creation as a Trekker, or departure scheduling as an Organizer with one click.
              </span>
            </div>
            <div className="flex items-center gap-2">
              <button
                onClick={() => quickDemoLogin('USER')}
                className="text-xs bg-slate-800 hover:bg-emerald-800/40 border border-slate-700 text-slate-200 px-3 py-1.5 rounded-lg transition"
              >
                🧑 Demo Trekker (Alex)
              </button>
              <button
                onClick={() => quickDemoLogin('ORGANIZER')}
                className="text-xs bg-slate-800 hover:bg-emerald-800/40 border border-slate-700 text-slate-200 px-3 py-1.5 rounded-lg transition"
              >
                🏕️ Demo Organizer (Mountain Trails)
              </button>
            </div>
          </div>
        )}

        {/* Discovery Feed Header & Controls */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          {/* Tabs */}
          <div className="flex bg-slate-900 p-1 rounded-xl border border-slate-800">
            <button
              onClick={() => setActiveTab('GROUPS')}
              className={`flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-lg transition ${
                activeTab === 'GROUPS'
                  ? 'bg-emerald-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Users className="w-3.5 h-3.5" />
              <span>Companion Groups ({groups.length})</span>
            </button>
            <button
              onClick={() => setActiveTab('TREKS')}
              className={`flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-lg transition ${
                activeTab === 'TREKS'
                  ? 'bg-emerald-600 text-white shadow-sm'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              <Layers className="w-3.5 h-3.5" />
              <span>Trek Packages ({treks.length})</span>
            </button>
          </div>

          {/* Search & Actions */}
          <div className="flex items-center gap-3 w-full sm:w-auto">
            <div className="relative flex-1 sm:w-64">
              <Search className="w-4 h-4 text-slate-500 absolute left-3 top-2.5" />
              <input
                type="text"
                placeholder="Filter destination (e.g. Kedarkantha)..."
                value={searchDestination}
                onChange={(e) => setSearchDestination(e.target.value)}
                className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-9 pr-3 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-emerald-500"
              />
            </div>

            {activeTab === 'GROUPS' && (
              <button
                onClick={() => {
                  if (!isAuthenticated) {
                    setAuthModalOpen(true);
                  } else {
                    setCreateGroupModalOpen(true);
                  }
                }}
                className="flex items-center gap-1.5 text-xs bg-emerald-600 hover:bg-emerald-500 text-white font-medium px-3.5 py-2 rounded-xl transition shrink-0 shadow-md shadow-emerald-950"
              >
                <Plus className="w-4 h-4" />
                <span>Create Group</span>
              </button>
            )}
          </div>
        </div>

        {/* Content Grids */}
        {loadingData ? (
          <div className="text-center py-20 text-slate-500">Loading discovery feed...</div>
        ) : activeTab === 'GROUPS' ? (
          /* Companion Groups Grid */
          groups.length > 0 ? (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {groups.map((group) => (
                <div
                  key={group.id}
                  onClick={() => setSelectedGroupId(group.id)}
                  className="bg-slate-900/60 border border-slate-800 hover:border-emerald-500/70 rounded-2xl p-6 transition-all duration-200 cursor-pointer flex flex-col justify-between group shadow-sm hover:shadow-xl hover:shadow-emerald-950/20"
                >
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] bg-emerald-950 text-emerald-400 border border-emerald-800/80 px-2 py-0.5 rounded-full font-semibold">
                        {group.status}
                      </span>
                      <span className="text-xs text-slate-400 flex items-center gap-1">
                        <Users className="w-3.5 h-3.5 text-emerald-400" />
                        {group.currentMemberCount} / {group.maxMembers}
                      </span>
                    </div>

                    <h3 className="text-lg font-bold text-white group-hover:text-emerald-300 transition line-clamp-1">
                      {group.name}
                    </h3>

                    <div className="space-y-1.5 text-xs text-slate-400">
                      <div className="flex items-center gap-1.5">
                        <MapPin className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                        <span className="font-medium text-slate-300">{group.destination}</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Calendar className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                        <span>
                          {group.preferredStartDate || 'TBD'} to {group.preferredEndDate || 'TBD'}
                          {group.datesFlexible && ' (Flexible)'}
                        </span>
                      </div>
                    </div>

                    {group.description && (
                      <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                        {group.description}
                      </p>
                    )}
                  </div>

                  <div className="border-t border-slate-800/80 pt-4 mt-4 flex items-center justify-between text-xs text-slate-400">
                    <span>By {group.createdByName}</span>
                    <span className="text-emerald-400 font-medium group-hover:underline">
                      View Group →
                    </span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-20 bg-slate-900/30 border border-dashed border-slate-800 rounded-2xl">
              <Users className="w-10 h-10 text-slate-600 mx-auto mb-3" />
              <h4 className="text-base font-semibold text-white">No companion groups found</h4>
              <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
                Be the first to create a trip group for this destination and find fellow solo trekkers!
              </p>
            </div>
          )
        ) : (
          /* Trek Listings Grid */
          treks.length > 0 ? (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {treks.map((trek) => (
                <div
                  key={trek.id}
                  onClick={() => setSelectedTrekId(trek.id)}
                  className="bg-slate-900/60 border border-slate-800 hover:border-emerald-500/70 rounded-2xl overflow-hidden transition-all duration-200 cursor-pointer flex flex-col justify-between group shadow-sm hover:shadow-xl hover:shadow-emerald-950/20"
                >
                  <div className="p-6 space-y-3">
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] bg-emerald-950 text-emerald-400 border border-emerald-800/80 px-2 py-0.5 rounded-full font-semibold">
                        {trek.difficulty}
                      </span>
                      <span className="text-xs text-slate-400">
                        {trek.durationDays} Days Itinerary
                      </span>
                    </div>

                    <h3 className="text-lg font-bold text-white group-hover:text-emerald-300 transition">
                      {trek.title}
                    </h3>

                    <div className="flex items-center gap-1.5 text-xs text-slate-400">
                      <MapPin className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                      <span className="font-medium text-slate-300">{trek.destination}</span>
                    </div>

                    <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                      {trek.description || 'Verified package offered with certified mountain guides.'}
                    </p>
                  </div>

                  <div className="p-4 bg-slate-950/60 border-t border-slate-800 flex items-center justify-between">
                    <div>
                      <span className="block text-[10px] text-slate-400">Starting Price</span>
                      <span className="text-sm font-extrabold text-white">
                        ₹{trek.startingPrice?.toLocaleString() || 'N/A'}
                      </span>
                    </div>
                    <span className="text-xs text-emerald-400 font-medium group-hover:underline">
                      {trek.upcomingDepartureCount} Departures →
                    </span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-20 bg-slate-900/30 border border-dashed border-slate-800 rounded-2xl">
              <Layers className="w-10 h-10 text-slate-600 mx-auto mb-3" />
              <h4 className="text-base font-semibold text-white">No trek packages found</h4>
              <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
                Organizers will publish departure batches here soon.
              </p>
            </div>
          )
        )}
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-900 bg-slate-950 py-6 text-center text-xs text-slate-500">
        The Great Trek Lores • Built for Zero-Cost Permanent Portfolio Hosting
      </footer>

      {/* Modals */}
      <AuthModal isOpen={authModalOpen} onClose={() => setAuthModalOpen(false)} />
      <ProfileModal isOpen={profileModalOpen} onClose={() => setProfileModalOpen(false)} />
      <CreateGroupModal
        isOpen={createGroupModalOpen}
        onClose={() => setCreateGroupModalOpen(false)}
        onSuccess={loadData}
      />
      <GroupDetailsModal
        groupId={selectedGroupId}
        onClose={() => setSelectedGroupId(null)}
        onRefresh={loadData}
        onOpenChat={() => setChatModalOpen(true)}
      />
      <TrekDetailsModal
        trekId={selectedTrekId}
        onClose={() => setSelectedTrekId(null)}
      />
      <OrganizerInboxModal
        isOpen={organizerInboxOpen}
        onClose={() => setOrganizerInboxOpen(false)}
      />
      <ChatModal
        isOpen={chatModalOpen}
        onClose={() => setChatModalOpen(false)}
      />
    </div>
  );
}

export default App;
