import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = {
  title: "Sourcing Cost Analyzer",
  description:
    "Compare domestic vs China sourcing: landed cost, margin, and tax-adjusted profit.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body style={{ margin: 0, fontFamily: "sans-serif", background: "#f8fafc" }}>
        <nav
          style={{
            display: "flex",
            gap: 16,
            padding: "12px 24px",
            background: "white",
            borderBottom: "1px solid #e2e8f0",
          }}
        >
          <strong>Sourcing Cost Analyzer</strong>
          <Link href="/">Calculator</Link>
          <Link href="/products">Products</Link>
          <Link href="/dashboard">Dashboard</Link>
          <Link href="/resources">Resources</Link>
        </nav>
        {children}
      </body>
    </html>
  );
}
