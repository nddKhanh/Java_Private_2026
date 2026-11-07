import Phaser from 'phaser';
import './style.css';
import { BattleScene } from './BattleScene';
import { GameSocket, type ConnectionStatus } from './GameSocket';
import type { GameEvent, MatchState, StateUpdatedMessage } from './types';

const app = document.querySelector<HTMLElement>('#app');
if (!app) {
  throw new Error('Missing #app element');
}

app.innerHTML = `
  <header class="topbar">
    <div>
      <p class="eyebrow">JAVA-CONTROLLED STRATEGY</p>
      <h1>Ant Colony Wars</h1>
    </div>
    <div id="connection" class="connection connecting">
      <span class="connection-dot"></span>
      <span id="connection-label">Đang kết nối</span>
    </div>
  </header>
  <main class="workspace">
    <section class="board-card" aria-label="Bản đồ trận đấu">
      <div id="game"></div>
    </section>
    <aside class="sidebar">
      <section class="panel match-panel">
        <p class="panel-label">TRẬN ĐẤU</p>
        <div class="status-row"><span>Trạng thái</span><strong id="match-status">Chờ server</strong></div>
        <div class="status-row"><span>Revision</span><strong id="revision">—</strong></div>
        <div class="status-row"><span>Winner</span><strong id="winner">—</strong></div>
      </section>
      <section class="panel">
        <p class="panel-label">LỰC LƯỢNG</p>
        <div id="forces" class="forces empty">Chưa có snapshot.</div>
      </section>
      <section class="panel log-panel">
        <p class="panel-label">COMMAND GẦN NHẤT</p>
        <ol id="command-log" class="command-log">
          <li class="empty">Chưa có command.</li>
        </ol>
      </section>
    </aside>
  </main>
  <footer>Viewer chỉ hiển thị state do Java server xác nhận.</footer>
`;

const battleScene = new BattleScene();
const config: Phaser.Types.Core.GameConfig = {
  type: Phaser.AUTO,
  width: 640,
  height: 640,
  parent: 'game',
  backgroundColor: '#0b1120',
  scene: battleScene,
  scale: {
    mode: Phaser.Scale.FIT,
    autoCenter: Phaser.Scale.CENTER_BOTH,
  },
};

new Phaser.Game(config);

const events: GameEvent[] = [];
const socketUrl = import.meta.env.VITE_GAME_WS_URL ?? 'ws://localhost:8080/game';
const gameSocket = new GameSocket(socketUrl, updateConnection, receiveState);
gameSocket.connect();

function receiveState(message: StateUpdatedMessage): void {
  battleScene.updateSnapshot(message.state);
  if (message.event && !events.some((event) => event.sequence === message.event?.sequence)) {
    events.unshift(message.event);
    events.splice(5);
  }
  updatePanel(message.state);
  updateLog();
}

function updateConnection(status: ConnectionStatus): void {
  const connection = document.querySelector<HTMLElement>('#connection');
  const label = document.querySelector<HTMLElement>('#connection-label');
  if (!connection || !label) {
    return;
  }
  connection.className = `connection ${status}`;
  label.textContent = status === 'online'
    ? 'Đang trực tuyến'
    : status === 'connecting'
      ? 'Đang kết nối'
      : 'Mất kết nối';
}

function updatePanel(state: MatchState): void {
  setText('#match-status', state.status === 'RUNNING' ? 'Đang diễn ra' : 'Đã kết thúc');
  setText('#revision', String(state.revision));
  setText('#winner', state.winnerTeamId ?? '—');
  const forces = document.querySelector<HTMLElement>('#forces');
  if (!forces) {
    return;
  }
  forces.classList.remove('empty');
  forces.innerHTML = state.teams.map((team) => {
    const headquarters = state.headquarters.find((item) => item.teamId === team.id);
    const units = state.units.filter((unit) => unit.teamId === team.id);
    const unitRows = units.map((unit) => `
      <div class="force-stat"><span>${escapeHtml(unit.displayName)} <small>(${escapeHtml(unit.id)})</small></span><strong>${unit.hp} HP</strong></div>
    `).join('');
    return `
      <article class="team-card" style="--team-color: ${safeColor(team.color)}">
        <div class="team-heading">
          <span class="team-swatch"></span>
          <strong>${escapeHtml(team.displayName)} · ${team.resources} res</strong>
        </div>
        <div class="force-stat"><span>HQ</span><strong>${headquarters?.hp ?? 0} HP</strong></div>
        ${unitRows}
      </article>
    `;
  }).join('');
}

function updateLog(): void {
  const log = document.querySelector<HTMLOListElement>('#command-log');
  if (!log) {
    return;
  }
  if (events.length === 0) {
    log.innerHTML = '<li class="empty">Chưa có command.</li>';
    return;
  }
  log.innerHTML = events.map((event) => `
    <li>
      <span class="log-sequence">#${event.sequence}</span>
      <div><strong>${escapeHtml(event.actorTeamId)} · ${escapeHtml(event.action)}</strong>
      <small>${escapeHtml(event.unitId)}${event.targetId ? ` → ${escapeHtml(event.targetId)}` : ''}</small></div>
    </li>
  `).join('');
}

function setText(selector: string, value: string): void {
  const element = document.querySelector<HTMLElement>(selector);
  if (element) {
    element.textContent = value;
  }
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function safeColor(value: string): string {
  return /^#[0-9a-fA-F]{6}$/.test(value) ? value : '#64748b';
}
