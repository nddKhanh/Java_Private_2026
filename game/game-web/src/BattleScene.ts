import Phaser from 'phaser';
import { botLight, coordinateStep, hpColor, HpScale, REJECT_FLASH_MS, type BotLight } from './boardStatus';
import { modelDataUrl, modelKey, type ModelKind } from './models';
import { BOARD, FONT_FAMILY, FONT_WEIGHTS, hexNumber, PALETTE } from './theme';
import type { MatchState, QueuedCommand, Team } from './types';

/** Board geometry of the last render, used to turn a click into a map cell. */
interface BoardLayout {
  originX: number;
  originY: number;
  cellSize: number;
}

/** What the viewer's own connection adds on top of the shared match state. */
export interface ViewerOverlay {
  viewerTeamId: string | null;
  queues: Record<string, QueuedCommand[]>;
}

/** One entity as the board draws it: model, footprint in cells, health and owner. */
interface Drawable {
  id: string;
  teamId: string | null;
  kind: ModelKind;
  hpKind: string | null;
  hp: number;
  x: number;
  y: number;
  width: number;
  height: number;
  scale: number;
  flip: boolean;
  /** Takes commands, so it shows a status light for its own team. */
  commandable: boolean;
  /** Resource amount shown next to a resource node. */
  amount?: number;
}

const LIGHT_COLORS: Record<BotLight, { fill: string; glow: boolean }> = {
  idle: { fill: '#334155', glow: false },
  busy: { fill: PALETTE.success, glow: true },
  rejected: { fill: PALETTE.warning, glow: true },
};

export class BattleScene extends Phaser.Scene {
  private latestState: MatchState | null = null;
  private overlay: ViewerOverlay = { viewerTeamId: null, queues: {} };
  private layout: BoardLayout | null = null;
  private selectedId: string | null = null;
  private selectListener: (id: string | null) => void = () => {};
  private readonly hpScale = new HpScale();
  private readonly rejectedAt = new Map<string, number>();
  private readonly pendingTextures = new Set<string>();
  private redrawQueued = false;

  constructor() {
    super('battle');
  }

  create(): void {
    this.renderWaitingState();
    // Text objects add canvas textures too, so only model textures trigger a redraw, batched into one per frame.
    this.textures.on(Phaser.Textures.Events.ADD, (key: string) => {
      if (!this.pendingTextures.delete(key) || this.redrawQueued) return;
      this.redrawQueued = true;
      this.time.delayedCall(0, () => {
        this.redrawQueued = false;
        this.rerender();
      });
    });
    // Phaser draws text on a canvas, which only uses the bundled font once the browser has loaded it.
    void Promise.all(FONT_WEIGHTS.map((weight) => document.fonts.load(`${weight} 16px "Be Vietnam Pro"`, 'Đội Gỗ')))
      .catch(() => undefined)
      .then(() => (this.latestState ? this.rerender() : this.renderWaitingState()));
    if (this.latestState) this.rerender();
    this.input.on('pointerdown', (pointer: Phaser.Input.Pointer) => this.selectAt(pointer.worldX, pointer.worldY));
  }

  /** Called with the unit, headquarters or building id under a click, or null when the click hits empty ground. */
  onSelect(listener: (id: string | null) => void): void {
    this.selectListener = listener;
  }

  setSelected(id: string | null): void {
    this.selectedId = id;
    this.rerender();
  }

  updateSnapshot(state: MatchState, overlay: ViewerOverlay): void {
    this.latestState = state;
    this.overlay = overlay;
    this.rerender();
  }

  /** Turns the status light of these bots orange for a moment. */
  markRejected(subjectIds: string[]): void {
    if (subjectIds.length === 0) return;
    const now = Date.now();
    subjectIds.forEach((id) => this.rejectedAt.set(id, now));
    if (this.scene.isActive()) this.time.delayedCall(REJECT_FLASH_MS + 50, () => this.rerender());
  }

  private rerender(): void {
    if (this.latestState && this.scene.isActive()) this.renderSnapshot(this.latestState);
  }

