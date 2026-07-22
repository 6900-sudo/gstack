export function exportProductsToCsv(
  products: {
    name: string;
    supplier: string;
    sellingPrice: number;
    trueCost: number;
    profit: number;
    marginPct: number;
  }[]
): string {
  const header = "Name,Supplier,Selling Price,True Cost,Profit,Margin %";
  const escape = (value: string) =>
    /[",\n]/.test(value) ? `"${value.replace(/"/g, '""')}"` : value;
  const rows = products.map((p) =>
    [
      escape(p.name),
      escape(p.supplier),
      p.sellingPrice,
      p.trueCost,
      p.profit,
      p.marginPct,
    ].join(",")
  );
  return [header, ...rows].join("\n");
}
