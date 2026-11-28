/** Pure rules behind the health bars, bot status lights and coordinate labels on the board. */

/** How long a bot's light stays orange after one of its commands is rejected. */
export const REJECT_FLASH_MS = 2000;

export type BotLight = 'idle' | 'busy' | 'rejected';

/** Orange right after a rejection, lit while commands are queued, dark when the bot has nothing to do. */
export function botLight(queueLength: number, rejectedAt: number | undefined, now: number): BotLight {
  if (rejectedAt !== undefined && now - rejectedAt < REJECT_FLASH_MS) return 'rejected';
  return queueLength > 0 ? 'busy' : 'idle';
}

/** Green above 60 %, amber above 30 %, red below. */
export function hpColor(ratio: number): string {
  if (ratio > 0.6) return '#4ade80';
  if (ratio > 0.3) return '#facc15';
  return '#f87171';
}

/**
 * The snapshot has no max HP, so the viewer keeps the highest HP seen for each entity kind
 * (`unit:WORKER`, `building:TOWER`, `hq`). Every entity of a kind spawns with the same HP from `game-rules.yml`,
 * so the scale is exact as soon as one full-health entity of that kind has been seen.
 */
export class HpScale {
  private readonly maxByKind = new Map<string, number>();

  observe(kind: string, hp: number): void {
    if (hp > (this.maxByKind.get(kind) ?? 0)) this.maxByKind.set(kind, hp);
  }

  max(kind: string, hp: number): number {
    return Math.max(this.maxByKind.get(kind) ?? 0, hp, 1);
  }

  ratio(kind: string, hp: number): number {
    return Math.max(0, Math.min(1, hp / this.max(kind, hp)));
  }
}

/** Every cell gets a coordinate when cells are wide enough, otherwise every fifth one. */
export function coordinateStep(cellSize: number): number {
  return cellSize >= 20 ? 1 : 5;
}