  private selectAt(worldX: number, worldY: number): void {
    const state = this.latestState;
    const layout = this.layout;
    if (!state || !layout) return;
    const x = Math.floor((worldX - layout.originX) / layout.cellSize);
    const y = Math.floor((worldY - layout.originY) / layout.cellSize);
    const unit = state.units.find((item) => item.hp > 0 && x >= item.position.x && x < item.position.x + item.width
      && y >= item.position.y && y < item.position.y + item.height);
    const headquarters = state.headquarters.find((item) => item.hp > 0 && item.position.x === x && item.position.y === y);
    const building = state.buildings.find((item) => item.hp > 0 && item.position.x === x && item.position.y === y);
    this.selectListener(unit?.id ?? headquarters?.id ?? building?.id ?? null);
  }

  private renderWaitingState(): void {
    this.children.removeAll(true);
    this.add.text(this.scale.width / 2, this.scale.height / 2, 'Đang chờ dữ liệu trận từ server…', {
      color: PALETTE.muted,
      fontFamily: FONT_FAMILY,
      fontSize: '22px',
      fontStyle: '500',
    }).setOrigin(0.5);
  }

  private renderSnapshot(state: MatchState): void {
    this.children.removeAll(true);
    const boardPadding = 42;
    const cellSize = Math.min(
      (this.scale.width - boardPadding * 2) / state.mapWidth,
      (this.scale.height - boardPadding * 2) / state.mapHeight,
    );
    const originX = (this.scale.width - cellSize * state.mapWidth) / 2;
    const originY = (this.scale.height - cellSize * state.mapHeight) / 2;
    this.layout = { originX, originY, cellSize };
    // Small cells hide ID labels until the entity is hovered.
    const compact = cellSize < 28;
    const teams = new Map(state.teams.map((team) => [team.id, team]));

    this.drawGround(state, originX, originY, cellSize);
    this.drawCoordinates(state, originX, originY, cellSize);
    this.drawHeadquartersZones(state, teams, originX, originY, cellSize);

    const drawables = this.collectDrawables(state);
    drawables.forEach((item) => item.hpKind && this.hpScale.observe(item.hpKind, item.hp));
    this.ensureTextures(drawables, teams);
    const now = Date.now();
    for (const item of drawables) {
      this.drawEntity(item, teams, originX, originY, cellSize, compact, now);
    }

    // Large maps keep the amount in the hover label so the number does not cover the resource.
    for (const resource of compact ? [] : state.resourceNodes) {
      const center = this.cellCenter(originX, originY, cellSize, resource.position.x, resource.position.y);
      this.add.text(center.x + cellSize * 0.46, center.y + cellSize * 0.46, `${resource.amount}`, {
        color: resource.type === 'MEAT' ? '#fecaca' : '#d9f99d',
        fontFamily: FONT_FAMILY,
        fontSize: `${Math.max(9, Math.min(13, cellSize * 0.3))}px`,
        fontStyle: '800',
        stroke: PALETTE.outline,
        strokeThickness: 3,
      }).setOrigin(1, 1).setDepth(12);
    }

    for (const projectile of state.projectiles) {
      const color = this.teamColor(teams.get(projectile.teamId));
      const center = this.cellCenter(originX, originY, cellSize, projectile.position.x, projectile.position.y);
      const key = modelKey('PROJECTILE', color);
      if (this.textures.exists(key)) {
        this.add.image(center.x, center.y, key).setDisplaySize(cellSize * 0.55, cellSize * 0.55).setDepth(15);
      }
    }
    this.drawSelection(drawables, originX, originY, cellSize);
  }

