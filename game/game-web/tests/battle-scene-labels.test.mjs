import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const source = await readFile(new URL('../src/BattleScene.ts', import.meta.url), 'utf8');

test('small cells reveal ID and HP labels on hover instead of covering neighbours', () => {
  assert.match(source, /const compact = cellOnScreen < 28;/);
  assert.match(source, /target\.on\('pointerover', \(\) => label\.setVisible\(true\)\)/);
  assert.match(source, /`\$\{item\.id\} · \$\{Math\.max\(0, item\.hp\)\}\/\$\{item\.maxHp\} HP`/);
});

test('every entity is drawn from the bundled models, never from a network URL', () => {
  assert.match(source, /this\.textures\.addBase64\(key, url\)/);
  assert.match(source, /modelDataUrl\(item\.kind, color\)/);
  assert.doesNotMatch(source, /https?:\/\//);
});

test('status lights appear only on commandable entities of the viewer team', () => {
  assert.match(source, /item\.commandable && item\.teamId !== null && item\.teamId === this\.overlay\.viewerTeamId/);
});

test('SIEGE keeps its 2x2 footprint and units facing left are mirrored', () => {
  assert.match(source, /width: item\.width, height: item\.height/);
  assert.match(source, /flip: item\.facing === 'LEFT'/);
});

test('only model textures trigger a redraw, so text canvases cannot cause an endless redraw loop', () => {
  assert.match(source, /if \(!this\.pendingTextures\.delete\(key\) \|\| this\.redrawQueued\) return;/);
});

test('health bars use the max HP sent by the server', () => {
  assert.match(source, /hpRatio\(item\.hp, item\.maxHp\)/);
  assert.doesNotMatch(source, /HpScale/);
});

test('coordinate labels stay on an unzoomed UI camera while the main camera zooms the board', () => {
  assert.match(source, /this\.cameras\.main\.ignore\(this\.uiObjects\)/);
  assert.match(source, /this\.uiCamera\?\.ignore\(/);
});

test('a press that moves pans the board instead of selecting', () => {
  assert.match(source, /if \(!drag \|\| drag\.moved\) return;/);
});
