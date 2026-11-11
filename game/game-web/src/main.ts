import Phaser from 'phaser';
import './style.css';
import { BattleScene } from './BattleScene';
import { GameSocket, type ConnectionStatus } from './GameSocket';
import type { Account, GameEvent, LobbyView, MatchRuntime, MatchState, Session, StateUpdatedMessage } from './types';

const SAMPLE_MAP = `{
  "map": "12x12",
  "teams": 2,
  "accountsPerTeam": 2,
  "resources": [
    { "x": 5, "y": 5, "amount": 20 },
    { "x": 6, "y": 6, "amount": 20 }
  ],
  "headquarters": [
    { "team": 1, "x": 0, "y": 0 },
    { "team": 2, "x": 11, "y": 11 }
  ],
  "units": [
    { "team": 1, "x": 1, "y": 0, "type": "WORKER" },
    { "team": 2, "x": 10, "y": 11, "type": "WORKER" }
  ]
}`;

const app = document.querySelector<HTMLElement>('#app');
if (!app) throw new Error('Missing #app element');

app.innerHTML = `
  <div class="shell">
    <header class="topbar">
      <div class="brand-lockup"><span class="brand-mark">ACW</span><div><p class="eyebrow">SERVER-AUTHORITATIVE STRATEGY</p><h1>Ant Colony Wars</h1></div></div>
      <div class="header-actions"><span id="account-chip" class="account-chip hidden"></span><button id="logout" class="quiet-button compact hidden" type="button">Đăng xuất</button><div id="connection" class="connection"><span class="connection-dot"></span><span id="connection-label">Chưa vào trận</span></div></div>
    </header>
    <main>
      <section id="auth" class="auth-layout">
        <div class="hero-copy"><p class="eyebrow">PLAYER LOBBY</p><h2>Đăng nhập để tạo hoặc tìm trận</h2><p>Mỗi tài khoản giữ một vị trí trong đội. Sau khi vào phòng, dùng token đội trong Desktop Code Studio để điều khiển quân.</p></div>
        <div class="auth-grid">
          <form id="login-form" class="card form-stack"><div><p class="panel-label">ĐÃ CÓ TÀI KHOẢN</p><h3>Đăng nhập</h3></div><label>Username<input name="username" autocomplete="username" required minlength="3" maxlength="32"></label><label>Password<input name="password" type="password" autocomplete="current-password" required maxlength="72"></label><button type="submit">Đăng nhập</button><p class="form-message" data-message="login"></p></form>
          <form id="register-form" class="card form-stack"><div><p class="panel-label">NGƯỜI CHƠI MỚI</p><h3>Đăng ký</h3></div><label>Username<input name="username" autocomplete="username" required minlength="3" maxlength="32"></label><label>Password<input name="password" type="password" autocomplete="new-password" required maxlength="72"></label><button type="submit">Tạo tài khoản</button><p class="form-message" data-message="register"></p></form>
        </div>
      </section>

      <section id="dashboard" class="hidden">
        <div class="section-heading"><div><p class="eyebrow">MATCH CENTER</p><h2 id="welcome">Chọn cách vào trận</h2></div></div>
        <div class="action-grid"><button id="show-create" class="action-card" type="button"><span class="action-icon">＋</span><strong>Tạo trận</strong><small>Nhập script bản đồ và mở phòng chờ mới.</small></button><button id="show-find" class="action-card" type="button"><span class="action-icon">⌕</span><strong>Tìm trận</strong><small>Tham gia bằng mã trận và token đội.</small></button></div>
        <section class="card demo-strip"><div><p class="panel-label">TRẬN ĐANG CHẠY</p><p class="muted-copy">Có thể mở nhanh một trận để quan sát.</p></div><select id="match-select"><option value="match-default">Đang tải…</option></select><button id="watch-match" class="quiet-button" type="button">Xem trận</button></section>
      </section>

      <section id="create-panel" class="card editor-panel hidden"><div class="section-heading"><div><p class="eyebrow">CREATE MATCH</p><h2>Tạo bản đồ</h2><p class="muted-copy">JSON mô tả bản đồ vuông nxn, tài nguyên, nhà chính, lính, số đội và số tài khoản mỗi đội.</p></div><button class="quiet-button back-dashboard" type="button">Quay lại</button></div><form id="create-form" class="form-stack"><label>Map script<textarea id="map-script" spellcheck="false"></textarea></label><button type="submit">Tạo phòng chờ</button><p class="form-message" data-message="create"></p></form></section>

      <section id="find-panel" class="card find-panel hidden"><div class="section-heading"><div><p class="eyebrow">FIND MATCH</p><h2>Nhập mã để tham gia</h2></div><button class="quiet-button back-dashboard" type="button">Quay lại</button></div><form id="find-form" class="form-stack narrow-form"><label>Mã trận<input name="matchCode" autocomplete="off" maxlength="6" placeholder="ABC234" required></label><label>Token đội<input name="teamToken" autocomplete="off" maxlength="8" placeholder="TOKEN ĐỘI" required></label><button type="submit">Tìm và vào trận</button><p class="form-message" data-message="find"></p></form></section>

      <section id="waiting-room" class="hidden"><div class="section-heading"><div><p class="eyebrow">WAITING ROOM</p><h2>Phòng <span id="waiting-code">—</span></h2><p id="waiting-status" class="muted-copy">Đang chờ chủ phòng bắt đầu.</p></div><button id="leave-waiting" class="quiet-button" type="button">Về sảnh</button></div><div id="team-grid" class="team-grid"></div><div class="waiting-actions"><button id="start-game" class="hidden" type="button">Bắt đầu trận đấu</button><button id="open-game" class="hidden" type="button">Mở bản đồ trận đấu</button><p class="form-message" data-message="waiting"></p></div></section>

      <section id="arena" class="arena hidden"><div class="arena-heading"><div><p class="eyebrow">LIVE MATCH</p><h2 id="arena-title">match-default</h2></div><button id="change-match" class="quiet-button" type="button">Về sảnh</button></div><div class="workspace"><section class="board-card" aria-label="Bản đồ trận đấu"><div id="game"></div><div id="result-banner" class="result-banner hidden"></div></section><aside class="sidebar"><section class="panel match-panel"><p class="panel-label">TRẬN ĐẤU</p><div class="status-row"><span>Trạng thái</span><strong id="match-status">Đang chờ</strong></div><div class="status-row"><span>Revision</span><strong id="revision">—</strong></div><div class="status-row"><span>Winner</span><strong id="winner">—</strong></div><div class="status-row"><span>Nhịp mô phỏng</span><strong id="tick">—</strong></div></section><section class="panel"><p class="panel-label">LỰC LƯỢNG</p><div id="forces" class="forces empty">Chưa có snapshot.</div></section><section class="panel log-panel"><p class="panel-label">COMMAND GẦN NHẤT</p><ol id="command-log" class="command-log"><li class="empty">Chưa có command.</li></ol></section></aside></div></section>
    </main>
    <footer>Trạng thái trận đấu được xác nhận bởi Java server.</footer>
  </div>`;

