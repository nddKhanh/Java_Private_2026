/**
 * Hand-drawn SVG models of every unit, building and resource. Team-colored parts take the team color, so one
 * function serves every team. The same SVG is used by the Phaser board and by the documentation images.
 * Pure module with no browser or Phaser dependency.
 */
export type ModelKind =
  | 'WORKER' | 'MELEE' | 'RANGED' | 'SIEGE'
  | 'HQ' | 'BARRACKS' | 'TOWER' | 'WALL'
  | 'WOOD' | 'MEAT' | 'PROJECTILE';

export const MODEL_KINDS: readonly ModelKind[] = [
  'WORKER', 'MELEE', 'RANGED', 'SIEGE', 'HQ', 'BARRACKS', 'TOWER', 'WALL', 'WOOD', 'MEAT', 'PROJECTILE',
];

/** Models that do not belong to a team and ignore the color argument. */
const NEUTRAL: ReadonlySet<ModelKind> = new Set<ModelKind>(['WOOD', 'MEAT']);

/** Pixel size the SVG is rasterized at; the board scales it down to the cell. */
export const MODEL_PIXELS = 256;

const OUTLINE = '#0b1020';
const SKIN = '#f1c27d';
const WOOD = '#9a6434';
const WOOD_DARK = '#5c3a1a';
const STEEL = '#d5dde7';
const STEEL_DARK = '#7b8794';
const STONE = '#a3a9b3';
const STONE_DARK = '#6f7782';
const GOLD = '#fbbf24';
const NEUTRAL_COLOR = '#64748b';

/** Mixes `#rrggbb` toward white (amount > 0) or black (amount < 0); amount is in [-1, 1]. */
export function shade(color: string, amount: number): string {
  const value = /^#[0-9a-fA-F]{6}$/.test(color) ? Number.parseInt(color.slice(1), 16) : 0x64748b;
  const target = amount >= 0 ? 255 : 0;
  const weight = Math.min(1, Math.abs(amount));
  const channel = (shift: number) => {
    const current = (value >> shift) & 0xff;
    return Math.round(current + (target - current) * weight).toString(16).padStart(2, '0');
  };
  return `#${channel(16)}${channel(8)}${channel(0)}`;
}

/** Full SVG document of a model drawn in the given team color. */
export function modelSvg(kind: ModelKind, teamColor: string): string {
  const team = /^#[0-9a-fA-F]{6}$/.test(teamColor) ? teamColor : NEUTRAL_COLOR;
  const body = DRAWERS[kind](team, shade(team, -0.35), shade(team, 0.45));
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" width="${MODEL_PIXELS}" height="${MODEL_PIXELS}">`
    + `<g stroke="${OUTLINE}" stroke-width="2" stroke-linejoin="round" stroke-linecap="round">${body}</g></svg>`;
}

