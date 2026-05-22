const API_BASE_URL = 'http://localhost:8080';
const PAGE_LINKS = [
  { id: 'dashboard', label: 'Dashboard', href: 'dashboard.html', roles: ['USER', 'RIDER', 'BIKE_OWNER', 'ADMIN'] },
  { id: 'bikes', label: 'Browse Bikes', href: 'bikes.html', roles: ['RIDER', 'BIKE_OWNER', 'ADMIN'] },
  { id: 'add-bike', label: 'Add Bike', href: 'add-bike.html', roles: ['BIKE_OWNER', 'ADMIN'] },
  { id: 'users', label: 'Users', href: 'users.html', roles: ['ADMIN'] },
  { id: 'rentals', label: 'My Rentals', href: 'rentals.html', roles: ['RIDER', 'BIKE_OWNER', 'ADMIN'] },
  { id: 'payments', label: 'Payments', href: 'payments.html', roles: ['USER', 'RIDER', 'BIKE_OWNER', 'ADMIN'] },
  { id: 'ride-sharing', label: 'Ride Sharing', href: 'ride-sharing.html', roles: ['USER', 'RIDER', 'ADMIN'] },
  { id: 'notifications', label: 'Notifications', href: 'notifications.html', roles: ['RIDER', 'BIKE_OWNER', 'ADMIN'] },
  { id: 'profile', label: 'Profile', href: 'profile.html', roles: ['USER', 'RIDER', 'BIKE_OWNER', 'ADMIN'] }
];
let activeRequests = 0;
let unreadPoll = null;

function getToken() { return localStorage.getItem('accessToken'); }
function getUserRole() { return localStorage.getItem('role') || ''; }
function currentUserId() { return localStorage.getItem('userId'); }
function unwrap(payload) { return payload && Object.prototype.hasOwnProperty.call(payload, 'data') ? payload.data : payload; }
function listFrom(payload) { const data = unwrap(payload); return Array.isArray(data) ? data : (data?.content || data?.items || data?.records || []); }

async function apiFetch(path, options = {}) {
  const token = localStorage.getItem('accessToken');
  const hasBody = Object.prototype.hasOwnProperty.call(options, 'body');
  showLoading(true);
  try {
    const res = await fetch(API_BASE_URL + path, {
      ...options,
      headers: {
        ...(hasBody ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: 'Bearer ' + token } : {}),
        ...options.headers
      }
    });
    if (res.status === 401) {
      localStorage.clear();
      location.href = 'login.html';
      return Promise.reject(new Error('Session expired. Please login again.'));
    }
    const text = await res.text();
    let payload = { success: res.ok, data: null };
    if (text) {
      try {
        payload = JSON.parse(text);
      } catch (_) {
        payload = { success: res.ok, message: text };
      }
    }
    if (res.status === 403) throw new Error(payload.message || 'You do not have permission to do this action.');
    if (!res.ok || payload.success === false) throw new Error(payload.message || 'Request failed');
    return payload;
  } finally {
    showLoading(false);
  }
}

function showLoading(show) {
  let overlay = document.getElementById('loadingOverlay');
  if (!overlay) {
    overlay = document.createElement('div');
    overlay.id = 'loadingOverlay';
    overlay.className = 'fixed inset-0 z-[80] hidden place-items-center bg-black/50 backdrop-blur-sm';
    overlay.innerHTML = '<div class="h-12 w-12 animate-spin rounded-full border-4 border-[#2a2a2a] border-t-[#00d4aa]"></div>';
    document.body.appendChild(overlay);
  }
  activeRequests += show ? 1 : -1;
  activeRequests = Math.max(0, activeRequests);
  overlay.classList.toggle('hidden', activeRequests === 0);
  overlay.classList.toggle('grid', activeRequests > 0);
}

function showToast(message, type = 'success') {
  let wrap = document.getElementById('toastWrap');
  if (!wrap) {
    wrap = document.createElement('div');
    wrap.id = 'toastWrap';
    wrap.className = 'fixed bottom-5 right-5 z-[90] space-y-3';
    document.body.appendChild(wrap);
  }
  const toast = document.createElement('div');
  const color = type === 'error' ? 'border-red-500/60 bg-red-500/15 text-red-100' : 'border-emerald-400/60 bg-emerald-400/15 text-emerald-100';
  toast.className = `min-w-72 rounded-lg border px-4 py-3 text-sm shadow-xl ${color}`;
  toast.textContent = message;
  wrap.appendChild(toast);
  setTimeout(() => { toast.style.opacity = '0'; toast.style.transform = 'translateY(8px)'; }, 2600);
  setTimeout(() => toast.remove(), 3200);
}