const lanHost = window.location.hostname || 'localhost';
const socketProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
const httpProtocol = window.location.protocol === 'https:' ? 'https' : 'http';
const socketBase = import.meta.env.VITE_GAME_WS_URL ?? `${socketProtocol}://${lanHost}:8080/game`;
const httpBase = import.meta.env.VITE_GAME_HTTP_URL ?? `${httpProtocol}://${lanHost}:8080`;
const battleScene = new BattleScene();
const events: GameEvent[] = [];
let game: Phaser.Game | null = null;
let gameSocket: GameSocket | null = null;
let sessionToken = window.localStorage.getItem('acw-session') ?? '';
let currentAccount: Account | null = null;
let currentLobby: LobbyView | null = null;
let lobbyPoll: number | null = null;

document.querySelector<HTMLTextAreaElement>('#map-script')!.value = SAMPLE_MAP;
bindForms();
void restoreSession();

function bindForms(): void {
  document.querySelector<HTMLFormElement>('#login-form')?.addEventListener('submit', (event) => void authenticate(event, false));
  document.querySelector<HTMLFormElement>('#register-form')?.addEventListener('submit', (event) => void authenticate(event, true));
  document.querySelector('#logout')?.addEventListener('click', logout);
  document.querySelector('#show-create')?.addEventListener('click', () => showScreen('create-panel'));
  document.querySelector('#show-find')?.addEventListener('click', () => showScreen('find-panel'));
  document.querySelectorAll('.back-dashboard').forEach((button) => button.addEventListener('click', showDashboard));
  document.querySelector('#leave-waiting')?.addEventListener('click', showDashboard);
  document.querySelector('#watch-match')?.addEventListener('click', () => {
    const matchId = document.querySelector<HTMLSelectElement>('#match-select')?.value ?? 'match-default';
    startMatch(matchId, '');
  });
  document.querySelector<HTMLFormElement>('#create-form')?.addEventListener('submit', (event) => void createLobby(event));
  document.querySelector<HTMLFormElement>('#find-form')?.addEventListener('submit', (event) => void findLobby(event));
  document.querySelector('#start-game')?.addEventListener('click', () => void startLobby());
  document.querySelector('#open-game')?.addEventListener('click', () => {
    if (currentLobby) startMatch(currentLobby.matchCode, sessionToken);
  });
  document.querySelector('#change-match')?.addEventListener('click', showDashboard);
}

