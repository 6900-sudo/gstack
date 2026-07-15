// Placeholder flat rates for quick estimates only. Real import duty varies by
// HS code and changes over time — keep these configurable, never hardcode
// them into components (rates change and hardcoding breaks estimates).
export const taxRates: Record<string, number> = {
  US: 0.05,
  UK: 0.2,
  CA: 0.13,
  AU: 0.1,
};

export function estimateDuty(country: string, itemValue: number): number {
  const rate = taxRates[country] ?? 0;
  return Number((itemValue * rate).toFixed(2));
}
