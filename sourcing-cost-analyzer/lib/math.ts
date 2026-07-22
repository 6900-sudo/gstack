export type CostInput = {
  unitPrice: number;
  shipping: number;
  dutyTax: number;
  paymentFee: number;
  defectAllowance: number;
  sellingPrice: number;
};

export function calculateCost(input: CostInput) {
  const trueCost =
    input.unitPrice +
    input.shipping +
    input.dutyTax +
    input.paymentFee +
    input.defectAllowance;

  const profit = input.sellingPrice - trueCost;
  const marginPct = input.sellingPrice
    ? (profit / input.sellingPrice) * 100
    : 0;

  return {
    trueCost: Number(trueCost.toFixed(2)),
    profit: Number(profit.toFixed(2)),
    marginPct: Number(marginPct.toFixed(2)),
  };
}
