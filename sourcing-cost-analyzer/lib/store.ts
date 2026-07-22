import { promises as fs } from "fs";
import path from "path";

// Simple JSON-file persistence so saved products survive refreshes and
// restarts without native dependencies. Upgrade path is Prisma + SQLite —
// see PROJECT_RECORD.md §4.
export type Product = {
  id: number;
  name: string;
  supplier: string;
  sellingPrice: number;
  trueCost: number;
  profit: number;
  marginPct: number;
  createdAt: string;
};

const DATA_FILE = path.join(process.cwd(), "data", "products.json");

export async function listProducts(): Promise<Product[]> {
  try {
    const raw = await fs.readFile(DATA_FILE, "utf-8");
    return JSON.parse(raw) as Product[];
  } catch (err) {
    if ((err as NodeJS.ErrnoException).code === "ENOENT") return [];
    throw err;
  }
}

async function writeProducts(products: Product[]): Promise<void> {
  await fs.mkdir(path.dirname(DATA_FILE), { recursive: true });
  await fs.writeFile(DATA_FILE, JSON.stringify(products, null, 2));
}

export async function addProduct(
  input: Omit<Product, "id" | "createdAt">
): Promise<Product> {
  const products = await listProducts();
  const product: Product = {
    ...input,
    id: Date.now(),
    createdAt: new Date().toISOString(),
  };
  products.unshift(product);
  await writeProducts(products);
  return product;
}

export async function deleteProduct(id: number): Promise<void> {
  const products = await listProducts();
  await writeProducts(products.filter((p) => p.id !== id));
}
