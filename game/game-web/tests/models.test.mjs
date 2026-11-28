import assert from 'node:assert/strict';
import test from 'node:test';
import { MODEL_KINDS, modelDataUrl, modelKey, modelSvg, shade } from '../src/models.ts';

test('every model is a self-contained SVG with no external reference', () => {
  for (const kind of MODEL_KINDS) {
    const svg = modelSvg(kind, '#3b82f6');
    assert.match(svg, /^<svg xmlns="http:\/\/www\.w3\.org\/2000\/svg" viewBox="0 0 64 64"/);
    assert.doesNotMatch(svg.replace('http://www.w3.org/2000/svg', ''), /https?:|href=|url\(/);
    assert.ok(modelDataUrl(kind, '#3b82f6').startsWith('data:image/svg+xml'));
  }
});

test('team models take the team color while resources stay neutral', () => {
  assert.ok(modelSvg('MELEE', '#ef4444').includes('#ef4444'));
  assert.notEqual(modelKey('MELEE', '#EF4444'), modelKey('MELEE', '#3b82f6'));
  assert.equal(modelKey('WOOD', '#ef4444'), modelKey('WOOD', '#3b82f6'));
  assert.equal(modelSvg('MEAT', '#ef4444'), modelSvg('MEAT', '#3b82f6'));
});

test('invalid team colors fall back to a neutral color instead of breaking the SVG', () => {
  assert.ok(modelSvg('HQ', 'red"/><script>').includes('#64748b'));
  assert.doesNotMatch(modelSvg('HQ', 'red"/><script>'), /script/);
});

test('shade mixes toward white or black', () => {
  assert.equal(shade('#000000', 1), '#ffffff');
  assert.equal(shade('#ffffff', -1), '#000000');
  assert.equal(shade('#808080', 0), '#808080');
});
