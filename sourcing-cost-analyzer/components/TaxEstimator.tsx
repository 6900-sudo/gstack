"use client";

import { useState } from "react";
import { taxRates, estimateDuty } from "@/lib/taxRates";

export function TaxEstimator() {
  const [country, setCountry] = useState("US");
  const [itemValue, setItemValue] = useState(100);

  const duty = estimateDuty(country, Number.isFinite(itemValue) ? itemValue : 0);

  return (
    <div style={{ display: "flex", gap: 12, alignItems: "center", flexWrap: "wrap" }}>
      <select value={country} onChange={(e) => setCountry(e.target.value)}>
        {Object.keys(taxRates).map((c) => (
          <option key={c} value={c}>
            {c}
          </option>
        ))}
      </select>

      <input
        type="number"
        step="0.01"
        value={itemValue}
        onChange={(e) => setItemValue(parseFloat(e.target.value))}
      />

      <p style={{ margin: 0 }}>
        Estimated duty: <strong>${duty.toFixed(2)}</strong>{" "}
        <span style={{ color: "#64748b" }}>(placeholder rate, not real HS-code duty)</span>
      </p>
    </div>
  );
}
