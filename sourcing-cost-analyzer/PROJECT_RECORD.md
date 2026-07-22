# Sourcing Cost Analyzer — Project Record

This is the continuation record for the project started in a Duck.ai (GPT-5.4 mini)
conversation on 14–15 July 2026. It captures the business context, every decision
made, the full app spec, and the exact build state, so work can resume in any new
session without re-deriving anything.

The app scaffold in this directory implements the MVP described below and runs
locally (see `README.md`).

---

## 1. Business context

Researching the **prepper market for affiliate dropshipping**, specifically whether
the same items can be sourced cheaper from Chinese wholesale markets.

Best-fit product categories identified:

- LED flashlights, headlamps
- Solar chargers / power banks
- Water filters and bottles
- Multi-tools and small knives
- Tarps, ponchos, paracord, bags
- First-aid accessories
- Camping cookware and simple survival kits

Key insight: compare **landed cost**, not quote price. "China price = cheaper" is a
trap for small low-margin items where shipping erases the savings.

### Canonical cost formula

```
true_cost  = unit_price + shipping + duty_tax + payment_fee + defect_allowance
profit     = selling_price - true_cost
margin_pct = profit / selling_price * 100
```

### Worked example (3000mAh solar power bank)

| Line item | Amount |
|---|---|
| Domestic retail (sell price) | $19.99 |
| China quote (unit) | $4.20 |
| Shipping | $3.80 |
| Duty/tax | $1.10 |
| Payment fee | $0.60 |
| Defect allowance | $0.40 |
| **True landed cost** | **$10.10** |
| **Profit per sale** | **$9.89** |
| **Margin** | **≈49.5%** |

Note: an earlier pass in the conversation quoted $9.10 landed / $10.89 margin —
that version excluded payment fees and defect allowance. The $10.10 figure with
all five cost components is the canonical one, and it's what the app computes.
Still excluded everywhere: returns and ad costs — factor those in per channel.

### Sourcing workflow (agreed)

1. Identify the exact SKU or close equivalent.
2. Check domestic retail price.
3. Get 3–5 China quotes for the same spec.
4. Add shipping and duties.
5. Estimate defect/return cost.
6. Compare final landed cost and margin.
7. Always test one small order first — low quotes hide quality/shipping problems.
8. Compare the exact same specs (battery size, material grade, certification differ
   between "same" items).

### Key resource sites (agreed list)

| Site | Purpose |
|---|---|
| Alibaba | Broad bulk sourcing, supplier comparison |
| 1688 | Lower domestic-China pricing (usually via sourcing agent) |
| Made-in-China | Industrial/hardware products, audited suppliers |
| Global Sources | Electronics, export-ready suppliers |
| DHgate | Small test orders, low-MOQ dropshipping |
| AliExpress | Sampling / tiny test buys (not cheapest for bulk) |
| Helium 10 | Product research and sourcing workflows |
| Jungle Scout | Product database and supplier discovery |

Plus: landed-cost / import-duty calculators (compare true cost before sourcing).
Rule of thumb: use one tool for research and a separate source for supplier
verification, so you never rely on a single platform's data.

---

## 2. App spec (agreed)

**Name:** Sourcing Cost Analyzer

**Goal:** Help users find cheaper product sources, calculate true landed cost, and
compare profit before buying or listing.

**Primary users:** affiliate marketers, dropshippers, small eCommerce sellers,
sourcing researchers.

**Core features:** product comparison, supplier quote tracking, landed cost
calculator, tax/duty estimator, profit/margin calculator, resource links library,
supplier notes and risk flags.

**Main screens:** Dashboard, Product detail, Supplier comparison, Cost calculator,
Tax/duty estimator, Resources page, Saved projects.

**Key inputs:** product name, supplier URL, unit price, shipping, duties/taxes,
payment fees, defect allowance, selling price, target market country.

**Key outputs:** true landed cost, gross margin, margin %, estimated profit per
sale, supplier ranking, risk score.

**Stack (chosen):** Next.js (App Router, TypeScript) frontend + API routes;
SQLite via Prisma for persistence (upgrade path — see §4); NextAuth for auth
(deferred — see §4).

**MVP scope (agreed):** product entry, landed cost calculator, margin calculator,
resource links, saved notes. Build one working calculator end-to-end before adding
logins, scraping, or automation.