  /** Units, headquarters, buildings and resources in drawing order, back to front. */
  private collectDrawables(state: MatchState): Drawable[] {
    const resources: Drawable[] = state.resourceNodes.map((item) => ({
      id: item.id, teamId: null, kind: item.type, hpKind: null, hp: 1, amount: item.amount,
      x: item.position.x, y: item.position.y, width: 1, height: 1, scale: 0.92, flip: false, commandable: false,
    }));
    const buildings: Drawable[] = state.buildings.map((item) => ({
      id: item.id, teamId: item.teamId, kind: item.type, hpKind: `building:${item.type}`, hp: item.hp,
      x: item.position.x, y: item.position.y, width: 1, height: 1, scale: 1.02, flip: false,
      commandable: item.type === 'BARRACKS',
    }));
    const headquarters: Drawable[] = state.headquarters.map((item) => ({
      id: item.id, teamId: item.teamId, kind: 'HQ', hpKind: 'hq', hp: item.hp,
      x: item.position.x, y: item.position.y, width: 1, height: 1, scale: 1.18, flip: false, commandable: true,
    }));
    const units: Drawable[] = state.units.map((item) => ({
      id: item.id, teamId: item.teamId, kind: this.unitKind(item.type), hpKind: `unit:${item.type}`, hp: item.hp,
      x: item.position.x, y: item.position.y, width: item.width, height: item.height, scale: 1.04,
      flip: item.facing === 'LEFT', commandable: true,
    }));
    return [...resources, ...buildings, ...headquarters, ...units];
  }

  private unitKind(type: string): ModelKind {
    return type === 'MELEE' || type === 'RANGED' || type === 'SIEGE' ? type : 'WORKER';
  }

  /** Rasterizes each model the first time a team color needs it; the board redraws once it is ready. */
  private ensureTextures(drawables: Drawable[], teams: Map<string, Team>): void {
    const needed = new Map<string, string>();
    for (const item of drawables) {
      const color = item.teamId ? this.teamColor(teams.get(item.teamId)) : PALETTE.subtle;
      needed.set(modelKey(item.kind, color), modelDataUrl(item.kind, color));
    }
    for (const team of teams.values()) {
      const color = this.teamColor(team);
      needed.set(modelKey('PROJECTILE', color), modelDataUrl('PROJECTILE', color));
    }
    for (const [key, url] of needed) {
      if (this.textures.exists(key) || this.pendingTextures.has(key)) continue;
      this.pendingTextures.add(key);
      this.textures.addBase64(key, url);
    }
  }

  private drawEntity(
    item: Drawable,
    teams: Map<string, Team>,
    originX: number,
    originY: number,
    cellSize: number,
    compact: boolean,
    now: number,
  ): void {
    const left = originX + item.x * cellSize;
    const top = originY + item.y * cellSize;
    const width = item.width * cellSize;
    const height = item.height * cellSize;
    const centerX = left + width / 2;
    const centerY = top + height / 2;
    const alive = item.hp > 0;
    const color = item.teamId ? this.teamColor(teams.get(item.teamId)) : PALETTE.subtle;
    const key = modelKey(item.kind, color);
    if (!this.textures.exists(key)) return;

    const image = this.add.image(centerX, centerY, key)
      .setDisplaySize(width * item.scale, height * item.scale)
      .setFlipX(item.flip)
      .setAlpha(alive ? 1 : 0.28)
      .setDepth(item.teamId ? 5 : 2);
    if (!item.hpKind) {
      const resource = item.kind === 'MEAT' ? 'Thịt' : 'Gỗ';
      if (compact) this.addIdLabel(image, centerX, top, item.id, `${item.id} · ${resource} ${item.amount ?? 0}`, width, true);
      return;
    }

    const max = this.hpScale.max(item.hpKind, item.hp);
    if (alive) this.drawHealthBar(left, top + height, width, cellSize, this.hpScale.ratio(item.hpKind, item.hp));
    if (alive && item.commandable && item.teamId !== null && item.teamId === this.overlay.viewerTeamId) {
      const light = botLight(this.overlay.queues[item.id]?.length ?? 0, this.rejectedAt.get(item.id), now);
      this.drawStatusLight(left + width, top, cellSize, light);
    }
    this.addIdLabel(image, centerX, top + Math.min(height * 0.2, cellSize * 0.2), item.id,
      `${item.id} · ${Math.max(0, item.hp)}/${max} HP`, width * 0.9, compact);
  }

