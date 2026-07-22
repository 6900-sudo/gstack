"use client";

export function ExportButton({ csv }: { csv: string }) {
  function handleDownload() {
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "products.csv";
    a.click();
    URL.revokeObjectURL(url);
  }

  return <button onClick={handleDownload}>Export CSV</button>;
}
