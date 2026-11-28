import assert from 'node:assert/strict';
import { readdir, readFile } from 'node:fs/promises';
import test from 'node:test';
import { PALETTE } from '../src/theme.ts';

const css = await readFile(new URL('../src/style.css', import.meta.url), 'utf8');
const fonts = await readFile(new URL('../src/fonts.css', import.meta.url), 'utf8');

test('CSS tokens match the palette used by the board', () => {
  const tokens = { bg: 'background', surface: 'surface', border: 'border', text: 'text', 'text-strong': 'textStrong',
    muted: 'muted', subtle: 'subtle', accent: 'accent', info: 'info', success: 'success', warning: 'warning', danger: 'danger' };
  for (const [token, key] of Object.entries(tokens)) {
    const match = css.match(new RegExp(`--${token}: (#[0-9a-fA-F]{6});`));
    assert.ok(match, `missing --${token}`);
    assert.equal(match[1].toLowerCase(), PALETTE[key], `--${token}`);
  }
});

test('fonts are bundled locally with their license and never loaded from the Internet', async () => {
  const files = await readdir(new URL('../src/assets/fonts/', import.meta.url));
  assert.ok(files.includes('OFL.txt'));
  for (const url of fonts.matchAll(/url\('\.\/(assets\/fonts\/[^']+)'\)/g)) {
    assert.ok(files.includes(url[1].split('/').pop()), url[1]);
  }
  for (const source of [css, fonts, await readFile(new URL('../index.html', import.meta.url), 'utf8')]) {
    assert.doesNotMatch(source, /https?:\/\//);
  }
});
