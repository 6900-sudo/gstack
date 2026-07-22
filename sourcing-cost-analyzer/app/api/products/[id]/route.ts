import { NextResponse } from "next/server";
import { deleteProduct } from "@/lib/store";

export async function DELETE(
  _req: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;
  await deleteProduct(Number(id));
  return NextResponse.json({ ok: true });
}