  private drawHealthBar(left: number, bottom: number, width: number, cellSize: number, ratio: number): void {
    const barWidth = width * 0.78;
    const barHeight = Math.max(3, cellSize * 0.1);
    const x = left + (width - barWidth) / 2;
    const y = bottom - barHeight - Math.max(1, cellSize * 0.04);
    const bar = this.add.graphics().setDepth(18);
    bar.fillStyle(hexNumber(PALETTE.outline), 0.9);
    bar.fillRoundedRect(x - 1, y - 1, barWidth + 2, barHeight + 2, (barHeight + 2) / 2);
    bar.fillStyle(hexNumber(hpColor(ratio)), 1);
    if (ratio > 0) bar.fillRoundedRect(x, y, Math.max(barHeight, barWidth * ratio), barHeight, barHeight / 2);
  }

  /** The bot's "eye": dark when idle, green while commands are queued, orange right after a rejection. */
  private drawStatusLight(right: number, top: number, cellSize: number, light: BotLight): void {
    const radius = Math.max(2.5, cellSize * 0.1);
    const x = right - radius - Math.max(1, cellSize * 0.04);
    const y = top + radius + Math.max(1, cellSize * 0.04);
    const { fill, glow } = LIGHT_COLORS[light];
    const graphics = this.add.graphics().setDepth(19);
    if (glow) {
      graphics.fillStyle(hexNumber(fill), 0.35);
      graphics.fillCircle(x, y, radius * 1.9);
    }
    graphics.fillStyle(hexNumber(fill), 1);
    graphics.fillCircle(x, y, radius);
    graphics.lineStyle(Math.max(1, radius * 0.35), hexNumber(light === 'idle' ? PALETTE.muted : PALETTE.outline), 1);
    graphics.strokeCircle(x, y, radius);
  }

  /** Checkered ground with faint cell lines and a stronger line every five cells. */
  private drawGround(state: MatchState, originX: number, originY: number, cellSize: number): void {
    const boardWidth = cellSize * state.mapWidth;
    const boardHeight = cellSize * state.mapHeight;
    const ground = this.add.graphics();
    ground.fillStyle(hexNumber(BOARD.frame), 1);
    ground.fillRoundedRect(originX - 12, originY - 12, boardWidth + 24, boardHeight + 24, 14);
    ground.fillStyle(hexNumber(BOARD.groundLight), 1);
    ground.fillRect(originX, originY, boardWidth, boardHeight);
    ground.fillStyle(hexNumber(BOARD.groundDark), 1);
    for (let y = 0; y < state.mapHeight; y += 1) {
      for (let x = (y % 2); x < state.mapWidth; x += 2) {
        ground.fillRect(originX + x * cellSize, originY + y * cellSize, cellSize, cellSize);
      }
    }
    if (cellSize >= 10) {
      ground.lineStyle(1, hexNumber(BOARD.gridMinor), 0.55);
      for (let x = 1; x < state.mapWidth; x += 1) {
        if (x % 5 !== 0) ground.lineBetween(originX + x * cellSize, originY, originX + x * cellSize, originY + boardHeight);
      }
      for (let y = 1; y < state.mapHeight; y += 1) {
        if (y % 5 !== 0) ground.lineBetween(originX, originY + y * cellSize, originX + boardWidth, originY + y * cellSize);
      }
    }
    ground.lineStyle(1.5, hexNumber(BOARD.gridMajor), 0.8);
    for (let x = 5; x < state.mapWidth; x += 5) {
      ground.lineBetween(originX + x * cellSize, originY, originX + x * cellSize, originY + boardHeight);
    }
    for (let y = 5; y < state.mapHeight; y += 5) {
      ground.lineBetween(originX, originY + y * cellSize, originX + boardWidth, originY + y * cellSize);
    }
    ground.lineStyle(2, hexNumber(BOARD.gridMajor), 1);
    ground.strokeRect(originX, originY, boardWidth, boardHeight);
  }

  /** Column numbers above the board and row numbers to its left, so players can count cells for MOVE. */
  private drawCoordinates(state: MatchState, originX: number, originY: number, cellSize: number): void {
    const step = coordinateStep(cellSize);
    const style = {
      color: BOARD.coordinate,
      fontFamily: FONT_FAMILY,
      fontSize: `${Math.max(10, Math.min(13, cellSize * 0.42))}px`,
      fontStyle: '700',
    };
    for (let x = 0; x < state.mapWidth; x += step) {
      this.add.text(originX + (x + 0.5) * cellSize, originY - 16, `${x}`, style).setOrigin(0.5, 1);
    }
    for (let y = 0; y < state.mapHeight; y += step) {
      this.add.text(originX - 16, originY + (y + 0.5) * cellSize, `${y}`, style).setOrigin(1, 0.5);
    }
  }

