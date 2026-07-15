"use client";

import { useEffect, useState } from "react";
import { ExportButton } from "@/components/ExportButton";
import { exportProductsToCsv } from "@/lib/exportCsv";
import type { Product } from "@/lib/store";

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);

  async function refresh() {
    const res = await fetch("/api/products");
    setProducts(await res.json());
  }

  useEffect(() => {
    refresh();
  }, []);

  async function remove(id: number) {
    await fetch(`/api/products/${id}`, { method: "DELETE" });
    refresh();
  }

  return (
    <main style={{ padding: 24 }}>
      <h1>Saved Products</h1>

      {products.length === 0 ? (
        <p>No saved products yet. Use the calculator, then hit “Save product”.</p>
      ) : (
        <>
          <ExportButton csv={exportProductsToCsv(products)} />
          <ul>
            {products.map((p) => (
              <li key={p.id} style={{ marginBottom: 8 }}>
                {p.name} — {p.supplier || "no supplier"} — Sell: $
                {p.sellingPrice.toFixed(2)} — Cost: ${p.trueCost.toFixed(2)} —
                Profit: ${p.profit.toFixed(2)} — Margin: {p.marginPct.toFixed(2)}%{" "}
                <button onClick={() => remove(p.id)}>Delete</button>
              </li>
            ))}
          </ul>
        </>
      )}
    </main>
  );
}
