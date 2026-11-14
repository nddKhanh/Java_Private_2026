import Phaser from 'phaser';
import type { MatchState, Team } from './types';

export class BattleScene extends Phaser.Scene {
  private latestState: MatchState | null = null;

  constructor() {
    super('battle');
  }

  create(): void {
    this.renderWaitingState();
    if (this.latestState) {
      this.renderSnapshot(this.latestState);
    }
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
      const size = Math.max(20, cellSize * 0.68);
      const shape = this.add.polygon(center.x, center.y, [
        0, -size * 0.52, size * 0.46, -size * 0.26, size * 0.46, size * 0.26,
        0, size * 0.52, -size * 0.46, size * 0.26, -size * 0.46, -size * 0.26,
      ], this.teamColor(team), headquarters.hp > 0 ? 0.82 : 0.22);
      shape.setStrokeStyle(Math.max(2, cellSize * 0.04), 0xf8fafc, headquarters.hp > 0 ? 0.8 : 0.25);
      this.add.text(center.x, center.y, `HQ\n${headquarters.hp}`, {
        align: 'center',
        color: '#ffffff',
        fontFamily: 'Inter, system-ui, sans-serif',
        fontSize: `${Math.max(10, Math.min(16, cellSize * 0.2))}px`,
        fontStyle: '700',
      }).setOrigin(0.5);
      this.addIdLabel(center.x, center.y + size * 0.62, headquarters.id, cellSize);
    }

    for (const resource of state.resourceNodes) {
      const center = this.cellCenter(originX, originY, cellSize, resource.position.x, resource.position.y);
      const size = Math.max(10, cellSize * 0.2);
      this.add.star(center.x, center.y, 6, size, size * 0.45, 0xf59e0b, 0.9);
      this.add.text(center.x, center.y + size, `${resource.amount}`, {
        color: '#fbbf24', fontFamily: 'Inter, system-ui, sans-serif', fontSize: '10px',
      }).setOrigin(0.5);
    }

    for (const building of state.buildings) {
      const team = teams.get(building.teamId);
      const center = this.cellCenter(originX, originY, cellSize, building.position.x, building.position.y);
      const size = Math.max(18, cellSize * 0.54);
      const shape = building.type === 'TOWER'
        ? this.add.circle(center.x, center.y, size * 0.42, this.teamColor(team), 0.95)
        : this.add.rectangle(center.x, center.y, size, building.type === 'WALL' ? size * 0.38 : size,
          this.teamColor(team), 0.95);
      if (building.type === 'BARRACKS') shape.setAngle(45);
      shape.setStrokeStyle(2, 0xfbbf24, 1);
      const glyph = building.type === 'BARRACKS' ? 'B' : building.type === 'TOWER' ? 'T' : '▰';
      this.add.text(center.x, center.y, glyph, {
        color: '#ffffff', fontFamily: 'Inter, system-ui, sans-serif', fontSize: '14px', fontStyle: '700',
      }).setOrigin(0.5);
      this.addIdLabel(center.x, center.y + size * 0.62, building.id, cellSize);
    }

    for (const projectile of state.projectiles) {
      const team = teams.get(projectile.teamId);
      const center = this.cellCenter(originX, originY, cellSize, projectile.position.x, projectile.position.y);
      const shape = this.add.circle(center.x, center.y, Math.max(4, cellSize * 0.09), this.teamColor(team), 1);
      shape.setStrokeStyle(2, 0xf8fafc, 0.95);
    }

    for (const unit of state.units) {
      const team = teams.get(unit.teamId);
      const center = new Phaser.Math.Vector2(
        originX + (unit.position.x + unit.width / 2) * cellSize,
        originY + (unit.position.y + unit.height / 2) * cellSize,
      );
      const radius = Math.max(10, cellSize * 0.3);
      const alpha = unit.hp > 0 ? 1 : 0.25;
      const shape = unit.type === 'WORKER'
        ? this.add.circle(center.x, center.y, radius, this.teamColor(team), alpha)
        : unit.type === 'MELEE'
          ? this.add.triangle(center.x, center.y, radius, 0, radius * 2, radius * 2, 0, radius * 2,
            this.teamColor(team), alpha)
          : unit.type === 'RANGED'
            ? this.add.rectangle(center.x, center.y, radius * 1.55, radius * 1.55,
              this.teamColor(team), alpha).setAngle(45)
            : this.add.rectangle(
          center.x,
          center.y,
          cellSize * unit.width * 0.82,
          cellSize * unit.height * 0.82,
          this.teamColor(team),
          alpha,
        );
      shape.setStrokeStyle(Math.max(2, cellSize * 0.04), 0xf8fafc, 0.9);
      const glyph = unit.type === 'WORKER' ? 'W' : unit.type === 'MELEE' ? 'M' : unit.type === 'RANGED' ? 'R' : 'S';
      this.add.text(center.x, center.y, `${glyph}\n${unit.hp}`, {
        align: 'center',
        color: '#ffffff',
        fontFamily: 'Inter, system-ui, sans-serif',
        fontSize: `${Math.max(9, Math.min(13, cellSize * 0.17))}px`,
        fontStyle: '700',
      }).setOrigin(0.5);
      this.addIdLabel(
        center.x,
        center.y + cellSize * unit.height * 0.42,
        unit.id,
        cellSize * unit.width,
      );
    }
  }

  private addIdLabel(x: number, y: number, id: string, availableWidth: number): void {
    const maxCharacters = Math.max(7, Math.floor(availableWidth / 6));
    const compactId = id.length > maxCharacters ? `${id.slice(0, maxCharacters - 1)}…` : id;
    const label = this.add.text(x, y, compactId, {
      align: 'center',
      backgroundColor: '#020617',
      color: '#f8fafc',
      fontFamily: 'Inter, system-ui, sans-serif',
      fontSize: `${Math.max(8, Math.min(11, availableWidth * 0.16))}px`,
      padding: { x: 3, y: 1 },
    }).setOrigin(0.5).setDepth(20).setInteractive();
    if (compactId !== id) {
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
