import Phaser from 'phaser';
import './style.css';
import { BattleScene } from './BattleScene';
import { GameSocket, type ConnectionStatus } from './GameSocket';
import type { GameEvent, MatchRuntime, MatchState, StateUpdatedMessage } from './types';

const app = document.querySelector<HTMLElement>('#app');
if (!app) throw new Error('Missing #app element');

app.innerHTML = `
  <div class="shell">
    <header class="topbar"><div class="brand-lockup"><span class="brand-mark">ACW</span><div><p class="eyebrow">SERVER-AUTHORITATIVE STRATEGY</p><h1>Ant Colony Wars</h1></div></div><div id="connection" class="connection connecting"><span class="connection-dot"></span><span id="connection-label">Chưa vào trận</span></div></header>
    <main>
      <section id="lobby" class="lobby-panel"><div class="lobby-copy"><p class="eyebrow">PHASE 8 · MATCH ROOM</p><h2>Chọn trận để vào chơi</h2><p>Chọn một bàn đang mở, nhập mã tài khoản nếu có, rồi bắt đầu theo dõi cùng một trạng thái mà máy chủ xác nhận.</p></div><form id="join-form" class="join-form"><label>Trận đấu<select id="match-select" required><option value="match-default">Đang tải danh sách…</option></select></label><label>Mã truy cập <span class="muted">(không bắt buộc)</span><input id="token-input" type="password" autocomplete="off" placeholder="Dán mã phiên"></label><button type="submit">Vào trận</button><p id="lobby-message" class="form-message" role="status"></p></form></section>
      <section id="arena" class="arena hidden"><div class="arena-heading"><div><p class="eyebrow">LIVE MATCH</p><h2 id="arena-title">match-default</h2></div><button id="change-match" class="quiet-button" type="button">Đổi trận</button></div><div class="workspace"><section class="board-card" aria-label="Bản đồ trận đấu"><div id="game"></div><div id="result-banner" class="result-banner hidden"></div></section><aside class="sidebar"><section class="panel match-panel"><p class="panel-label">TRẬN ĐẤU</p><div class="status-row"><span>Trạng thái</span><strong id="match-status">Đang chờ</strong></div><div class="status-row"><span>Revision</span><strong id="revision">—</strong></div><div class="status-row"><span>Winner</span><strong id="winner">—</strong></div><div class="status-row"><span>Nhịp mô phỏng</span><strong id="tick">—</strong></div></section><section class="panel"><p class="panel-label">LỰC LƯỢNG</p><div id="forces" class="forces empty">Chưa có snapshot.</div></section><section class="panel log-panel"><p class="panel-label">COMMAND GẦN NHẤT</p><ol id="command-log" class="command-log"><li class="empty">Chưa có command.</li></ol></section></aside></div></section>
    </main><footer>Trạng thái hiển thị lấy từ Java server. Mất kết nối sẽ tự nối lại.</footer>
  </div>`;

const battleScene = new BattleScene();
let game: Phaser.Game | null = null;
let gameSocket: GameSocket | null = null;
const events: GameEvent[] = [];
const lanHost = window.location.hostname || 'localhost';
const socketProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
const httpProtocol = window.location.protocol === 'https:' ? 'https' : 'http';
const socketBase = import.meta.env.VITE_GAME_WS_URL ?? `${socketProtocol}://${lanHost}:8080/game`;
const httpBase = import.meta.env.VITE_GAME_HTTP_URL ?? `${httpProtocol}://${lanHost}:8080`;

void loadMatches();
document.querySelector<HTMLFormElement>('#join-form')?.addEventListener('submit', (event) => { event.preventDefault(); const matchId = document.querySelector<HTMLSelectElement>('#match-select')?.value ?? 'match-default'; const token = document.querySelector<HTMLInputElement>('#token-input')?.value.trim() ?? ''; startMatch(matchId, token); });
document.querySelector<HTMLButtonElement>('#change-match')?.addEventListener('click', () => { gameSocket?.disconnect(); game?.destroy(true); game = null; document.querySelector('#arena')?.classList.add('hidden'); document.querySelector('#lobby')?.classList.remove('hidden'); setConnection('offline'); });

