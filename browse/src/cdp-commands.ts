/*
 * $B cdp <Domain.method> [json-params] — CLI surface for the CDP escape hatch.
 *
 * Output for trusted methods is a plain JSON pretty-print.
 * Output for untrusted methods is wrapped with the centralized UNTRUSTED EXTERNAL
 * CONTENT envelope so the sidebar-agent classifier sees it (matches the pattern
 * used by other untrusted-content commands in commands.ts).
 */

import type { BrowserManager } from './browser-manager';
import { dispatchCdpCall } from './cdp-bridge';
import { wrapUntrustedContent } from './commands';

function parseQualified(name: string): { domain: string; method: string } {
  const idx = name.indexOf('.');
  if (idx <= 0 || idx === name.length - 1) {
    throw new Error(
      `Usage: $B cdp <Domain.method> [json-params]\n` +
        `Cause: '${name}' is not in Domain.method format.\n` +
        'Action: e.g. $B cdp Accessibility.getFullAXTree {}'
    );
  }
  return { domain: name.slice(0, idx), method: name.slice(idx + 1) };
}

/**
 * Validate CDP params parsed from JSON. Guards against overly-deep/large
 * payloads and non-serializable values. Keeps checks intentionally simple —
 * this is a minimal hardening to fail-fast on obviously-malicious inputs.
 */
function validateCdpParams(obj: unknown, maxDepth = 5, maxNodes = 1000, maxStringLen = 2000) {
  let nodes = 0;
  function _walk(v: unknown, depth: number) {
    nodes++;
    if (nodes > maxNodes) throw new Error('CDP params rejected: payload too large');
    if (depth > maxDepth) throw new Error('CDP params rejected: payload too deep');

    if (v === null) return;
    const t = typeof v;
    if (t === 'string') {
      if ((v as string).length > maxStringLen) throw new Error('CDP params rejected: string too long');
      return;
    }
    if (t === 'number') {
      if (!Number.isFinite(v as number)) throw new Error('CDP params rejected: non-finite number');
      return;
    }
    if (t === 'boolean') return;
    if (Array.isArray(v)) {
      for (const it of v) _walk(it, depth + 1);
      return;
    }
    if (t === 'object') {
      // iterate own enumerable properties only
      for (const key of Object.keys(v as Record<string, unknown>)) {
        const val = (v as Record<string, unknown>)[key];
        _walk(key, depth + 1);
        _walk(val, depth + 1);
      }
      return;
    }
    // Functions, symbols, undefined — reject
    throw new Error('CDP params rejected: unsupported value type');
  }
  _walk(obj, 0);
}

export async function handleCdpCommand(args: string[], bm: BrowserManager): Promise<string> {
  if (args.length === 0 || args[0] === 'help' || args[0] === '--help') {
    return [
      '$B cdp — raw CDP method dispatch (deny-default escape hatch)',
      '',
      'Usage: $B cdp <Domain.method> [json-params]',
      '',
      'Allowed methods are listed in browse/src/cdp-allowlist.ts. To add one,',
      'open a PR with a one-line justification and the (scope, output) tags.',
      'Examples:',
      '  $B cdp Accessibility.getFullAXTree {}',
      '  $B cdp Performance.getMetrics {}',
      '  $B cdp DOM.describeNode \'{"backendNodeId":42,"depth":3}\'',
    ].join('\n');
  }
  const qualified = args[0]!;
  const { domain, method } = parseQualified(qualified);
  // Optional second arg is JSON params; default to {}.
  let params: Record<string, unknown> = {};
  if (args[1]) {
    try {
      params = JSON.parse(args[1]) ?? {};
      // Basic validation to reject obviously-malicious or malformed payloads.
      validateCdpParams(params);
    } catch (e: any) {
      // If our own validator threw, surface a clear error; otherwise the JSON.parse
      // error path provides context about invalid JSON.
      if (e && typeof e.message === 'string' && e.message.startsWith('CDP params rejected')) {
        throw e;
      }
      throw new Error(
        `Cannot parse params as JSON: ${e.message}\n` +
          `Cause: argument '${args[1]}' is not valid JSON.\n` +
          'Action: pass a JSON object literal, e.g. \'{"backendNodeId":42}\'.'
      );
    }
  }
  // Dispatch via the bridge (allowlist + mutex + timeout + finally-release).
  const tabId = bm.getActiveTabId();
  const { raw, entry } = await dispatchCdpCall({ domain, method, params, tabId, bm });
  const json = JSON.stringify(raw, null, 2);
  if (entry.output === 'untrusted') {
    return wrapUntrustedContent(json, `cdp:${qualified}`);
  }
  return json;
}
