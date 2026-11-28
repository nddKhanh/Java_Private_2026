import Phaser from 'phaser';
import {
  BASE_CANVAS, botLight, clampScroll, clampZoom, coordinateStep, hpColor, hpRatio, maxZoom, REJECT_FLASH_MS,
  screenAt, scrollToKeep, worldAt, type BotLight,
} from './boardStatus';
import { modelDataUrl, modelKey, type ModelKind } from './models';
import { BOARD, FONT_FAMILY, FONT_WEIGHTS, hexNumber, PALETTE } from './theme';
import type { MatchState, QueuedCommand, Team } from './types';

/** Board geometry of the last render in world coordinates. */
interface BoardLayout {
  originX: number;
  originY: number;
  cellSize: number;
  columns: number;
  rows: number;
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
  hp: number;
  /** Null for resources, which have no health. */
  maxHp: number | null;
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

interface CoordinateLabel {
  axis: 'column' | 'row';
  index: number;
  text: Phaser.GameObjects.Text;
}

const LIGHT_COLORS: Record<BotLight, { fill: string; glow: boolean }> = {
  idle: { fill: '#334155', glow: false },
  busy: { fill: PALETTE.success, glow: true },
  rejected: { fill: PALETTE.warning, glow: true },
};

/** Widest a cell may get on screen when zoomed in, in base-canvas pixels. */
const MAX_CELL_PIXELS = 220;
/** Mouse movement, in base-canvas pixels, before a press turns into dragging instead of a click. */
const DRAG_THRESHOLD = 6;
const WHEEL_STEP = 1.2;

export class BattleScene extends Phaser.Scene {
  private latestState: MatchState | null = null;
  private overlay: ViewerOverlay = { viewerTeamId: null, queues: {} };
  private layout: BoardLayout | null = null;
  private selectedId: string | null = null;
  private selectListener: (id: string | null) => void = () => {};
  private zoomListener: (zoom: number, max: number) => void = () => {};
  private readonly rejectedAt = new Map<string, number>();
  private readonly pendingTextures = new Set<string>();
  private redrawQueued = false;
  private zoom = 1;
  /** Draws coordinate labels unzoomed on top of the board; the main camera draws the zoomable world. */
  private uiCamera: Phaser.Cameras.Scene2D.Camera | null = null;
  private uiObjects: Phaser.GameObjects.GameObject[] = [];
  private coordinateLabels: CoordinateLabel[] = [];
  private coordinateBands: Phaser.GameObjects.Graphics | null = null;
  private drag: { x: number; y: number; scrollX: number; scrollY: number; moved: boolean } | null = null;

  constructor() {
    super('battle');
  }

  /** Canvas pixels per base-layout pixel; the canvas grows with the screen's pixel density. */
  private get ui(): number {
    return this.scale.width / BASE_CANVAS;
  }

  create(): void {
    this.uiCamera = this.cameras.add(0, 0, this.scale.width, this.scale.height);
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
    this.bindPointer();
  }

  /** Called with the unit, headquarters or building id under a click, or null when the click hits empty ground. */
  onSelect(listener: (id: string | null) => void): void {
    this.selectListener = listener;
  }

  /** Called with the zoom level and its upper limit whenever either changes. */
  onZoom(listener: (zoom: number, max: number) => void): void {
    this.zoomListener = listener;
  }

  setSelected(id: string | null): void {
    this.selectedId = id;
    this.rerender();
  }

  updateSnapshot(state: MatchState, overlay: ViewerOverlay): void {
    const resized = !this.latestState || this.latestState.mapWidth !== state.mapWidth
      || this.latestState.mapHeight !== state.mapHeight;
    this.latestState = state;
    this.overlay = overlay;
    this.rerender();
    if (resized) this.zoomListener(this.zoom, this.zoomLimit());
  }

  /** Turns the status light of these bots orange for a moment. */
  markRejected(subjectIds: string[]): void {
    if (subjectIds.length === 0) return;
    const now = Date.now();
    subjectIds.forEach((id) => this.rejectedAt.set(id, now));
    if (this.scene.isActive()) this.time.delayedCall(REJECT_FLASH_MS + 50, () => this.rerender());
  }