async function restoreSession(): Promise<void> {
  if (!sessionToken) return showScreen('auth');
  try {
    currentAccount = await api<Account>('/api/lobby/me');
    showDashboard();
  } catch {
    clearSession();
    showScreen('auth');
  }
}

async function authenticate(event: SubmitEvent, register: boolean): Promise<void> {
  event.preventDefault();
  const form = event.currentTarget as HTMLFormElement;
  const data = new FormData(form);
  const username = String(data.get('username') ?? '').trim();
  const password = String(data.get('password') ?? '');
  const message = register ? 'register' : 'login';
  setMessage(message, register ? 'Đang tạo tài khoản…' : 'Đang đăng nhập…');
  try {
    if (register) await api<Account>('/api/accounts', { method: 'POST', body: JSON.stringify({ username, password }) }, false);
    const session = await api<Session>('/api/sessions', { method: 'POST', body: JSON.stringify({ username, password }) }, false);
    sessionToken = session.token;
    currentAccount = session.account;
    window.localStorage.setItem('acw-session', sessionToken);
    form.reset();
    showDashboard();
  } catch (error) {
    setMessage(message, errorMessage(error), true);
  }
}

async function createLobby(event: SubmitEvent): Promise<void> {
  event.preventDefault();
  const mapScript = document.querySelector<HTMLTextAreaElement>('#map-script')?.value ?? '';
  setMessage('create', 'Đang kiểm tra script và tạo phòng…');
  try {
    currentLobby = await api<LobbyView>('/api/lobbies', { method: 'POST', body: JSON.stringify({ mapScript }) });
    showWaitingRoom();
  } catch (error) {
    setMessage('create', errorMessage(error), true);
  }
}

async function findLobby(event: SubmitEvent): Promise<void> {
  event.preventDefault();
  const form = event.currentTarget as HTMLFormElement;
  const data = new FormData(form);
  setMessage('find', 'Đang tìm phòng…');
  try {
    currentLobby = await api<LobbyView>('/api/lobbies/join', {
      method: 'POST',
      body: JSON.stringify({
        matchCode: String(data.get('matchCode') ?? '').trim().toUpperCase(),
        teamToken: String(data.get('teamToken') ?? '').trim().toUpperCase(),
      }),
    });
    form.reset();
    showWaitingRoom();
  } catch (error) {
    setMessage('find', errorMessage(error), true);
  }
}

async function startLobby(): Promise<void> {
  if (!currentLobby) return;
  setMessage('waiting', 'Đang khởi tạo trận đấu…');
  try {
    currentLobby = await api<LobbyView>(`/api/lobbies/${encodeURIComponent(currentLobby.matchCode)}/start`, { method: 'POST' });
    renderLobby(currentLobby);
    startMatch(currentLobby.matchCode, sessionToken);
  } catch (error) {
    setMessage('waiting', errorMessage(error), true);
  }
}

function showDashboard(): void {
  stopLobbyPoll();
  gameSocket?.disconnect();
  game?.destroy(true);
  game = null;
  showScreen('dashboard');
  setConnection('idle');
  setText('#welcome', `Xin chào ${currentAccount?.username ?? ''}`);
  document.querySelector('#account-chip')?.classList.remove('hidden');
  document.querySelector('#logout')?.classList.remove('hidden');
  setText('#account-chip', currentAccount?.username ?? '');
  void loadMatches();
}

function showWaitingRoom(): void {
  if (!currentLobby) return;
  showScreen('waiting-room');
  renderLobby(currentLobby);
  stopLobbyPoll();
  lobbyPoll = window.setInterval(() => void refreshLobby(), 2000);
}

