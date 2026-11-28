/** Pure rules behind the health bars, bot status lights, coordinate labels and board zoom. */

/** How long a bot's light stays orange after one of its commands is rejected. */
export const REJECT_FLASH_MS = 2000;

export type BotLight = 'idle' | 'busy' | 'rejected';

/** Orange right after a rejection, lit while commands are queued, dark when the bot has nothing to do. */
export function botLight(queueLength: number, rejectedAt: number | undefined, now: number): BotLight {
  if (rejectedAt !== undefined && now - rejectedAt < REJECT_FLASH_MS) return 'rejected';
  return queueLength > 0 ? 'busy' : 'idle';
}

/** Share of health left, using the max HP the server sends with every entity. */
export function hpRatio(hp: number, maxHp: number): number {
  return maxHp > 0 ? Math.max(0, Math.min(1, hp / maxHp)) : 0;
}

/** Green above 60 %, amber above 30 %, red below. */
export function hpColor(ratio: number): string {
  if (ratio > 0.6) return '#4ade80';
  if (ratio > 0.3) return '#facc15';
  return '#f87171';
}

/** Every cell gets a coordinate when cells look wide enough on screen, otherwise every fifth one. */
export function coordinateStep(cellScreenSize: number): number {
  return cellScreenSize >= 20 ? 1 : 5;
}

/** Layout size the board is designed at; larger canvases scale every on-screen size by `canvas / BASE`. */
export const BASE_CANVAS = 960;

/** Canvas pixels for a board shown `cssWidth` wide: sharp on high-density screens, bounded for slow machines. */
export function canvasSize(cssWidth: number, devicePixelRatio: number): number {
  const wanted = Math.round(cssWidth * Math.max(1, devicePixelRatio) * 1.25);
  return Math.max(BASE_CANVAS, Math.min(2400, wanted));
}

export const MIN_ZOOM = 1;

/** Zoom that shows one cell about `maxCellPixels` wide, never below the full-board view. */
export function maxZoom(cellSize: number, maxCellPixels: number): number {
  return cellSize > 0 ? Math.max(MIN_ZOOM, maxCellPixels / cellSize) : MIN_ZOOM;
}

export function clampZoom(zoom: number, max: number): number {
  return Math.max(MIN_ZOOM, Math.min(max, zoom));
}

/*
 * Phaser zooms around the middle of the view, so a world coordinate w appears on screen at
 * s = (w - scroll - size / 2) * zoom + size / 2. The helpers below solve that relation per axis.
 */

/** World coordinate under the screen coordinate `screen`. */
export function worldAt(screen: number, size: number, zoom: number, scroll: number): number {
  return (screen - size / 2) / zoom + size / 2 + scroll;
}

/** Screen coordinate of the world coordinate `world`. */
export function screenAt(world: number, size: number, zoom: number, scroll: number): number {
  return (world - scroll - size / 2) * zoom + size / 2;
}

/** Keeps the zoomed view inside the canvas: no empty space past the board frame. */
export function clampScroll(scroll: number, size: number, zoom: number): number {
  const limit = (size / 2) * (1 - 1 / zoom);
  return Math.max(-limit, Math.min(limit, scroll));
}

/** Scroll that keeps `world` under `screen` at the new zoom, so zooming follows the mouse. */
export function scrollToKeep(world: number, screen: number, size: number, zoom: number): number {
  return clampScroll(world - (screen - size / 2) / zoom - size / 2, size, zoom);
}