  /** Zooms around the middle of the view; used by the zoom buttons. */
  zoomBy(factor: number): void {
    this.zoomAt(factor, this.scale.width / 2, this.scale.height / 2);
  }

  /** Back to the whole board. */
  resetZoom(): void {
    this.applyZoom(1, 0, 0);
  }

  private bindPointer(): void {
    const camera = this.cameras.main;
    this.input.on('pointerdown', (pointer: Phaser.Input.Pointer) => {
      this.drag = { x: pointer.x, y: pointer.y, scrollX: camera.scrollX, scrollY: camera.scrollY, moved: false };
    });
    this.input.on('pointermove', (pointer: Phaser.Input.Pointer) => {
      const drag = this.drag;
      if (!drag || !pointer.isDown) return;
      const dx = pointer.x - drag.x;
      const dy = pointer.y - drag.y;
      if (!drag.moved && Math.hypot(dx, dy) < DRAG_THRESHOLD * this.ui) return;
      drag.moved = true;
      camera.setScroll(
        clampScroll(drag.scrollX - dx / this.zoom, this.scale.width, this.zoom),
        clampScroll(drag.scrollY - dy / this.zoom, this.scale.height, this.zoom),
      );
      this.positionCoordinates();
    });
    this.input.on('pointerup', (pointer: Phaser.Input.Pointer) => {
      const drag = this.drag;
      this.drag = null;
      if (!drag || drag.moved) return;
      this.selectAt(
        worldAt(pointer.x, this.scale.width, this.zoom, camera.scrollX),
        worldAt(pointer.y, this.scale.height, this.zoom, camera.scrollY),
      );
    });
    this.input.on('wheel', (pointer: Phaser.Input.Pointer, _over: unknown, _dx: number, dy: number) => {
      if (dy !== 0) this.zoomAt(dy < 0 ? WHEEL_STEP : 1 / WHEEL_STEP, pointer.x, pointer.y);
    });
  }

  private zoomLimit(): number {
    return this.layout ? maxZoom(this.layout.cellSize, MAX_CELL_PIXELS * this.ui) : 1;
  }

  /** Zooms while keeping the board point under (screenX, screenY) in place. */
  private zoomAt(factor: number, screenX: number, screenY: number): void {
    const next = clampZoom(this.zoom * factor, this.zoomLimit());
    if (Math.abs(next - this.zoom) < 1e-6) return;
    const camera = this.cameras.main;
    const worldX = worldAt(screenX, this.scale.width, this.zoom, camera.scrollX);
    const worldY = worldAt(screenY, this.scale.height, this.zoom, camera.scrollY);
    this.applyZoom(
      next,
      scrollToKeep(worldX, screenX, this.scale.width, next),
      scrollToKeep(worldY, screenY, this.scale.height, next),
    );
  }