/** `data:` URL of the model, usable as an image source with no network access. */
export function modelDataUrl(kind: ModelKind, teamColor: string): string {
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(modelSvg(kind, teamColor))}`;
}

/** Texture key shared by every entity of the same kind and color. */
export function modelKey(kind: ModelKind, teamColor: string): string {
  return NEUTRAL.has(kind) ? `model-${kind}` : `model-${kind}-${teamColor.toLowerCase()}`;
}

const shadow = (rx: number) => `<ellipse cx="32" cy="58" rx="${rx}" ry="4" fill="#000" fill-opacity=".35" stroke="none"/>`;
const legs = `<rect x="24" y="44" width="6" height="12" rx="2" fill="#3b2f2a"/><rect x="34" y="44" width="6" height="12" rx="2" fill="#3b2f2a"/>`;
const eyes = `<circle cx="28.5" cy="22" r="1.4" fill="${OUTLINE}" stroke="none"/><circle cx="35.5" cy="22" r="1.4" fill="${OUTLINE}" stroke="none"/>`;
const tunic = (team: string, dark: string) =>
  `<rect x="20" y="28" width="24" height="20" rx="6" fill="${team}"/>`
  + `<path d="M24 46 h16" stroke="${dark}" stroke-width="2.5"/>`;

const DRAWERS: Record<ModelKind, (team: string, dark: string, light: string) => string> = {
  WORKER: (team, dark) => shadow(13) + legs + tunic(team, dark)
    + `<rect x="20" y="39" width="24" height="3.5" fill="${WOOD_DARK}" stroke="none"/>`
    + `<circle cx="32" cy="20" r="10" fill="${SKIN}"/>` + eyes
    + `<path d="M21 18 a11 11 0 0 1 22 0 z" fill="${GOLD}"/><rect x="19" y="16.5" width="26" height="3.5" rx="1.5" fill="${GOLD}"/>`
    + `<path d="M46 54 L55 18" stroke="${WOOD}" stroke-width="4"/>`
    + `<path d="M44 22 Q53 10 63 19" fill="none" stroke="${STEEL}" stroke-width="5"/>`
    + `<circle cx="48" cy="44" r="4" fill="${SKIN}"/>`,

  MELEE: (team, dark, light) => shadow(14) + legs + tunic(team, dark)
    + `<circle cx="32" cy="20" r="10" fill="${SKIN}"/>` + eyes
    + `<path d="M21 21 a11 11 0 0 1 22 0 v1 h-22 z" fill="${STEEL}"/><rect x="30.5" y="17" width="3" height="8" fill="${STEEL_DARK}"/>`
    + `<rect x="49" y="6" width="5" height="30" rx="1" fill="${STEEL}"/>`
    + `<rect x="44" y="35" width="15" height="4" rx="1.5" fill="${GOLD}"/><rect x="49.5" y="39" width="4" height="9" fill="${WOOD_DARK}"/>`
    + `<path d="M5 27 h20 v11 q0 12 -10 17 q-10 -5 -10 -17 z" fill="${team}"/>`
    + `<path d="M10 33 l5 6 l5 -6" fill="none" stroke="${light}" stroke-width="3"/>`,

  RANGED: (team, dark, light) => shadow(13) + legs
    + `<rect x="44" y="22" width="8" height="20" rx="2" fill="${WOOD_DARK}"/>`
    + `<path d="M46 22 l-2 -6 M50 22 l2 -6" stroke="${light}" stroke-width="2.5"/>`
    + tunic(team, dark)
    + `<path d="M19 28 q0 -20 13 -20 q13 0 13 20 z" fill="${dark}"/>`
    + `<circle cx="32" cy="22" r="7.5" fill="${SKIN}"/>` + eyes
    + `<path d="M15 8 Q-1 32 15 56" fill="none" stroke="${WOOD}" stroke-width="4"/>`
    + `<path d="M15 8 V56" stroke="#e5e7eb" stroke-width="1.2"/>`
    + `<path d="M7 32 H40" stroke="${WOOD}" stroke-width="2.5"/><path d="M3 32 l6 -4 v8 z" fill="${STEEL}" stroke-width="1.5"/>`
    + `<circle cx="40" cy="34" r="4" fill="${SKIN}"/>`,

  SIEGE: (team, dark) => shadow(28)
    + `<path d="M10 36 V7" stroke="${WOOD_DARK}" stroke-width="2.5"/><path d="M10 8 h17 l-5 6 l5 6 h-17 z" fill="${team}"/>`
    + `<path d="M19 36 L31 14 L43 36" fill="none" stroke="${WOOD}" stroke-width="4.5"/>`
    + `<path d="M31 30 L52 11" stroke="${WOOD_DARK}" stroke-width="4"/>`
    + `<circle cx="53" cy="10" r="6" fill="${WOOD_DARK}"/><circle cx="53" cy="9" r="3" fill="${STONE}" stroke-width="1.5"/>`
    + `<rect x="5" y="35" width="54" height="11" rx="3" fill="${WOOD}"/><path d="M8 40.5 H56" stroke="${dark}" stroke-width="2.5"/>`
    + `<circle cx="17" cy="50" r="8.5" fill="${WOOD_DARK}"/><circle cx="17" cy="50" r="3" fill="${STEEL}"/>`
    + `<circle cx="47" cy="50" r="8.5" fill="${WOOD_DARK}"/><circle cx="47" cy="50" r="3" fill="${STEEL}"/>`,

  HQ: (team, dark, light) => shadow(29)
    + `<rect x="11" y="28" width="42" height="28" fill="${STONE}"/>`
    + `<path d="M11 36 H53 M11 46 H53 M22 28 V36 M42 28 V36 M17 36 V46 M32 36 V46 M47 36 V46" stroke="${STONE_DARK}" stroke-width="1.2"/>`
    + `<rect x="4" y="20" width="14" height="36" fill="${STONE}"/><rect x="46" y="20" width="14" height="36" fill="${STONE}"/>`
    + `<path d="M2 21 L11 5 L20 21 z" fill="${team}"/><path d="M44 21 L53 5 L62 21 z" fill="${team}"/>`
    + `<path d="M11 5 V21 M53 5 V21" stroke="${dark}" stroke-width="1.5"/>`
    + `<path d="M25 56 v-11 a7 7 0 0 1 14 0 v11 z" fill="${WOOD_DARK}"/><path d="M32 39 V56" stroke="${WOOD}" stroke-width="1.5"/>`
    + `<path d="M32 28 V5" stroke="${WOOD_DARK}" stroke-width="2.5"/><path d="M32 6 h15 l-4 5 l4 5 h-15 z" fill="${team}"/>`
    + `<circle cx="38" cy="11" r="2" fill="${light}" stroke="none"/>`
    + `<rect x="8" y="30" width="6" height="8" rx="3" fill="${OUTLINE}" stroke="none"/><rect x="50" y="30" width="6" height="8" rx="3" fill="${OUTLINE}" stroke="none"/>`,

  BARRACKS: (team, dark) => shadow(28)
    + `<rect x="8" y="30" width="48" height="25" fill="${WOOD}"/>`
    + `<path d="M8 38 H56 M8 46 H56" stroke="${WOOD_DARK}" stroke-width="1.2"/>`
    + `<path d="M3 33 L32 9 L61 33 z" fill="${team}"/><path d="M12 28 L32 12 L52 28" fill="none" stroke="${dark}" stroke-width="2"/>`
    + `<rect x="25" y="40" width="14" height="15" fill="${WOOD_DARK}"/>`
    + `<path d="M24 18 L40 30 M40 18 L24 30" stroke="${STEEL}" stroke-width="3.5"/>`
    + `<path d="M24 18 L40 30 M40 18 L24 30" stroke="${OUTLINE}" stroke-width="1" fill="none"/>`,

  TOWER: (team, dark) => shadow(17)
    + `<path d="M18 58 L22 20 H42 L46 58 z" fill="${STONE}"/>`
    + `<path d="M20.5 34 H43.5 M19.5 46 H44.5 M28 20 V34 M36 34 V46 M28 46 V58" stroke="${STONE_DARK}" stroke-width="1.2"/>`
    + `<rect x="16" y="12" width="32" height="10" fill="${STONE}"/>`
    + `<rect x="16" y="6" width="7" height="7" fill="${STONE}"/><rect x="28.5" y="6" width="7" height="7" fill="${STONE}"/><rect x="41" y="6" width="7" height="7" fill="${STONE}"/>`
    + `<rect x="20" y="22" width="24" height="6" fill="${team}"/><path d="M20 28 H44" stroke="${dark}" stroke-width="1.5"/>`
    + `<rect x="30" y="34" width="4" height="10" rx="2" fill="${OUTLINE}" stroke="none"/>`,

  WALL: (team, dark) => `<ellipse cx="32" cy="52" rx="30" ry="4" fill="#000" fill-opacity=".35" stroke="none"/>`
    + `<rect x="2" y="22" width="60" height="28" fill="${STONE}"/>`
    + `<rect x="2" y="15" width="10" height="8" fill="${STONE}"/><rect x="18" y="15" width="10" height="8" fill="${STONE}"/>`
    + `<rect x="36" y="15" width="10" height="8" fill="${STONE}"/><rect x="52" y="15" width="10" height="8" fill="${STONE}"/>`
    + `<path d="M2 31 H62 M12 22 V31 M27 22 V31 M42 22 V31 M57 22 V31 M20 31 V40 M35 31 V40 M50 31 V40" stroke="${STONE_DARK}" stroke-width="1.2"/>`
    + `<rect x="2" y="40" width="60" height="5" fill="${team}" stroke-width="1.5"/><path d="M2 45 H62" stroke="${dark}" stroke-width="1"/>`,

  WOOD: () => shadow(24)
    + `<rect x="28" y="24" width="8" height="18" fill="${WOOD_DARK}"/>`
    + `<path d="M32 3 L48 24 H16 z" fill="#4d7c0f"/><path d="M32 12 L50 33 H14 z" fill="#65a30d"/>`
    + `<rect x="6" y="44" width="40" height="10" rx="5" fill="${WOOD}"/><circle cx="46" cy="49" r="5" fill="#e0b07a"/>`
    + `<rect x="14" y="36" width="36" height="10" rx="5" fill="${WOOD}"/><circle cx="50" cy="41" r="5" fill="#e0b07a"/>`
    + `<circle cx="46" cy="49" r="2" fill="none" stroke="${WOOD}" stroke-width="1"/><circle cx="50" cy="41" r="2" fill="none" stroke="${WOOD}" stroke-width="1"/>`,

  MEAT: () => shadow(20)
    + `<path d="M40 38 L54 52" stroke="#f5f5f4" stroke-width="6"/><path d="M40 38 L54 52" stroke="${OUTLINE}" stroke-width="1" fill="none"/>`
    + `<circle cx="55" cy="49" r="4" fill="#f5f5f4"/><circle cx="51" cy="55" r="4" fill="#f5f5f4"/>`
    + `<ellipse cx="27" cy="29" rx="19" ry="16" transform="rotate(-35 27 29)" fill="#b4431c"/>`
    + `<ellipse cx="24" cy="25" rx="11" ry="8" transform="rotate(-35 24 25)" fill="#e2683a" stroke="none"/>`
    + `<ellipse cx="20" cy="22" rx="4" ry="2.5" transform="rotate(-35 20 22)" fill="#f8b28c" stroke="none"/>`,

  PROJECTILE: (team, _dark, light) => `<circle cx="32" cy="32" r="20" fill="${team}" fill-opacity=".3" stroke="none"/>`
    + `<circle cx="32" cy="32" r="12" fill="${team}"/><circle cx="32" cy="32" r="5" fill="${light}" stroke="none"/>`,
};