async function refreshLobby(): Promise<void> {
  if (!currentLobby || document.querySelector('#waiting-room')?.classList.contains('hidden')) return;
  try {
    currentLobby = await api<LobbyView>(`/api/lobbies/${encodeURIComponent(currentLobby.matchCode)}`);
    renderLobby(currentLobby);
  } catch (error) {
    setMessage('waiting', errorMessage(error), true);
  }
}

function renderLobby(lobby: LobbyView): void {
  setText('#waiting-code', lobby.matchCode);
  setText('#waiting-status', lobby.status === 'WAITING' ? 'Đang chờ chủ phòng bắt đầu.' : 'Trận đã bắt đầu.');
  const grid = document.querySelector<HTMLElement>('#team-grid');
  if (grid) {
    grid.innerHTML = lobby.teams.map((team) => {
      const slots = Array.from({ length: team.capacity }, (_, index) => {
        const member = team.members[index];
        const own = member?.accountId === lobby.currentAccountId ? ' own' : '';
        return `<div class="player-slot${own}">${member ? escapeHtml(member.username) : '<span>Trống</span>'}</div>`;
      }).join('');
      return `<article class="lobby-team" style="--team-color:${safeColor(team.color)}"><div class="lobby-team-head"><div><span class="team-swatch"></span><strong>${escapeHtml(team.displayName)}</strong></div><span>${team.members.length}/${team.capacity}</span></div><div class="team-token"><small>TOKEN ĐỘI</small><code>${escapeHtml(team.token)}</code></div><div class="slot-grid">${slots}</div></article>`;
    }).join('');
  }
  document.querySelector('#start-game')?.classList.toggle('hidden', !lobby.owner || lobby.status !== 'WAITING');
  document.querySelector('#open-game')?.classList.toggle('hidden', lobby.status !== 'RUNNING');
  setMessage('waiting', '');
}

async function loadMatches(): Promise<void> {
  try {
    const matches = await api<MatchRuntime[]>('/api/matches', {}, false);
    const select = document.querySelector<HTMLSelectElement>('#match-select');
    if (!select || matches.length === 0) return;
    select.innerHTML = matches.map((match) => `<option value="${escapeHtml(match.matchId)}">${escapeHtml(match.matchId)} · ${match.status === 'RUNNING' ? 'đang chạy' : 'đã kết thúc'}</option>`).join('');
  } catch {
    // Create and find forms surface their own connection errors.
  }
}

function startMatch(matchId: string, token: string): void {
  stopLobbyPoll();
  gameSocket?.disconnect();
  game?.destroy(true);
  game = new Phaser.Game({ type: Phaser.AUTO, width: 640, height: 640, parent: 'game', backgroundColor: '#0b1120', scene: battleScene, scale: { mode: Phaser.Scale.FIT, autoCenter: Phaser.Scale.CENTER_BOTH } });
  events.length = 0;
  setText('#arena-title', matchId);
  showScreen('arena');
  const query = new URLSearchParams({ matchId });
  if (token) query.set('token', token);
  const url = `${socketBase}${socketBase.includes('?') ? '&' : '?'}${query.toString()}`;
  gameSocket = new GameSocket(url, setConnection, receiveState);
  gameSocket.connect();
}

async function api<T>(path: string, init: RequestInit = {}, authenticated = true): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set('Content-Type', 'application/json');
  if (authenticated && sessionToken) headers.set('Authorization', `Bearer ${sessionToken}`);
  const response = await fetch(`${httpBase}${path}`, { ...init, headers });
  if (!response.ok) {
    const body = await response.json().catch(() => ({})) as { code?: string; message?: string };
    throw new ApiError(body.code ?? 'REQUEST_FAILED', body.message ?? `HTTP ${response.status}`);
  }
  return response.json() as Promise<T>;
}

function receiveState(message: StateUpdatedMessage): void {
  battleScene.updateSnapshot(message.state);
  if (message.event && !events.some((event) => event.sequence === message.event?.sequence)) {
    events.unshift(message.event);
    events.splice(5);
  }
  updatePanel(message.state);
  updateLog();
}

