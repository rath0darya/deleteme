"use client";

import { FormEvent, useState } from "react";
import { Search, ShieldCheck, Database, Globe2, FileWarning, Send, LockKeyhole } from "lucide-react";

const sources = [
  { name: "Data brokers", detail: "People-search & public records", status: "Scan", tone: "" },
  { name: "Search engines", detail: "Indexed pages & cached exposure", status: "Scan", tone: "" },
  { name: "Social platforms", detail: "Profiles, posts & identifiers", status: "Scan", tone: "" },
  { name: "Breach intelligence", detail: "Known breach exposure", status: "Monitor", tone: "" },
  { name: "Paste & leak sites", detail: "Publicly indexed leak references", status: "Monitor", tone: "" },
  { name: "Developer platforms", detail: "Repos, commits & exposed secrets", status: "Scan", tone: "" },
];

export default function Home() {
  const [identity, setIdentity] = useState("");
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);

  function submit(e: FormEvent) {
    e.preventDefault();
    if (!identity.trim()) return;
    setLoading(true);
    setTimeout(() => { setLoading(false); setSearched(true); }, 650);
  }

  return (
    <main>
      <header className="wrap nav">
        <a className="brand" href="#top">delete<span>me</span>.</a>
        <nav className="navlinks"><a href="#coverage">Coverage</a><a href="#how">How it works</a><a href="#dashboard">Dashboard</a></nav>
        <a className="pill" href="#dashboard">Privacy first</a>
      </header>

      <section className="wrap hero" id="top">
        <div className="eyebrow"><span className="dot"/> DIGITAL FOOTPRINT CONTROL</div>
        <h1>Find it. Remove it.<br/><i>Keep it gone.</i></h1>
        <p>One privacy workspace for discovering personal data across the public web, data brokers, social platforms and breach intelligence. Then turn every finding into a tracked removal request.</p>
        <form className="search" onSubmit={submit}>
          <input value={identity} onChange={e => setIdentity(e.target.value)} placeholder="Email, phone number, username or full name" aria-label="Identity to scan" />
          <button className="btn" type="submit">{loading ? "Scanning…" : "Start exposure scan"}</button>
        </form>
        <div className="stats"><div className="stat"><strong>6+</strong><small>source categories</small></div><div className="stat"><strong>24/7</strong><small>removal tracking model</small></div><div className="stat"><strong>0</strong><small>passwords required</small></div></div>
        {searched && <div className="note"><b>Scan workspace created for:</b> {identity}. Results are shown as a removal workflow. No claim is made that every website on the internet can be searched or that every breach can be erased from third-party systems.</div>}
      </section>

      <section className="wrap section" id="coverage">
        <h2>Coverage without pretending the internet is a database</h2>
        <p>Sources are grouped by what can actually be discovered and what can legally be requested for removal.</p>
        <div className="grid">{sources.map((s, i) => <article className="card" key={s.name}><div className="icon">{i % 3 === 0 ? <Globe2 size={20}/> : i % 3 === 1 ? <Database size={20}/> : <FileWarning size={20}/>}</div><h3>{s.name}</h3><p>{s.detail}</p><div style={{marginTop:16}} className="pill">{s.status}</div></article>)}</div>
      </section>

      <section className="wrap section" id="how">
        <h2>Removal engine</h2>
        <p>The product separates discovery from deletion, because the internet unfortunately does not have a giant red “undo” button.</p>
        <div className="grid">
          <article className="card"><div className="icon"><Search size={20}/></div><h3>1. Discover</h3><p>Normalize identities, search supported sources, detect matching records and score confidence before showing a finding.</p></article>
          <article className="card"><div className="icon"><Send size={20}/></div><h3>2. Request</h3><p>Generate a source-specific removal request, retain proof of submission and track the response deadline.</p></article>
          <article className="card"><div className="icon"><ShieldCheck size={20}/></div><h3>3. Verify</h3><p>Re-scan after removal and mark each source as removed, pending, refused, inaccessible or reappeared.</p></article>
        </div>
      </section>

      <section className="wrap section" id="dashboard">
        <h2>Exposure dashboard</h2>
        <p>A single queue for findings, requests and verification.</p>
        <div className="dashboard">
          <div className="dashhead"><strong>Removal queue</strong><span className="pill">MVP preview</span></div>
          <div className="row"><div className="source"><LockKeyhole size={17}/><div><b>Identity profile</b><span>Primary scan target</span></div></div><span className="status safe">Protected</span><span>Core</span><span className="pill">Ready</span></div>
          <div className="row"><div className="source"><Database size={17}/><div><b>Data broker record</b><span>Source-specific deletion request</span></div></div><span className="status">Pending</span><span>Broker</span><span className="pill">Track</span></div>
          <div className="row"><div className="source"><FileWarning size={17}/><div><b>Breach exposure</b><span>Exposure can be identified, not magically erased</span></div></div><span className="status risk">Exposed</span><span>Breach</span><span className="pill">Review</span></div>
          <div className="row"><div className="source"><Globe2 size={17}/><div><b>Public web result</b><span>URL-level removal workflow</span></div></div><span className="status">Pending</span><span>Web</span><span className="pill">Request</span></div>
        </div>
        <div className="note"><b>Important:</b> a breach notification is not the same thing as breach deletion. The system should help users identify exposure, contact the data controller or host where appropriate, rotate compromised credentials and verify remediation. HIBP, for example, exposes breach metadata through an API but does not provide a universal deletion mechanism.</div>
      </section>

      <footer className="wrap footer"><span>delete.me · privacy operations, not privacy theatre.</span><span>Built as an open-source removal workflow.</span></footer>
    </main>
  );
}
