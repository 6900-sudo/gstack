"use client";

import { useEffect, useState } from "react";
import { TaxEstimator } from "@/components/TaxEstimator";
import { ExportButton } from "@/components/ExportButton";
import { exportProductsToCsv } from "@/lib/exportCsv";
import type { Product } from "@/lib/store";

const card = {
  background: "white",
  padding: 16,
  borderRadius: 12,
} as const;

export default function DashboardPage() {
  const [products, setProducts] = useState<Product[]>([]);

  useEffect(() => {
    fetch("/api/products")
      .then((r) => r.json())
      .then(setProducts);
  }, []);

  const totalProfit = products.reduce((sum, p) => sum + p.profit, 0);

  return (
    <main style={{ padding: 24 }}>
      <h1 style={{ fontSize: 32 }}>Dashboard</h1>

      <div
        style={{
          display: "grid",
          gap: 16,
          gridTemplateColumns: "repeat(2, 1fr)",
          marginTop: 24,
        }}
      >
        <section style={card}>
          <h2>Profit Snapshot</h2>
          <p>
            {products.length} saved product{products.length === 1 ? "" : "s"},
            total profit per unit set: ${totalProfit.toFixed(2)}
          </p>
        </section>

        <section style={card}>
          <h2>Tax Estimator</h2>
          <TaxEstimator />
        </section>

        <section style={card}>
          <h2>Export</h2>
          {products.length > 0 ? (
            <ExportButton csv={exportProductsToCsv(products)} />
          ) : (
            <p>Save a product first, then export your list as CSV.</p>
          )}
        </section>

        <section style={card}>
          <h2>Next steps</h2>
          <p>
            Supplier tracking, auth, and Prisma storage are queued — see
            PROJECT_RECORD.md.
          </p>
        </section>
      </div>
    </main>
  );
}