function updatePanel(state: MatchState): void {
  const finished = state.status === 'FINISHED';
  setText('#match-status', finished ? 'Đã kết thúc' : 'Đang diễn ra');
  setText('#revision', String(state.revision));
  setText('#winner', state.winnerTeamId ?? '—');
  setText('#tick', String(state.simulationTick));
  const banner = document.querySelector<HTMLElement>('#result-banner');
  if (banner) { banner.classList.toggle('hidden', !finished); banner.textContent = state.winnerTeamId ? `Đội ${state.winnerTeamId} thắng trận` : 'Trận đã kết thúc'; }
  const forces = document.querySelector<HTMLElement>('#forces');
  if (!forces) return;
  forces.classList.remove('empty');
  forces.innerHTML = state.teams.map((team) => {
    const headquarters = state.headquarters.find((item) => item.teamId === team.id);
    const units = state.units.filter((unit) => unit.teamId === team.id);
    return `<article class="team-card" style="--team-color:${safeColor(team.color)}"><div class="team-heading"><span class="team-swatch"></span><strong>${escapeHtml(team.displayName)}</strong><small>${team.resources} res</small></div><div class="force-stat"><span>HQ</span><strong>${headquarters?.hp ?? 0} HP</strong></div>${units.map((unit) => `<div class="force-stat"><span>${escapeHtml(unit.displayName)}</span><strong>${unit.hp} HP</strong></div>`).join('')}</article>`;
  }).join('');
}

function updateLog(): void {
  const log = document.querySelector<HTMLOListElement>('#command-log');
  if (!log) return;
  log.innerHTML = events.length ? events.map((event) => `<li><span class="log-sequence">#${event.sequence}</span><div><strong>${escapeHtml(event.actorTeamId)} · ${escapeHtml(event.action)}</strong><small>${escapeHtml(event.unitId)}${event.targetId ? ` → ${escapeHtml(event.targetId)}` : ''}</small></div></li>`).join('') : '<li class="empty">Chưa có command.</li>';
}

function setConnection(status: ConnectionStatus | 'idle'): void {
  const connection = document.querySelector<HTMLElement>('#connection');
  if (!connection) return;
  connection.className = `connection ${status === 'idle' ? '' : status}`;
  setText('#connection-label', status === 'online' ? 'Đang trực tuyến' : status === 'connecting' ? 'Đang kết nối' : status === 'offline' ? 'Mất kết nối' : 'Chưa vào trận');
}

function showScreen(id: string): void {
  ['auth', 'dashboard', 'create-panel', 'find-panel', 'waiting-room', 'arena'].forEach((screen) => document.querySelector(`#${screen}`)?.classList.toggle('hidden', screen !== id));
}

function stopLobbyPoll(): void {
  if (lobbyPoll !== null) window.clearInterval(lobbyPoll);
  lobbyPoll = null;
}

function logout(): void {
  stopLobbyPoll();
  gameSocket?.disconnect();
  game?.destroy(true);
  game = null;
  clearSession();
  document.querySelector('#account-chip')?.classList.add('hidden');
  document.querySelector('#logout')?.classList.add('hidden');
  showScreen('auth');
  setConnection('idle');
}

function clearSession(): void {
  sessionToken = '';
  currentAccount = null;
  currentLobby = null;
  window.localStorage.removeItem('acw-session');
}

function setMessage(name: string, value: string, error = false): void {
  const element = document.querySelector<HTMLElement>(`[data-message="${name}"]`);
  if (!element) return;
  element.textContent = value;
  element.classList.toggle('error', error);
}

function errorMessage(error: unknown): string {
  if (!(error instanceof ApiError)) return 'Không thể kết nối máy chủ.';
  const messages: Record<string, string> = {
    USERNAME_TAKEN: 'Username này đã được sử dụng.',
    INVALID_CREDENTIALS: 'Username hoặc password không đúng.',
    INVALID_SESSION: 'Phiên đăng nhập đã hết hạn.',
    TEAM_FULL: 'Đội này đã đủ người.',
    MATCH_NOT_FOUND: 'Không tìm thấy mã trận.',
    TEAM_TOKEN_NOT_FOUND: 'Mã trận hoặc token đội không đúng.',
    MATCH_ALREADY_STARTED: 'Trận này đã bắt đầu.',
  };
  return messages[error.code] ?? error.message;
}

function setText(selector: string, value: string): void {
  const element = document.querySelector<HTMLElement>(selector);
  if (element) element.textContent = value;
}

function escapeHtml(value: string): string {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#039;');
}

function safeColor(value: string): string {
  return /^#[0-9a-fA-F]{6}$/.test(value) ? value : '#64748b';
}

class ApiError extends Error {
  constructor(readonly code: string, message: string) {
    super(message);
  }
}