  /** Tints the 3×3 area around each living headquarters in its team color. */
  private drawHeadquartersZones(
    state: MatchState,
    teams: Map<string, Team>,
    originX: number,
    originY: number,
    cellSize: number,
  ): void {
    const zones = this.add.graphics().setDepth(1);
    for (const headquarters of state.headquarters) {
      if (headquarters.hp <= 0) continue;
      const color = hexNumber(this.teamColor(teams.get(headquarters.teamId)));
      const minX = Math.max(0, headquarters.position.x - 1);
      const minY = Math.max(0, headquarters.position.y - 1);
      const maxX = Math.min(state.mapWidth, headquarters.position.x + 2);
      const maxY = Math.min(state.mapHeight, headquarters.position.y + 2);
      const x = originX + minX * cellSize;
      const y = originY + minY * cellSize;
      const width = (maxX - minX) * cellSize;
      const height = (maxY - minY) * cellSize;
      zones.fillStyle(color, 0.12);
      zones.fillRoundedRect(x, y, width, height, Math.min(10, cellSize * 0.3));
      zones.lineStyle(1.5, color, 0.55);
      zones.strokeRoundedRect(x, y, width, height, Math.min(10, cellSize * 0.3));
    }
  }

  private drawSelection(drawables: Drawable[], originX: number, originY: number, cellSize: number): void {
    const selected = this.selectedId ? drawables.find((item) => item.id === this.selectedId) : undefined;
    if (!selected) return;
    const outline = this.add.graphics().setDepth(30);
    outline.lineStyle(Math.max(2, cellSize * 0.08), hexNumber(PALETTE.accent), 1);
    outline.strokeRoundedRect(originX + selected.x * cellSize, originY + selected.y * cellSize,
      selected.width * cellSize, selected.height * cellSize, Math.min(8, cellSize * 0.2));
  }

  /** ID label over the entity; hovering shows the full ID with its HP. Small cells show it only on hover. */
  private addIdLabel(
    target: Phaser.GameObjects.Image,
    x: number,
    y: number,
    id: string,
    detail: string,
    availableWidth: number,
    compact: boolean,
  ): void {
    const maxCharacters = Math.max(7, Math.floor(availableWidth / 6));
    const compactId = id.length > maxCharacters ? `${id.slice(0, maxCharacters - 1)}…` : id;
    const label = this.add.text(x, y, compact ? detail : compactId, {
      align: 'center',
      backgroundColor: '#020617cc',
      color: PALETTE.textStrong,
      fontFamily: FONT_FAMILY,
      fontSize: `${Math.max(9, Math.min(12, availableWidth * 0.16))}px`,
      fontStyle: '700',
      padding: { x: 4, y: 1 },
    }).setOrigin(0.5).setDepth(20).setInteractive();
    if (compact) {
      label.setVisible(false).setDepth(40);
      target.setInteractive();
      target.on('pointerover', () => label.setVisible(true));
      target.on('pointerout', () => label.setVisible(false));
    } else {
      const show = () => label.setText(detail).setDepth(40);
      const hide = () => label.setText(compactId).setDepth(20);
      target.setInteractive();
      for (const source of [label, target]) {
        source.on('pointerover', show);
        source.on('pointerout', hide);
      }
    }
  }

  private cellCenter(originX: number, originY: number, cellSize: number, x: number, y: number): Phaser.Math.Vector2 {
    return new Phaser.Math.Vector2(originX + (x + 0.5) * cellSize, originY + (y + 0.5) * cellSize);
  }

  private teamColor(team: Team | undefined): string {
    return team?.color && /^#[0-9a-fA-F]{6}$/.test(team.color) ? team.color.toLowerCase() : PALETTE.subtle;
  }
}
