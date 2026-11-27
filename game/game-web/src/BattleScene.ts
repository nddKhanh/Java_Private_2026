import Phaser from 'phaser';
import type { MatchState, Team } from './types';

/** Board geometry of the last render, used to turn a click into a map cell. */
interface BoardLayout {
  originX: number;
  originY: number;
  cellSize: number;
}

export class BattleScene extends Phaser.Scene {
  private latestState: MatchState | null = null;
  private layout: BoardLayout | null = null;
  private selectedId: string | null = null;
  private selectListener: (id: string | null) => void = () => {};

  constructor() {
    super('battle');
  }

  create(): void {
    this.renderWaitingState();
    if (this.latestState) {
      this.renderSnapshot(this.latestState);
    }
    this.input.on('pointerdown', (pointer: Phaser.Input.Pointer) => this.selectAt(pointer.worldX, pointer.worldY));
  }

  /** Called with the unit, headquarters or building id under a click, or null when the click hits empty ground. */
  onSelect(listener: (id: string | null) => void): void {
    this.selectListener = listener;
  }

  setSelected(id: string | null): void {
    this.selectedId = id;
    if (this.latestState && this.scene.isActive()) {
      this.renderSnapshot(this.latestState);
    }
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

  private drawSelection(state: MatchState, originX: number, originY: number, cellSize: number): void {
    if (!this.selectedId) return;
    const unit = state.units.find((item) => item.id === this.selectedId);
    const fixed = state.headquarters.find((item) => item.id === this.selectedId)
      ?? state.buildings.find((item) => item.id === this.selectedId);
    const position = unit?.position ?? fixed?.position;
    if (!position) return;
    const width = unit?.width ?? 1;
    const height = unit?.height ?? 1;
    const outline = this.add.graphics().setDepth(30);
    outline.lineStyle(Math.max(2, cellSize * 0.08), 0xfacc15, 1);
    outline.strokeRect(originX + position.x * cellSize, originY + position.y * cellSize, width * cellSize, height * cellSize);
  }

  updateSnapshot(state: MatchState): void {
    this.latestState = state;
    if (this.scene.isActive()) {
      this.renderSnapshot(state);
    }
  }

  private renderWaitingState(): void {
    this.children.removeAll();
    this.add.text(320, 300, 'Đang chờ snapshot từ server…', {
      color: '#94a3b8',
      fontFamily: 'Inter, system-ui, sans-serif',
      fontSize: '18px',
    }).setOrigin(0.5);
  }

  private renderSnapshot(state: MatchState): void {
    this.children.removeAll();
    const canvasWidth = this.scale.width;
    const canvasHeight = this.scale.height;
    const boardPadding = 42;
    const availableWidth = canvasWidth - boardPadding * 2;
    const availableHeight = canvasHeight - boardPadding * 2;
    const cellSize = Math.min(
      availableWidth / state.mapWidth,
      availableHeight / state.mapHeight,
    );
    const boardWidth = cellSize * state.mapWidth;
    const boardHeight = cellSize * state.mapHeight;
    const originX = (canvasWidth - boardWidth) / 2;
    const originY = (canvasHeight - boardHeight) / 2;
    this.layout = { originX, originY, cellSize };
    // Small cells hide ID labels until the entity is hovered.
    const compact = cellSize < 28;

    const background = this.add.graphics();
    background.fillStyle(0x111827, 1);
    background.fillRoundedRect(originX - 12, originY - 12, boardWidth + 24, boardHeight + 24, 12);

    const grid = this.add.graphics();
    grid.lineStyle(1, 0x334155, 0.9);
    for (let x = 0; x <= state.mapWidth; x += 1) {
      const lineX = originX + x * cellSize;
      grid.lineBetween(lineX, originY, lineX, originY + boardHeight);
    }
    for (let y = 0; y <= state.mapHeight; y += 1) {
      const lineY = originY + y * cellSize;
      grid.lineBetween(originX, lineY, originX + boardWidth, lineY);
    }

    const teams = new Map(state.teams.map((team) => [team.id, team]));
    for (const headquarters of state.headquarters) {
      const team = teams.get(headquarters.teamId);
      const center = this.cellCenter(originX, originY, cellSize, headquarters.position.x, headquarters.position.y);
      const size = cellSize * 0.9;
      // Polygon points stay non-negative so Phaser centers the hexagon on its cell.
      const halfWidth = size * 0.46;
      const halfHeight = size * 0.5;
      const shape = this.add.polygon(center.x, center.y, [
        halfWidth, 0, halfWidth * 2, halfHeight * 0.5, halfWidth * 2, halfHeight * 1.5,
        halfWidth, halfHeight * 2, 0, halfHeight * 1.5, 0, halfHeight * 0.5,
      ], this.teamColor(team), headquarters.hp > 0 ? 0.82 : 0.22);
      shape.setStrokeStyle(Math.max(1, cellSize * 0.06), 0xf8fafc, headquarters.hp > 0 ? 0.8 : 0.25);
      this.add.text(center.x, center.y, compact ? `${headquarters.hp}` : `HQ\n${headquarters.hp}`, {
        align: 'center',
        color: '#ffffff',
        fontFamily: 'Inter, system-ui, sans-serif',
        fontSize: `${Math.max(7, Math.min(16, cellSize * 0.32))}px`,
        fontStyle: '700',
      }).setOrigin(0.5);
      this.addIdLabel(shape, center.x, center.y - size * 0.32, headquarters.id, size * 0.8, compact);
    }

    for (const resource of state.resourceNodes) {
      const center = this.cellCenter(originX, originY, cellSize, resource.position.x, resource.position.y);
      const size = cellSize * 0.32;
      const meat = resource.type === 'MEAT';
      this.add.star(center.x, center.y, 6, size, size * 0.45, meat ? 0xef4444 : 0x84cc16, 0.9);
      this.add.text(center.x, center.y + size, compact ? `${resource.amount}` : `${meat ? 'Thịt' : 'Gỗ'} ${resource.amount}`, {
        color: meat ? '#fca5a5' : '#bef264',
        fontFamily: 'Inter, system-ui, sans-serif',
        fontSize: `${Math.max(7, Math.min(10, cellSize * 0.26))}px`,
      }).setOrigin(0.5, 0.2);
    }

    for (const building of state.buildings) {
      const team = teams.get(building.teamId);
      const center = this.cellCenter(originX, originY, cellSize, building.position.x, building.position.y);
      const size = cellSize * 0.8;
      const shape = building.type === 'TOWER'
        ? this.add.circle(center.x, center.y, size * 0.42, this.teamColor(team), 0.95)
        : this.add.rectangle(center.x, center.y, size, building.type === 'WALL' ? size * 0.38 : size,
          this.teamColor(team), 0.95);
      if (building.type === 'BARRACKS') shape.setAngle(45);
      shape.setStrokeStyle(Math.max(1, cellSize * 0.05), 0xfbbf24, 1);
      const glyph = building.type === 'BARRACKS' ? 'B' : building.type === 'TOWER' ? 'T' : '▰';
      this.add.text(center.x, center.y, glyph, {
        color: '#ffffff',
        fontFamily: 'Inter, system-ui, sans-serif',
        fontSize: `${Math.max(7, Math.min(14, cellSize * 0.4))}px`,
        fontStyle: '700',
      }).setOrigin(0.5);
      this.addIdLabel(shape, center.x, center.y - size * 0.3, building.id, size * 0.82, compact);
    }

    for (const projectile of state.projectiles) {
      const team = teams.get(projectile.teamId);
      const center = this.cellCenter(originX, originY, cellSize, projectile.position.x, projectile.position.y);
      const shape = this.add.circle(center.x, center.y, Math.max(2, cellSize * 0.12), this.teamColor(team), 1);
      shape.setStrokeStyle(1, 0xf8fafc, 0.95);
    }

    for (const unit of state.units) {
      const team = teams.get(unit.teamId);
      const center = new Phaser.Math.Vector2(
        originX + (unit.position.x + unit.width / 2) * cellSize,
        originY + (unit.position.y + unit.height / 2) * cellSize,
      );
      const alpha = unit.hp > 0 ? 1 : 0.25;
      const color = this.teamColor(team);
      let shape: Phaser.GameObjects.GameObject;
      if (unit.type === 'SIEGE') {
        const vehicle = this.add.rectangle(
          center.x, center.y, cellSize * unit.width * 0.82, cellSize * unit.height * 0.82, color, alpha,
        );
        vehicle.setStrokeStyle(Math.max(1, cellSize * 0.06), 0xf8fafc, 0.9);
        shape = vehicle;
      } else {
        shape = this.drawPerson(center.x, center.y, cellSize * 0.9, color, alpha, unit.type);
      }
      this.add.text(center.x, center.y + cellSize * unit.height * 0.42, `${unit.hp}`, {
        align: 'center',
        color: '#ffffff',
        fontFamily: 'Inter, system-ui, sans-serif',
        fontSize: `${Math.max(7, Math.min(12, cellSize * 0.28))}px`,
        fontStyle: '700',
        stroke: '#020617',
        strokeThickness: 2,
      }).setOrigin(0.5, 0.2).setDepth(10);
      this.addIdLabel(
        shape,
        center.x,
        center.y - cellSize * unit.height * 0.3,
        unit.id,
        cellSize * unit.width * 0.78,
        compact,
      );
    }
    this.drawSelection(state, originX, originY, cellSize);
  }

  /** Stick figure in team color; WORKER holds a pickaxe, MELEE a sword, RANGED a bow. */
  private drawPerson(
    x: number,
    y: number,
    height: number,
    color: number,
    alpha: number,
    type: string,
  ): Phaser.GameObjects.Graphics {
    const figure = this.add.graphics({ x, y });
    const line = Math.max(1.2, height * 0.12);
    const headY = -height * 0.3;
    const neckY = -height * 0.17;
    const hipY = height * 0.1;
    const footY = height * 0.42;
    const handY = height * 0.02;
    for (const [width, stroke] of [[line + 2, 0x020617], [line, color]] as const) {
      figure.lineStyle(width, stroke, alpha);
      figure.lineBetween(0, neckY, 0, hipY);
      figure.lineBetween(0, hipY, -height * 0.15, footY);
      figure.lineBetween(0, hipY, height * 0.15, footY);
      figure.lineBetween(0, neckY + line, -height * 0.2, handY);
      figure.lineBetween(0, neckY + line, height * 0.2, handY);
    }
    figure.fillStyle(0x020617, alpha);
    figure.fillCircle(0, headY, height * 0.14 + 1);
    figure.fillStyle(color, alpha);
    figure.fillCircle(0, headY, height * 0.14);
    figure.lineStyle(Math.max(1, line * 0.7), 0xe2e8f0, alpha);
    if (type === 'WORKER') {
      figure.lineBetween(height * 0.2, handY, height * 0.3, -height * 0.25);
      figure.lineBetween(height * 0.18, -height * 0.22, height * 0.4, -height * 0.28);
    } else if (type === 'MELEE') {
      figure.lineBetween(height * 0.2, handY, height * 0.2, -height * 0.4);
      figure.lineBetween(height * 0.12, -height * 0.05, height * 0.28, -height * 0.05);
    } else if (type === 'RANGED') {
      figure.beginPath();
      figure.arc(-height * 0.2, handY, height * 0.22, -Math.PI / 2, Math.PI / 2, true);
      figure.strokePath();
    }
    figure.setInteractive(
      new Phaser.Geom.Rectangle(-height * 0.3, -height * 0.5, height * 0.6, height),
      Phaser.Geom.Rectangle.Contains,
    );
    return figure;
  }

  private addIdLabel(
    target: Phaser.GameObjects.GameObject,
    x: number,
    y: number,
    id: string,
    availableWidth: number,
    compact: boolean,
  ): void {
    const maxCharacters = Math.max(7, Math.floor(availableWidth / 6));
    const compactId = id.length > maxCharacters ? `${id.slice(0, maxCharacters - 1)}…` : id;
    const label = this.add.text(x, y, compact ? id : compactId, {
      align: 'center',
      backgroundColor: '#020617',
      color: '#f8fafc',
      fontFamily: 'Inter, system-ui, sans-serif',
      fontSize: `${Math.max(8, Math.min(11, availableWidth * 0.16))}px`,
      fontStyle: '700',
      padding: { x: 3, y: 1 },
    }).setOrigin(0.5).setDepth(20).setInteractive();
    if (compact) {
      label.setVisible(false).setDepth(40);
      if (!target.input) target.setInteractive();
      target.on('pointerover', () => label.setVisible(true));
      target.on('pointerout', () => label.setVisible(false));
    } else if (compactId !== id) {
      label.on('pointerover', () => label.setText(id).setDepth(40));
      label.on('pointerout', () => label.setText(compactId).setDepth(20));
    }
  }

  private cellCenter(
    originX: number,
    originY: number,
    cellSize: number,
    x: number,
    y: number,
  ): Phaser.Math.Vector2 {
    return new Phaser.Math.Vector2(
      originX + (x + 0.5) * cellSize,
      originY + (y + 0.5) * cellSize,
    );
  }

  private teamColor(team: Team | undefined): number {
    return Phaser.Display.Color.ValueToColor(team?.color ?? '#64748b').color;
  }
}