---

## 3. Build state — what exists in this directory

Runnable Next.js scaffold implementing the MVP plus the three follow-on features
(saved products, CSV export, country tax estimator):

```
sourcing-cost-analyzer/
├── app/
│   ├── layout.tsx              # shared nav
│   ├── page.tsx                # calculator (client form → /api/calculate)
│   ├── dashboard/page.tsx      # combined view: tax estimator + CSV export
│   ├── products/page.tsx       # saved products (persisted via /api/products)
│   ├── resources/page.tsx      # curated links from lib/resources.ts
│   └── api/
│       ├── calculate/route.ts  # POST → true cost / profit / margin %
│       └── products/
│           ├── route.ts        # GET list, POST create
│           └── [id]/route.ts   # DELETE
├── components/
│   ├── ExportButton.tsx        # CSV download from live product list
│   └── TaxEstimator.tsx        # country dropdown + duty estimate
├── lib/
│   ├── math.ts                 # calculateCost() — the canonical formula
│   ├── exportCsv.ts            # products → CSV string
│   ├── taxRates.ts             # per-country rates (placeholder, configurable)
│   ├── resources.ts            # curated resource links
│   └── store.ts                # JSON-file persistence (data/products.json)
├── package.json / tsconfig.json / next.config.mjs
└── README.md                   # run instructions
```

**One deliberate deviation from the conversation:** persistence is a JSON-file
store (`lib/store.ts`) instead of Prisma. The conversation's in-memory API reset
on refresh; the JSON store fixes that with zero native dependencies. Prisma
remains the intended production path — full setup preserved in §4.

**Tax rates are placeholders** (US 5%, UK 20%, CA 13%, AU 10%) kept in
`lib/taxRates.ts` precisely so they're easy to update — real duty rates vary by
HS code and change over time. Do not treat them as accurate import duty.

---

## 4. Deferred work (next steps, in agreed build order)

1. **Auth (NextAuth)** — deferred because it needs real OAuth credentials.
   Agreed setup: `npm install next-auth @next-auth/prisma-adapter`, GitHub
   provider in `app/api/auth/[...nextauth]/route.ts`, env vars `GITHUB_ID`,
   `GITHUB_SECRET`, `NEXTAUTH_SECRET`, `NEXTAUTH_URL`; protect
   `/products`, `/resources`, `/dashboard` via `middleware.ts` `withAuth` with
   `signIn: "/signin"`. Then attach saved products/notes per user.
2. **Prisma migration** — replace `lib/store.ts` with Prisma + SQLite
   (`npm install prisma @prisma/client && npx prisma init`), models:

   ```prisma
   model Product {
     id           Int      @id @default(autoincrement())
     name         String
     supplier     String
     sellingPrice Float
     trueCost     Float
     profit       Float
     marginPct    Float
     createdAt    DateTime @default(now())
   }
   model Supplier { id Int @id @default(autoincrement()); name String; url String?; country String?; notes String? }
   model Resource { id Int @id @default(autoincrement()); title String; url String; category String }
   model User     { id Int @id @default(autoincrement()); email String @unique; name String?; createdAt DateTime @default(now()) }
   ```

   `npx prisma migrate dev --name init`, singleton client in `lib/db.ts`.
   After any schema change: rerun migration and regenerate the client.
3. **Supplier entities** — supplier comparison screen, MOQ / lead time / risk
   flags (Low/Med/High), supplier ranking by true cost.
4. **Editable resources** — move `lib/resources.ts` into the database.
5. **Sign-in page, create-product form, polished dashboard.**
6. **Real duty data** — replace placeholder tax rates with a landed-cost API or
   per-country HS-code tables.

### Test products for validation (agreed)

flashlight, solar charger, water filter, paracord, first-aid kit.

---

## 5. Accumulated tips from the conversation

- Compare suppliers on **landed cost per sellable unit**, not quote price.
- If a quote looks too good, check the same SKU's landed cost, MOQ, and defect
  risk before assuming it's a better deal.
- Keep tax rules, supplier links, and pricing formulas modular/configurable.
- If the calculator shows `NaN`, check every input parses as a number before it
  reaches the API.
- When auth fails, check the callback URL and env var names first.
- Make each layer work locally/statically before wiring live APIs, scraping, or
  accounts.
