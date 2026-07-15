import { NextResponse } from "next/server";
import { addProduct, listProducts } from "@/lib/store";
import { calculateCost } from "@/lib/math";

export async function GET() {
  return NextResponse.json(await listProducts());
}

export async function POST(req: Request) {
  const body = await req.json();

  const name = String(body.name ?? "").trim();
  const supplier = String(body.supplier ?? "").trim();
  const sellingPrice = Number(body.sellingPrice);
  const trueCost = Number(body.trueCost);

  if (!name || !Number.isFinite(sellingPrice) || !Number.isFinite(trueCost)) {
    return NextResponse.json(
      { error: "name, sellingPrice, and trueCost are required" },
      { status: 400 }
    );
  }

  const { profit, marginPct } = calculateCost({
    unitPrice: trueCost,
    shipping: 0,
    dutyTax: 0,
    paymentFee: 0,
    defectAllowance: 0,
    sellingPrice,
  });

  const product = await addProduct({
    name,
    supplier,
    sellingPrice,
    trueCost,
    profit,
    marginPct,
  });

  return NextResponse.json(product);
}
