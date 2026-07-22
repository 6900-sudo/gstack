# Sourcing Cost Analyzer

Compare domestic vs China sourcing for a product: true landed cost, profit,
and margin — plus a saved-product list, CSV export, a rough per-country duty
estimator, and a curated library of sourcing/research links.

Started from a July 2026 planning conversation; the full business context,
spec, decisions, and roadmap live in [PROJECT_RECORD.md](./PROJECT_RECORD.md).

## Run it

```bash
npm install   # or bun install
npm run dev   # or bun run dev
```

Open http://localhost:3000.

- **Calculator** (`/`) — enter unit price, shipping, duty/tax, payment fee,
  defect allowance, and selling price; get true cost, profit, and margin %.
  Save the result as a product.
- **Products** (`/products`) — saved products (persisted to `data/products.json`),
  delete, export as CSV.
- **Dashboard** (`/dashboard`) — profit snapshot, country duty estimator, export.
- **Resources** (`/resources`) — Alibaba, 1688, Made-in-China, Global Sources,
  DHgate, AliExpress, Helium 10, Jungle Scout.

## Notes

- The duty estimator uses flat placeholder rates in `lib/taxRates.ts` — quick
  ballparks only, not real HS-code duty.
- Persistence is a JSON file store (`lib/store.ts`); the planned upgrade is
  Prisma + SQLite, and auth (NextAuth) is deferred until OAuth credentials
  exist. Both are specced in PROJECT_RECORD.md §4.
