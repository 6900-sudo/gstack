import { NextResponse } from "next/server";
import { calculateCost, type CostInput } from "@/lib/math";

const FIELDS = [
  "unitPrice",
  "shipping",
  "dutyTax",
  "paymentFee",
  "defectAllowance",
  "sellingPrice",
] as const;

export async function POST(req: Request) {
  const body = await req.json();

  const input = {} as CostInput;
  for (const field of FIELDS) {
    const value = Number(body[field]);
    if (!Number.isFinite(value)) {
      return NextResponse.json(
        { error: `Field "${field}" must be a number` },
        { status: 400 }
      );
    }
    input[field] = value;
  }

  return NextResponse.json(calculateCost(input));
}