async function loadMatches(): Promise<void> {
  try { const response = await fetch(`${httpBase}/api/matches`); if (!response.ok) throw new Error(`HTTP ${response.status}`); const matches = await response.json() as MatchRuntime[]; const select = document.querySelector<HTMLSelectElement>('#match-select'); if (!select || matches.length === 0) return; select.innerHTML = matches.map((match) => `<option value="${escapeHtml(match.matchId)}">${escapeHtml(match.matchId)} · ${match.status === 'RUNNING' ? 'đang chạy' : 'đã kết thúc'} · #${match.revision}</option>`).join(''); setText('#lobby-message', `${matches.length} trận sẵn sàng.`); } catch { setText('#lobby-message', 'Chưa lấy được danh sách; bạn vẫn có thể thử trận mặc định.'); }
}

function startMatch(matchId: string, token: string): void {
  gameSocket?.disconnect(); game?.destroy(true); game = new Phaser.Game({ type: Phaser.AUTO, width: 640, height: 640, parent: 'game', backgroundColor: '#0b1120', scene: battleScene, scale: { mode: Phaser.Scale.FIT, autoCenter: Phaser.Scale.CENTER_BOTH } }); events.length = 0; setText('#arena-title', matchId); document.querySelector('#lobby')?.classList.add('hidden'); document.querySelector('#arena')?.classList.remove('hidden'); const query = new URLSearchParams({ matchId }); if (token) query.set('token', token); const url = `${socketBase}${socketBase.includes('?') ? '&' : '?'}${query.toString()}`; gameSocket = new GameSocket(url, setConnection, receiveState); gameSocket.connect();
}

function receiveState(message: StateUpdatedMessage): void { battleScene.updateSnapshot(message.state); if (message.event && !events.some((event) => event.sequence === message.event?.sequence)) { events.unshift(message.event); events.splice(5); } updatePanel(message.state); updateLog(); }
function setConnection(status: ConnectionStatus): void { const connection = document.querySelector<HTMLElement>('#connection'); const label = document.querySelector<HTMLElement>('#connection-label'); if (!connection || !label) return; connection.className = `connection ${status}`; label.textContent = status === 'online' ? 'Đang trực tuyến' : status === 'connecting' ? 'Đang kết nối' : 'Mất kết nối'; }
function updatePanel(state: MatchState): void { const finished = state.status === 'FINISHED'; setText('#match-status', finished ? 'Đã kết thúc' : 'Đang diễn ra'); setText('#revision', String(state.revision)); setText('#winner', state.winnerTeamId ?? '—'); setText('#tick', String(state.simulationTick)); const banner = document.querySelector<HTMLElement>('#result-banner'); if (banner) { banner.classList.toggle('hidden', !finished); banner.textContent = state.winnerTeamId ? `Đội ${state.winnerTeamId} thắng trận` : 'Trận đã kết thúc'; } const forces = document.querySelector<HTMLElement>('#forces'); if (!forces) return; forces.classList.remove('empty'); forces.innerHTML = state.teams.map((team) => { const headquarters = state.headquarters.find((item) => item.teamId === team.id); const units = state.units.filter((unit) => unit.teamId === team.id); return `<article class="team-card" style="--team-color: ${safeColor(team.color)}"><div class="team-heading"><span class="team-swatch"></span><strong>${escapeHtml(team.displayName)}</strong><small>${team.resources} res</small></div><div class="force-stat"><span>HQ</span><strong>${headquarters?.hp ?? 0} HP</strong></div>${units.map((unit) => `<div class="force-stat"><span>${escapeHtml(unit.displayName)}</span><strong>${unit.hp} HP</strong></div>`).join('')}</article>`; }).join(''); }
function updateLog(): void { const log = document.querySelector<HTMLOListElement>('#command-log'); if (!log) return; log.innerHTML = events.length ? events.map((event) => `<li><span class="log-sequence">#${event.sequence}</span><div><strong>${escapeHtml(event.actorTeamId)} · ${escapeHtml(event.action)}</strong><small>${escapeHtml(event.unitId)}${event.targetId ? ` → ${escapeHtml(event.targetId)}` : ''}</small></div></li>`).join('') : '<li class="empty">Chưa có command.</li>'; }
function setText(selector: string, value: string): void { const element = document.querySelector<HTMLElement>(selector); if (element) element.textContent = value; }
function escapeHtml(value: string): string { return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#039;'); }
function safeColor(value: string): string { return /^#[0-9a-fA-F]{6}$/.test(value) ? value : '#64748b'; }
