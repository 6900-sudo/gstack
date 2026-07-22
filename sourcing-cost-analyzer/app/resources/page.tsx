import { resources } from "@/lib/resources";

export default function ResourcesPage() {
  return (
    <main style={{ padding: 24 }}>
      <h1>Resources</h1>
      <ul>
        {resources.map((r) => (
          <li key={r.title} style={{ marginBottom: 6 }}>
            <strong>{r.category}:</strong>{" "}
            <a href={r.url} target="_blank" rel="noopener noreferrer">
              {r.title}
            </a>
          </li>
        ))}
      </ul>
    </main>
  );
}
