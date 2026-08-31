const API = 'http://127.0.0.1:8765';
const $ = (id) => document.getElementById(id);
let pollTimer = null;

const fields = {
  first_name: 'firstName', last_name: 'lastName', email: 'email', phone: 'phone',
  address: 'address', city: 'city', state: 'state', postal_code: 'postalCode', country: 'country'
};

function profile() {
  const p = {};
  for (const [key, id] of Object.entries(fields)) p[key] = $(id).value.trim();
  p.aliases = $('aliases').value.split(',').map(x => x.trim()).filter(Boolean);
  return p;
}

function setEngine(ok, text) {
  $('engineBar').classList.toggle('online', ok);
  $('engineText').textContent = text;
}

async function api(path, options = {}) {
  const r = await fetch(API + path, options);
  if (!r.ok) throw new Error(await r.text() || `HTTP ${r.status}`);
  return r.json();
}

async function checkEngine() {
  try {
    const health = await api('/api/health');
    setEngine(true, 'Local removal engine connected');
    const b = await api('/api/brokers');
    $('brokerCount').textContent = `${b.count.toLocaleString('en-IN')} brokers`;
    return true;
  } catch (e) {
    setEngine(false, 'Local engine offline — start engine/server.py');
    $('brokerCount').textContent = 'Engine offline';
    return false;
  }
}

function validProfile(p) {
  if (!p.first_name || !p.last_name) return 'First name and last name are required.';
  if (!p.email && !p.phone) return 'Add at least an email or phone number.';
  return '';
}

function statusCard(item) {
  const el = document.createElement('article');
  el.className = `result status-${item.status}`;
  const badge = document.createElement('span');
  badge.className = 'badge'; badge.textContent = item.status.toUpperCase();
  const h = document.createElement('h3'); h.textContent = item.company;
  const p = document.createElement('p'); p.textContent = item.detail || item.domain || '';
  el.append(badge, h, p);
  return el;
}

function renderStatus(s) {
  $('results').hidden = false;
  $('totalStat').textContent = (s.total || 0).toLocaleString('en-IN');
  $('submittedStat').textContent = (s.submitted || 0).toLocaleString('en-IN');
  $('manualStat').textContent = (s.manual || 0).toLocaleString('en-IN');
  $('failedStat').textContent = (s.failed || 0).toLocaleString('en-IN');
  const pct = s.total ? Math.round((s.completed / s.total) * 100) : 0;
  $('progressBar').style.width = `${pct}%`;
  $('runState').textContent = s.running ? `Running · ${pct}%` : (s.completed ? 'Completed' : 'Idle');
  $('runTitle').textContent = s.running ? 'Removing your data…' : 'Removal progress';
  const grid = $('resultGrid');
  grid.replaceChildren();
  (s.items || []).slice().reverse().forEach(item => grid.appendChild(statusCard(item)));
  if (s.running) pollTimer = setTimeout(refreshStatus, 1500);
}

async function refreshStatus() {
  try { renderStatus(await api('/api/status')); }
  catch { setEngine(false, 'Local engine connection lost'); }
}

$('removeBtn').addEventListener('click', async () => {
  const p = profile();
  const error = validProfile(p);
  if (error) { alert(error); return; }
  if (!(await checkEngine())) { alert('Start the local engine first: python engine/server.py'); return; }
  $('removeBtn').disabled = true;
  $('removeBtn').innerHTML = 'Starting removal…';
  try {
    await api('/api/remove-all', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({profile:p}) });
    renderStatus({running:true,total:0,completed:0,submitted:0,manual:0,failed:0,items:[]});
    await refreshStatus();
  } catch (e) {
    alert(`Could not start removal: ${e.message}`);
  } finally {
    $('removeBtn').disabled = false;
    $('removeBtn').innerHTML = 'Remove my data <span>→</span>';
  }
});

$('resetBtn').addEventListener('click', () => {
  for (const id of Object.values(fields)) $(id).value = '';
  $('country').value = 'India'; $('aliases').value = '';
});

checkEngine();
refreshStatus();