function formatDate(isoString) {
  if (!isoString) return 'Not set';
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) return isoString;
  return date.toLocaleString('en-LK', { dateStyle: 'medium', timeStyle: 'short' });
}

function formatCurrency(amount) {
  const value = Number(amount || 0);
  return 'LKR ' + value.toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(value = '') {
  return String(value).replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char]));
}

const DEFAULT_BIKE_IMAGE = 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&w=900&q=80';
const DEFAULT_PROFILE_IMAGE = 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=300&q=80';

function normalizeImageUrl(value) {
  let url = String(value || '').trim();
  if (!url) return '';
  url = url.replace(/^['"]|['"]$/g, '');
  if (/^data:image\/[a-zA-Z0-9.+-]+;base64,/.test(url)) return url;
  url = url.split(',').map(part => part.trim()).find(Boolean) || '';

  const driveMatch = url.match(/drive\.google\.com\/file\/d\/([^/]+)/) || url.match(/[?&]id=([^&]+)/);
  if (driveMatch && url.includes('drive.google.com')) {
    return `https://drive.google.com/thumbnail?id=${encodeURIComponent(driveMatch[1])}&sz=w1000`;
  }

  if (url.includes('dropbox.com')) {
    return url.replace('www.dropbox.com', 'dl.dropboxusercontent.com').replace(/[?&]dl=0$/, '').replace(/[?&]raw=1$/, '');
  }

  const githubMatch = url.match(/^https:\/\/github\.com\/([^/]+\/[^/]+)\/blob\/(.+)$/);
  if (githubMatch) return `https://raw.githubusercontent.com/${githubMatch[1]}/${githubMatch[2]}`;

  return url;
}

function imageSrc(value, fallback = DEFAULT_BIKE_IMAGE) {
  return normalizeImageUrl(value) || fallback;
}

function imageFallbackAttrs(fallback = DEFAULT_BIKE_IMAGE) {
  return `onerror="this.onerror=null;this.src='${escapeHtml(fallback)}';"`;
}

function badgeClass(status = '') {
  const s = String(status).toUpperCase();
  if (['AVAILABLE', 'ACTIVE', 'SUCCESS', 'APPROVED', 'COMPLETED', 'READ'].includes(s)) return 'bg-emerald-400/15 text-emerald-300 border-emerald-400/30';
  if (['RENTED', 'FAILED', 'REJECTED', 'CANCELLED'].includes(s)) return 'bg-red-400/15 text-red-300 border-red-400/30';
  if (['MAINTENANCE', 'PENDING'].includes(s)) return 'bg-yellow-400/15 text-yellow-200 border-yellow-400/30';
  if (['REFUNDED'].includes(s)) return 'bg-orange-400/15 text-orange-300 border-orange-400/30';
  return 'bg-zinc-700/70 text-zinc-200 border-zinc-600';
}

function getStatusBadge(status) {
  return `<span class="inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold ${badgeClass(status)}">${escapeHtml(status || 'UNKNOWN')}</span>`;
}

function logout() {
  localStorage.clear();
  location.href = 'login.html';
}

function requireAuth() {
  if (!localStorage.getItem('accessToken')) location.href = 'login.html';
}

function tailwindConfigScript() {
  return `<script>tailwind.config={theme:{extend:{colors:{app:{bg:'#0f0f0f',card:'#1a1a1a',side:'#111111',border:'#2a2a2a',accent:'#00d4aa',primary:'#f0f0f0',secondary:'#9ca3af',muted:'#6b7280'}}}}}</script>`;
}

function renderAppShell(activeId, title, subtitle = '') {
  const username = localStorage.getItem('username') || 'Rider';
  const role = getUserRole() || 'RIDER';
  const allowedLinks = PAGE_LINKS.filter(link => !link.roles || link.roles.includes(role));
  if (!allowedLinks.some(link => link.id === activeId)) {
    location.href = role === 'USER' ? 'ride-sharing.html' : 'dashboard.html';
    return;
  }
  document.body.className = 'min-h-screen bg-[#0b0d0f] text-app-primary';
  document.body.insertAdjacentHTML('afterbegin', `
    <div class="pointer-events-none fixed inset-0 bg-[linear-gradient(135deg,rgba(0,212,170,0.10),transparent_34%,rgba(59,130,246,0.07)_72%,transparent)]"></div>
    <button id="menuBtn" class="fixed left-4 top-4 z-50 rounded-lg border border-white/10 bg-[#171b1f]/90 p-2 text-app-primary shadow-xl shadow-black/30 backdrop-blur lg:hidden" aria-label="Open menu">
      <span class="block h-0.5 w-6 bg-app-primary"></span><span class="mt-1.5 block h-0.5 w-6 bg-app-primary"></span><span class="mt-1.5 block h-0.5 w-6 bg-app-primary"></span>
    </button>
    <div id="sideBackdrop" class="fixed inset-0 z-30 hidden bg-black/60 lg:hidden"></div>
    <aside id="sidebar" class="fixed left-0 top-0 z-40 h-full w-[260px] -translate-x-full border-r border-white/10 bg-[#0f1215]/95 shadow-2xl shadow-black/40 backdrop-blur transition-transform lg:translate-x-0">
      <div class="flex h-24 items-center gap-3 border-b border-white/10 px-6">
        <div class="grid h-12 w-12 place-items-center rounded-lg bg-app-accent text-lg font-black text-black shadow-lg shadow-teal-500/20">BR</div>
        <div><p class="text-lg font-bold">BikeShare</p><p class="text-xs text-app-secondary">Rental System</p></div>
      </div>
      <nav class="space-y-1.5 p-4">${allowedLinks.map(link => `
        <a href="${link.href}" data-page="${link.id}" class="group flex items-center justify-between rounded-lg px-4 py-3 text-sm font-semibold transition ${activeId === link.id ? 'bg-app-accent text-black shadow-lg shadow-teal-500/15' : 'text-app-secondary hover:bg-white/[0.06] hover:text-app-primary'}">
          <span>${link.label}</span>${link.id === 'notifications' ? '<span id="navUnread" class="hidden rounded-full bg-red-500 px-2 py-0.5 text-xs text-white">0</span>' : link.id === 'ride-sharing' ? '<span id="navChatUnread" class="hidden rounded-full bg-red-500 px-2 py-0.5 text-xs text-white">0</span>' : ''}
        </a>`).join('')}
      </nav>
    </aside>
    <main class="relative min-h-screen p-5 pt-20 lg:ml-[260px] lg:p-8">
      <header class="mb-8 flex flex-col gap-4 rounded-lg border border-white/10 bg-[#14181c]/80 p-5 shadow-xl shadow-black/20 backdrop-blur sm:flex-row sm:items-center sm:justify-between">
        <div><h1 class="text-2xl font-bold tracking-normal sm:text-3xl">${escapeHtml(title)}</h1><p class="mt-1 text-sm text-app-secondary">${escapeHtml(subtitle)}</p></div>
        <div class="flex items-center gap-3">
          <div class="text-right"><p class="text-sm font-semibold">${escapeHtml(username)}</p><p class="text-xs text-app-secondary">${escapeHtml(role)}</p></div>
          <span class="rounded-full border border-app-accent/30 bg-app-accent/10 px-3 py-1 text-xs font-bold text-app-accent">${escapeHtml(role)}</span>
          <button onclick="logout()" class="rounded-lg border border-white/10 bg-white/[0.03] px-4 py-2 text-sm text-app-secondary transition hover:border-red-400/60 hover:bg-red-500/10 hover:text-red-200">Logout</button>
        </div>
      </header>
      <section id="pageContent"></section>
    </main>`);
  document.getElementById('menuBtn')?.addEventListener('click', () => toggleSidebar(true));
  document.getElementById('sideBackdrop')?.addEventListener('click', () => toggleSidebar(false));
  updateUnreadBadge();
  unreadPoll = setInterval(updateUnreadBadge, 30000);
}

function toggleSidebar(open) {
  document.getElementById('sidebar')?.classList.toggle('-translate-x-full', !open);
  document.getElementById('sideBackdrop')?.classList.toggle('hidden', !open);
}

async function updateUnreadBadge() {
  if (!getToken()) return;
  try {
    const response = await apiFetch('/api/notifications/my/unread-count');
    const raw = unwrap(response);
    const count = Number(raw?.count ?? raw?.unreadCount ?? raw ?? 0);
    const el = document.getElementById('navUnread');
    if (el) {
      el.textContent = count;
      el.classList.toggle('hidden', count <= 0);
    }
    const chatEl = document.getElementById('navChatUnread');
    if (chatEl) {
      try {
        const chatRes = await apiFetch('/api/ride-shares/messages/unread-total');
        const chatRaw = unwrap(chatRes);
        const chatCount = Number(chatRaw?.count ?? chatRaw?.unreadCount ?? chatRaw ?? 0);
        chatEl.textContent = chatCount;
        chatEl.classList.toggle('hidden', chatCount <= 0);
      } catch (_) {}
    }
  } catch (_) {}
}

function card(title, value, hint = '') {
  return `<div class="rounded-lg border border-white/10 bg-[#171b1f]/85 p-5 shadow-xl shadow-black/20 transition hover:-translate-y-0.5 hover:border-app-accent/30 hover:bg-[#1b2025]"><p class="text-sm font-medium text-app-secondary">${escapeHtml(title)}</p><p class="mt-3 text-3xl font-black text-app-primary">${escapeHtml(value)}</p>${hint ? `<p class="mt-2 text-xs text-app-muted">${escapeHtml(hint)}</p>` : ''}</div>`;
}

function emptyState(message) {
  return `<div class="rounded-lg border border-dashed border-white/15 bg-white/[0.03] p-8 text-center text-app-secondary">${escapeHtml(message)}</div>`;
}

function modalShell(id, title, body) {
  return `<div id="${id}" class="fixed inset-0 z-[70] hidden items-center justify-center bg-black/75 p-4 backdrop-blur-sm"><div class="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-lg border border-white/10 bg-[#171b1f] p-6 shadow-2xl shadow-black/50"><div class="mb-5 flex items-center justify-between"><h2 class="text-xl font-bold">${title}</h2><button data-close="${id}" class="rounded-lg border border-white/10 bg-white/[0.03] px-3 py-1 text-app-secondary hover:text-app-primary">Close</button></div>${body}</div></div>`;
}

function openModal(id) { document.getElementById(id)?.classList.replace('hidden', 'flex'); }
function closeModal(id) { document.getElementById(id)?.classList.replace('flex', 'hidden'); }
document.addEventListener('click', (event) => { const id = event.target?.dataset?.close; if (id) closeModal(id); });

function inputClass() { return 'w-full rounded-lg border border-white/10 bg-[#0f1215] px-3 py-2 text-app-primary outline-none transition placeholder:text-app-muted focus:border-app-accent focus:ring-2 focus:ring-app-accent/20'; }
function buttonClass(kind = 'primary') { return kind === 'danger' ? 'rounded-lg bg-red-500 px-4 py-2 text-sm font-bold text-white shadow-lg shadow-red-500/15 transition hover:bg-red-400' : kind === 'ghost' ? 'rounded-lg border border-white/10 bg-white/[0.03] px-4 py-2 text-sm font-semibold text-app-secondary transition hover:border-app-accent/40 hover:bg-app-accent/10 hover:text-app-primary' : 'rounded-lg bg-app-accent px-4 py-2 text-sm font-bold text-black shadow-lg shadow-teal-500/20 transition hover:bg-teal-300'; }
function toIsoLocal(value) { return value ? new Date(value).toISOString().slice(0, 19) : null; }
function dateRangeQuery(startValue, endValue) {
  if (!startValue || !endValue) {
    throw new Error('Please select both start and end date/time.');
  }
  const start = new Date(startValue);
  const end = new Date(endValue);
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    throw new Error('Please enter a valid date/time range.');
  }
  if (end <= start) {
    throw new Error('End date/time must be after start date/time.');
  }
  return new URLSearchParams({
    start: toIsoLocal(startValue),
    end: toIsoLocal(endValue)
  });
}
function daysBetween(start, end) { const diff = new Date(end) - new Date(start); return Math.max(1, Math.ceil(diff / 86400000)); }
