/**
 * Shared palette and font of the viewer. The CSS custom properties in `style.css` carry the same values;
 * `tests/theme.test.mjs` fails when the two drift apart.
 */
export const FONT_FAMILY = '"Be Vietnam Pro", "Segoe UI", system-ui, sans-serif';

/** Weights bundled in `assets/fonts`; Phaser text waits for them before drawing. */
export const FONT_WEIGHTS = [400, 500, 700, 800] as const;

export const PALETTE = {
  background: '#060b16',
  surface: '#0b1221',
  border: '#334155',
  text: '#e2e8f0',
  textStrong: '#f8fafc',
  muted: '#94a3b8',
  subtle: '#64748b',
  accent: '#fbbf24',
  info: '#60a5fa',
  success: '#4ade80',
  warning: '#fb923c',
  danger: '#f87171',
  outline: '#0b1020',
} as const;

/** Battlefield colors used only by the Phaser board. */
export const BOARD = {
  frame: '#151d18',
  groundLight: '#223127',
  groundDark: '#1d2a22',
  gridMinor: '#2f4035',
  gridMajor: '#56695b',
  coordinate: '#8a9a8f',
} as const;

/** Converts `#rrggbb` to the number Phaser expects. */
export function hexNumber(color: string): number {
  return Number.parseInt(color.slice(1), 16);
}
