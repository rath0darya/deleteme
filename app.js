const state = { type: 'email', value: '' };
const input = document.querySelector('#identity');
const results = document.querySelector('#results');
const grid = document.querySelector('#resultGrid');
const scanBtn = document.querySelector('#scanBtn');
const placeholders = {
  email: 'Enter an email address',
  phone: 'Enter a phone number',
  username: 'Enter a username',
  name: 'Enter a full name'
};

document.querySelectorAll('.tab').forEach(btn => btn.addEventListener('click', () => {
  document.querySelector('.tab.active')?.classList.remove('active');
  btn.classList.add('active');
  state.type = btn.dataset.type;
  input.placeholder = placeholders[state.type];
  input.value = '';
  input.focus();
}));

const esc = value => String(value).replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#039;' }[c]));
const encoded = value => encodeURIComponent(value);
const exact = value => encodeURIComponent(`"${value.replaceAll('"', '')}"`);

function externalLink(url, label) {
  const a = document.createElement('a');
  a.href = url;
  a.target = '_blank';
  a.rel = 'noopener noreferrer nofollow';
  a.textContent = label;
  return a;
}

function sourceData(value) {
  const v = encoded(value);
  const x = exact(value);
  return [
    { cat:'SEARCH', name:'Google', detail:'Exact public-web exposure search', url:`https://www.google.com/search?q=${x}`, action:'Search manually' },
    { cat:'SEARCH', name:'Bing', detail:'Exact public-web exposure search', url:`https://www.bing.com/search?q=${x}`, action:'Search manually' },
    { cat:'SEARCH', name:'DuckDuckGo', detail:'Exact public-web exposure search', url:`https://duckduckgo.com/?q=${x}`, action:'Search manually' },
    { cat:'CODE', name:'GitHub', detail:'Public repositories, issues and code', url:`https://github.com/search?q=${v}&type=code`, action:'Search manually' },
    { cat:'SOCIAL', name:'LinkedIn', detail:'Public profile and indexed exposure', url:`https://www.google.com/search?q=${encoded('site:linkedin.com '+`"${value.replaceAll('"','')}"`)}`, action:'Search manually' },
    { cat:'SOCIAL', name:'Facebook', detail:'Public profile and page exposure', url:`https://www.google.com/search?q=${encoded('site:facebook.com '+`"${value.replaceAll('"','')}"`)}`, action:'Search manually' },
    { cat:'SOCIAL', name:'Instagram', detail:'Public profile/indexed exposure', url:`https://www.google.com/search?q=${encoded('site:instagram.com '+`"${value.replaceAll('"','')}"`)}`, action:'Search manually' },
    { cat:'SOCIAL', name:'Reddit', detail:'Public posts, comments and profiles', url:`https://www.google.com/search?q=${encoded('site:reddit.com '+`"${value.replaceAll('"','')}"`)}`, action:'Search manually' },
    { cat:'DATA BROKER', name:'People-search sites', detail:'Search for profiles and opt-out paths', url:`https://www.google.com/search?q=${encoded('"'+value.replaceAll('"','')+'" '+ 'people search opt out')}`, action:'Find removal path' },
    { cat:'PUBLIC WEB', name:'PDF & documents', detail:'Indexed documents containing the identifier', url:`https://www.google.com/search?q=${encoded('filetype:pdf '+`"${value.replaceAll('"','')}"`)}`, action:'Search manually' },
    { cat:'PUBLIC WEB', name:'Paste / leak references', detail:'Publicly indexed references only', url:`https://www.google.com/search?q=${encoded('"'+value.replaceAll('"','')+'" paste leak')}`, action:'Review carefully' }
  ];
}

const removalDirectory = [
  { name:'Google', type:'Search removal', url:'https://support.google.com/websearch/troubleshooter/9685456' },
  { name:'Bing', type:'Search removal', url:'https://www.microsoft.com/en-us/concern/bing' },
  { name:'GitHub', type:'Public content removal', url:'https://support.github.com/contact/dmca-takedown' },
  { name:'LinkedIn', type:'Account/privacy controls', url:'https://www.linkedin.com/help/linkedin' },
  { name:'Facebook', type:'Privacy / account controls', url:'https://www.facebook.com/help/' },
  { name:'Instagram', type:'Privacy / account controls', url:'https://help.instagram.com/' },
  { name:'Reddit', type:'Privacy / account controls', url:'https://support.reddithelp.com/' },
  { name:'California residents', type:'Data-broker opt-out directory', url:'https://privacy.ca.gov/submit-a-privacy-request/' },
  { name:'FTC', type:'Identity-theft / privacy guidance', url:'https://consumer.ftc.gov/topics/privacy-identity-online-security' }
];

function makeCard(item) {
  const el = document.createElement('article');
  el.className = 'result';
  const badge = document.createElement('span');
  badge.className = 'badge';
  badge.textContent = item.cat || 'REMOVAL';
  const h = document.createElement('h3'); h.textContent = item.name;
  const p = document.createElement('p'); p.textContent = item.detail || item.type;
  el.append(badge, h, p, externalLink(item.url, item.action || 'Open official removal route ↗'));
  return el;
}

function render(value) {
  grid.replaceChildren();
  const searches = sourceData(value);
  searches.forEach(item => grid.appendChild(makeCard(item)));

  const plan = document.createElement('article');
  plan.className = 'result result-wide';
  plan.innerHTML = `<span class="badge">REMOVAL PLAN</span><h3>One scan → one removal queue</h3><p>Use the generated findings above, then work through the official removal routes below. DeleteMe does not send your identifier automatically and does not claim that a third-party breach database can be erased by a browser.</p>`;
  const list = document.createElement('div');
  list.className = 'removal-list';
  removalDirectory.forEach(item => {
    const row = document.createElement('div');
    row.className = 'removal-row';
    const text = document.createElement('span');
    text.innerHTML = `<strong>${esc(item.name)}</strong><small>${esc(item.type)}</small>`;
    row.append(text, externalLink(item.url, 'Open official route ↗'));
    list.appendChild(row);
  });
  plan.appendChild(list);
  grid.appendChild(plan);

  const warning = document.createElement('div');
  warning.className = 'note';
  warning.textContent = 'Important: “all data” means every supported exposure surface in this open-source workflow, not a guaranteed deletion from every system on the internet. Breached copies, private databases and mirrors require action by the relevant controller, host or service. Re-scan after each request to verify removal.';
  grid.appendChild(warning);

  results.hidden = false;
  results.scrollIntoView({ behavior:'smooth', block:'start' });
}

scanBtn.addEventListener('click', () => {
  const value = input.value.trim();
  if (!value) { input.focus(); return; }
  state.value = value;
  scanBtn.disabled = true;
  scanBtn.textContent = 'Building removal plan…';
  setTimeout(() => {
    scanBtn.disabled = false;
    scanBtn.innerHTML = 'Scan exposure <span>↗</span>';
    render(value);
  }, 250);
});

document.querySelector('#clearBtn').addEventListener('click', () => {
  state.value = '';
  input.value = '';
  grid.replaceChildren();
  results.hidden = true;
  input.focus();
});

input.addEventListener('keydown', e => {
  if (e.key === 'Enter') { e.preventDefault(); scanBtn.click(); }
});

window.addEventListener('pagehide', () => {
  state.value = '';
  input.value = '';
});