  private applyZoom(zoom: number, scrollX: number, scrollY: number): void {
    this.zoom = zoom;
    this.cameras.main.setZoom(zoom).setScroll(scrollX, scrollY);
    this.rerender();
    this.zoomListener(zoom, this.zoomLimit());
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

  /** Font size in world pixels that shows as `screenPixels` base pixels at the current zoom. */
  private fontPx(screenPixels: number): string {
    return `${((screenPixels * this.ui) / this.zoom).toFixed(2)}px`;
  }

  /** Length in world pixels that shows as `screenPixels` base pixels at the current zoom. */
  private screenLength(screenPixels: number): number {
    return (screenPixels * this.ui) / this.zoom;
  }

  /** Text rendered at the zoom level so it stays sharp when zoomed in. */
  private get textResolution(): number {
    return Math.min(4, Math.max(1, this.zoom));
  }

  private renderWaitingState(): void {
    this.children.removeAll(true);
    this.add.text(this.scale.width / 2, this.scale.height / 2, 'Đang chờ dữ liệu trận từ server…', {
      color: PALETTE.muted,
      fontFamily: FONT_FAMILY,
      fontSize: `${22 * this.ui}px`,
      fontStyle: '500',
    }).setOrigin(0.5);
  }

  private renderSnapshot(state: MatchState): void {
    this.children.removeAll(true);
    this.uiObjects = [];
    this.coordinateLabels = [];
    this.coordinateBands = null;
    const boardPadding = 42 * this.ui;
    const cellSize = Math.min(
      (this.scale.width - boardPadding * 2) / state.mapWidth,
      (this.scale.height - boardPadding * 2) / state.mapHeight,
    );
    const originX = (this.scale.width - cellSize * state.mapWidth) / 2;
    const originY = (this.scale.height - cellSize * state.mapHeight) / 2;
    this.layout = { originX, originY, cellSize, columns: state.mapWidth, rows: state.mapHeight };
    // Cells that look small on screen hide ID labels until the entity is hovered.
    const cellOnScreen = (cellSize * this.zoom) / this.ui;
    const compact = cellOnScreen < 28;
    const teams = new Map(state.teams.map((team) => [team.id, team]));

    this.drawGround(state, originX, originY, cellSize, cellOnScreen);
    this.drawHeadquartersZones(state, teams, originX, originY, cellSize);

    const drawables = this.collectDrawables(state);
    this.ensureTextures(drawables, teams);
    const now = Date.now();
    for (const item of drawables) {
      this.drawEntity(item, teams, originX, originY, cellSize, compact, now);
    }

    // Small cells keep the amount in the hover label so the number does not cover the resource.
    for (const resource of compact ? [] : state.resourceNodes) {
      const right = originX + (resource.position.x + 1) * cellSize - this.screenLength(2);
      const bottom = originY + (resource.position.y + 1) * cellSize - this.screenLength(1);
      this.add.text(right, bottom, `${resource.amount}`, {
        color: resource.type === 'MEAT' ? '#fecaca' : '#d9f99d',
        fontFamily: FONT_FAMILY,
        fontSize: this.fontPx(Math.max(9, Math.min(14, cellOnScreen * 0.3))),
        fontStyle: '800',
        stroke: PALETTE.outline,
        strokeThickness: this.screenLength(3),
        resolution: this.textResolution,
      }).setOrigin(1, 1).setDepth(12);
    }

    for (const projectile of state.projectiles) {
      const color = this.teamColor(teams.get(projectile.teamId));
      const key = modelKey('PROJECTILE', color);
      if (!this.textures.exists(key)) continue;
      this.add.image(
        originX + (projectile.position.x + 0.5) * cellSize,
        originY + (projectile.position.y + 0.5) * cellSize,
        key,
      ).setDisplaySize(cellSize * 0.55, cellSize * 0.55).setDepth(15);
    }
    this.drawSelection(drawables, originX, originY, cellSize);
    this.drawCoordinates(state, cellOnScreen);
    this.splitCameras();
  }

  /** The main camera zooms the board; the UI camera draws the coordinate labels at a fixed size on top. */
  private splitCameras(): void {
    const ui = new Set(this.uiObjects);
    this.cameras.main.ignore(this.uiObjects);
    this.uiCamera?.ignore(this.children.list.filter((child) => !ui.has(child)));
  }

  /** Units, headquarters, buildings and resources in drawing order, back to front. */
  private collectDrawables(state: MatchState): Drawable[] {
    const resources: Drawable[] = state.resourceNodes.map((item) => ({
      id: item.id, teamId: null, kind: item.type, hp: 1, maxHp: null, amount: item.amount,
      x: item.position.x, y: item.position.y, width: 1, height: 1, scale: 0.92, flip: false, commandable: false,
    }));
    const buildings: Drawable[] = state.buildings.map((item) => ({
      id: item.id, teamId: item.teamId, kind: item.type, hp: item.hp, maxHp: item.maxHp,
      x: item.position.x, y: item.position.y, width: 1, height: 1, scale: 1.02, flip: false,
      commandable: item.type === 'BARRACKS',
    }));
    const headquarters: Drawable[] = state.headquarters.map((item) => ({
      id: item.id, teamId: item.teamId, kind: 'HQ', hp: item.hp, maxHp: item.maxHp,
      x: item.position.x, y: item.position.y, width: 1, height: 1, scale: 1.18, flip: false, commandable: true,
    }));
    const units: Drawable[] = state.units.map((item) => ({
      id: item.id, teamId: item.teamId, kind: this.unitKind(item.type), hp: item.hp, maxHp: item.maxHp,
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
    const alive = item.hp > 0;
    const color = item.teamId ? this.teamColor(teams.get(item.teamId)) : PALETTE.subtle;
    const key = modelKey(item.kind, color);
    if (!this.textures.exists(key)) return;

    const image = this.add.image(centerX, top + height / 2, key)
      .setDisplaySize(width * item.scale, height * item.scale)
      .setFlipX(item.flip)
      .setAlpha(alive ? 1 : 0.28)
      .setDepth(item.teamId ? 5 : 2);
    const labelY = top + Math.min(height * 0.2, cellSize * 0.2);
    if (item.maxHp === null) {
      const resource = item.kind === 'MEAT' ? 'Thịt' : 'Gỗ';
      if (compact) this.addIdLabel(image, centerX, labelY, item.id, `${item.id} · ${resource} ${item.amount ?? 0}`, width, true);
      return;
    }

    if (alive) this.drawHealthBar(left, top + height, width, cellSize, hpRatio(item.hp, item.maxHp));
    if (alive && item.commandable && item.teamId !== null && item.teamId === this.overlay.viewerTeamId) {
      const light = botLight(this.overlay.queues[item.id]?.length ?? 0, this.rejectedAt.get(item.id), now);
      this.drawStatusLight(left + width, top, cellSize, light);
    }
    this.addIdLabel(image, centerX, labelY, item.id, `${item.id} · ${Math.max(0, item.hp)}/${item.maxHp} HP`,
      width, compact);
  }

  /** Size proportional to the cell, kept between two on-screen sizes so it stays readable at every zoom. */
  private cellPart(cellSize: number, share: number, minScreen: number, maxScreen: number): number {
    return Math.max(this.screenLength(minScreen), Math.min(cellSize * share, this.screenLength(maxScreen)));
  }

  private drawHealthBar(left: number, bottom: number, width: number, cellSize: number, ratio: number): void {
    const barWidth = width * 0.78;
    const barHeight = this.cellPart(cellSize, 0.1, 3, 7);
    const border = this.screenLength(1);
    const x = left + (width - barWidth) / 2;
    const y = bottom - barHeight - Math.max(border, cellSize * 0.04);
    const bar = this.add.graphics().setDepth(18);
    bar.fillStyle(hexNumber(PALETTE.outline), 0.9);
    bar.fillRoundedRect(x - border, y - border, barWidth + border * 2, barHeight + border * 2, barHeight / 2 + border);
    bar.fillStyle(hexNumber(hpColor(ratio)), 1);
    if (ratio > 0) bar.fillRoundedRect(x, y, Math.max(barHeight, barWidth * ratio), barHeight, barHeight / 2);
  }

  /** The bot's "eye": dark when idle, green while commands are queued, orange right after a rejection. */
  private drawStatusLight(right: number, top: number, cellSize: number, light: BotLight): void {
    const radius = this.cellPart(cellSize, 0.1, 2.5, 6);
    const inset = Math.max(this.screenLength(1), cellSize * 0.04);
    const x = right - radius - inset;
    const y = top + radius + inset;
    const { fill, glow } = LIGHT_COLORS[light];
    const graphics = this.add.graphics().setDepth(19);
    if (glow) {
      graphics.fillStyle(hexNumber(fill), 0.35);
      graphics.fillCircle(x, y, radius * 1.9);
    }
    graphics.fillStyle(hexNumber(fill), 1);
    graphics.fillCircle(x, y, radius);
    graphics.lineStyle(radius * 0.35, hexNumber(light === 'idle' ? PALETTE.muted : PALETTE.outline), 1);
    graphics.strokeCircle(x, y, radius);
  }

  /** Checkered ground with faint cell lines and a stronger line every five cells; lines keep their screen width. */
  private drawGround(state: MatchState, originX: number, originY: number, cellSize: number, cellOnScreen: number): void {
    const boardWidth = cellSize * state.mapWidth;
    const boardHeight = cellSize * state.mapHeight;
    const frame = 12 * this.ui;
    const ground = this.add.graphics();
    ground.fillStyle(hexNumber(BOARD.frame), 1);
    ground.fillRoundedRect(originX - frame, originY - frame, boardWidth + frame * 2, boardHeight + frame * 2, 14 * this.ui);
    ground.fillStyle(hexNumber(BOARD.groundLight), 1);
    ground.fillRect(originX, originY, boardWidth, boardHeight);
    ground.fillStyle(hexNumber(BOARD.groundDark), 1);
    for (let y = 0; y < state.mapHeight; y += 1) {
      for (let x = (y % 2); x < state.mapWidth; x += 2) {
        ground.fillRect(originX + x * cellSize, originY + y * cellSize, cellSize, cellSize);
      }
    }
    if (cellOnScreen >= 10) {
      ground.lineStyle(this.screenLength(1), hexNumber(BOARD.gridMinor), 0.55);
      for (let x = 1; x < state.mapWidth; x += 1) {
        if (x % 5 !== 0) ground.lineBetween(originX + x * cellSize, originY, originX + x * cellSize, originY + boardHeight);
      }
      for (let y = 1; y < state.mapHeight; y += 1) {
        if (y % 5 !== 0) ground.lineBetween(originX, originY + y * cellSize, originX + boardWidth, originY + y * cellSize);
      }
    }
    ground.lineStyle(this.screenLength(1.5), hexNumber(BOARD.gridMajor), 0.8);
    for (let x = 5; x < state.mapWidth; x += 5) {
      ground.lineBetween(originX + x * cellSize, originY, originX + x * cellSize, originY + boardHeight);
    }
    for (let y = 5; y < state.mapHeight; y += 5) {
      ground.lineBetween(originX, originY + y * cellSize, originX + boardWidth, originY + y * cellSize);
    }
    ground.lineStyle(this.screenLength(2), hexNumber(BOARD.gridMajor), 1);
    ground.strokeRect(originX, originY, boardWidth, boardHeight);
  }

  /**
   * Column numbers above the board and row numbers to its left, so players can count cells for MOVE.
   * They live on the UI camera and follow the visible cells, staying on screen while the board is zoomed.
   */
  private drawCoordinates(state: MatchState, cellOnScreen: number): void {
    const step = coordinateStep(cellOnScreen);
    const style = {
      color: BOARD.coordinate,
      fontFamily: FONT_FAMILY,
      fontSize: `${Math.max(10, Math.min(13, cellOnScreen * 0.42)) * this.ui}px`,
      fontStyle: '700',
    };
    this.coordinateBands = this.add.graphics().setDepth(50);
    this.uiObjects.push(this.coordinateBands);
    for (let x = 0; x < state.mapWidth; x += step) {
      const text = this.add.text(0, 0, `${x}`, style).setOrigin(0.5, 1).setDepth(51);
      this.coordinateLabels.push({ axis: 'column', index: x, text });
      this.uiObjects.push(text);
    }
    for (let y = 0; y < state.mapHeight; y += step) {
      const text = this.add.text(0, 0, `${y}`, style).setOrigin(1, 0.5).setDepth(51);
      this.coordinateLabels.push({ axis: 'row', index: y, text });
      this.uiObjects.push(text);
    }
    this.positionCoordinates();
  }

  /** Moves coordinate labels next to their cells after a zoom or drag; labels of off-screen cells hide. */
  private positionCoordinates(): void {
    const layout = this.layout;
    if (!layout) return;
    const camera = this.cameras.main;
    const width = this.scale.width;
    const height = this.scale.height;
    const toScreenX = (worldX: number) => screenAt(worldX, width, this.zoom, camera.scrollX);
    const toScreenY = (worldY: number) => screenAt(worldY, height, this.zoom, camera.scrollY);
    const boardLeft = toScreenX(layout.originX);
    const boardTop = toScreenY(layout.originY);
    const boardRight = toScreenX(layout.originX + layout.columns * layout.cellSize);
    const boardBottom = toScreenY(layout.originY + layout.rows * layout.cellSize);
    const band = 28 * this.ui;
    const columnY = Math.max(band - 6 * this.ui, boardTop - 14 * this.ui);
    const rowX = Math.max(band + 6 * this.ui, boardLeft - 14 * this.ui);

    const bands = this.coordinateBands;
    bands?.clear();
    if (bands && this.zoom > 1) {
      bands.fillStyle(hexNumber(BOARD.frame), 0.88);
      bands.fillRect(0, 0, width, band);
      bands.fillRect(0, band, band + 12 * this.ui, height - band);
    }
    for (const label of this.coordinateLabels) {
      const center = label.axis === 'column'
        ? toScreenX(layout.originX + (label.index + 0.5) * layout.cellSize)
        : toScreenY(layout.originY + (label.index + 0.5) * layout.cellSize);
      if (label.axis === 'column') {
        label.text.setPosition(center, columnY).setVisible(center >= Math.max(boardLeft, band) && center <= boardRight);
      } else {
        label.text.setPosition(rowX, center).setVisible(center >= Math.max(boardTop, band) && center <= boardBottom);
      }
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
      const radius = Math.min(10 * this.ui, cellSize * 0.3);
      zones.fillStyle(color, 0.12);
      zones.fillRoundedRect(x, y, width, height, radius);
      zones.lineStyle(this.screenLength(1.5), color, 0.55);
      zones.strokeRoundedRect(x, y, width, height, radius);
    }
  }

  private drawSelection(drawables: Drawable[], originX: number, originY: number, cellSize: number): void {
    const selected = this.selectedId ? drawables.find((item) => item.id === this.selectedId) : undefined;
    if (!selected) return;
    const outline = this.add.graphics().setDepth(30);
    outline.lineStyle(this.cellPart(cellSize, 0.08, 2, 4), hexNumber(PALETTE.accent), 1);
    outline.strokeRoundedRect(originX + selected.x * cellSize, originY + selected.y * cellSize,
      selected.width * cellSize, selected.height * cellSize, Math.min(8 * this.ui, cellSize * 0.2));
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
    const widthOnScreen = (availableWidth * this.zoom) / this.ui;
    const maxCharacters = Math.max(7, Math.floor(widthOnScreen / 6));
    const compactId = id.length > maxCharacters ? `${id.slice(0, maxCharacters - 1)}…` : id;
    const label = this.add.text(x, y, compact ? detail : compactId, {
      align: 'center',
      backgroundColor: '#020617cc',
      color: PALETTE.textStrong,
      fontFamily: FONT_FAMILY,
      fontSize: this.fontPx(Math.max(9, Math.min(13, widthOnScreen * 0.16))),
      fontStyle: '700',
      padding: { x: this.screenLength(4), y: this.screenLength(1) },
      resolution: this.textResolution,
    }).setOrigin(0.5).setDepth(20).setInteractive();
    target.setInteractive();
    if (compact) {
      label.setVisible(false).setDepth(40);
      target.on('pointerover', () => label.setVisible(true));
      target.on('pointerout', () => label.setVisible(false));
    } else {
      const show = () => label.setText(detail).setDepth(40);
      const hide = () => label.setText(compactId).setDepth(20);
      for (const source of [label, target]) {
        source.on('pointerover', show);
        source.on('pointerout', hide);
      }
    }
  }

  private teamColor(team: Team | undefined): string {
    return team?.color && /^#[0-9a-fA-F]{6}$/.test(team.color) ? team.color.toLowerCase() : PALETTE.subtle;
  }
}
