"use client";

import { useState } from "react";

const LABELS: Record<string, string> = {
  unitPrice: "Unit price",
  shipping: "Shipping",
  dutyTax: "Duty / tax",
  paymentFee: "Payment fee",
  defectAllowance: "Defect allowance",
  sellingPrice: "Selling price",
};

export default function Home() {
  const [productName, setProductName] = useState("3000mAh solar power bank");
  const [supplier, setSupplier] = useState("");
  const [form, setForm] = useState({
    unitPrice: 4.2,
    shipping: 3.8,
    dutyTax: 1.1,
    paymentFee: 0.6,
    defectAllowance: 0.4,
    sellingPrice: 19.99,
  });

  const [result, setResult] = useState<{
    trueCost: number;
    profit: number;
    marginPct: number;
  } | null>(null);
  const [saved, setSaved] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaved(false);
    const res = await fetch("/api/calculate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(form),
    });
    setResult(await res.json());
  }

  async function handleSave() {
    if (!result) return;
    await fetch("/api/products", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        name: productName,
        supplier,
        sellingPrice: form.sellingPrice,
        trueCost: result.trueCost,
      }),
    });
    setSaved(true);
  }

  return (
    <main style={{ padding: 24, maxWidth: 720 }}>
      <h1>Landed Cost Calculator</h1>

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: 12 }}>
          <label>
            Product name:{" "}
            <input
              value={productName}
              onChange={(e) => setProductName(e.target.value)}
            />
          </label>
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>
            Supplier:{" "}
            <input value={supplier} onChange={(e) => setSupplier(e.target.value)} />
          </label>
        </div>

        {Object.entries(form).map(([key, value]) => (
          <div key={key} style={{ marginBottom: 12 }}>
            <label>
              {LABELS[key]}:{" "}
              <input
                type="number"
                step="0.01"
                value={value}
                onChange={(e) =>
                  setForm({ ...form, [key]: parseFloat(e.target.value) })
                }
              />
            </label>
          </div>
        ))}
        <button type="submit">Calculate</button>
      </form>

      {result && (
        <section
          style={{
            marginTop: 24,
            background: "white",
            padding: 16,
            borderRadius: 12,
          }}
        >
          <h2>Results</h2>
          <p>True landed cost: ${result.trueCost}</p>
          <p>Profit per sale: ${result.profit}</p>
          <p>Margin: {result.marginPct}%</p>
          <button onClick={handleSave}>Save product</button>
          {saved && <span style={{ marginLeft: 8, color: "#16a34a" }}>Saved.</span>}
        </section>
      )}
    </main>
  );
}
