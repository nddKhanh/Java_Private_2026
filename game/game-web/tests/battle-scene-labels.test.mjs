import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const source = await readFile(new URL('../src/BattleScene.ts', import.meta.url), 'utf8');

test('entity IDs render as contrasting overlays inside every entity symbol', () => {
  assert.match(source, /addIdLabel\(center\.x, center\.y - size \* 0\.32, headquarters\.id/);
  assert.match(source, /addIdLabel\(center\.x, center\.y - size \* 0\.3, building\.id/);
  assert.match(source, /center\.y - cellSize \* unit\.height \* 0\.3,[\s\S]*unit\.id/);
  assert.match(source, /backgroundColor: '#020617'/);
  assert.match(source, /label\.on\('pointerover'/);
});

test('entity ID overlays keep type and health glyphs visible', () => {
  assert.match(source, /center\.y \+ size \* 0\.08, `HQ\\n\$\{headquarters\.hp\}`/);
  assert.match(source, /center\.y \+ radius \* 0\.16, `\$\{glyph\}\\n\$\{unit\.hp\}`/);
  assert.doesNotMatch(source, /addIdLabel\([^;]+center\.y \+[^;]+(?:headquarters|building|unit)\.id/);
});
