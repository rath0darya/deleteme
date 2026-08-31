import { NextRequest, NextResponse } from "next/server";

export const runtime = "nodejs";

export async function POST(request: NextRequest) {
  const key = process.env.HIBP_API_KEY;
  if (!key) return NextResponse.json({ configured: false, message: "HIBP_API_KEY is not configured." }, { status: 503 });

  const body = await request.json().catch(() => null);
  const email = typeof body?.email === "string" ? body.email.trim() : "";
  if (!email || !email.includes("@")) return NextResponse.json({ error: "A valid email is required." }, { status: 400 });

  const response = await fetch(`https://haveibeenpwned.com/api/v3/breachedaccount/${encodeURIComponent(email)}?truncateResponse=false`, {
    headers: { "hibp-api-key": key, "user-agent": "deleteme-open-source/0.1" },
    cache: "no-store",
  });

  if (response.status === 404) return NextResponse.json({ configured: true, found: false, breaches: [] });
  if (!response.ok) return NextResponse.json({ error: `Breach provider returned ${response.status}.` }, { status: 502 });

  const breaches = await response.json();
  return NextResponse.json({
    configured: true,
    found: true,
    breaches: breaches.map((b: any) => ({
      name: b.Name,
      title: b.Title,
      domain: b.Domain,
      breachDate: b.BreachDate,
      addedDate: b.AddedDate,
      dataClasses: b.DataClasses,
      isVerified: b.IsVerified,
      isSensitive: b.IsSensitive,
      isStealerLog: b.IsStealerLog,
    })),
  });
}
