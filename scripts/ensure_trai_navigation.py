from pathlib import Path

p = Path('android/app/src/main/java/com/rath0darya/deleteme/MainActivity.java')
s = p.read_text(encoding='utf-8')
old = 'if(id==R.id.nav_scan){showScan();}else if(id==R.id.nav_results){showResults();}else if(id==R.id.nav_settings){showSettings();}'
new = 'if(id==R.id.nav_scan){showScan();}else if(id==R.id.nav_results){showResults();}else if(id==R.id.nav_trai){startActivity(new Intent(this,TraiHeaderActivity.class));}else if(id==R.id.nav_settings){showSettings();}'
if old not in s:
    if 'R.id.nav_trai' in s:
        raise SystemExit('TRAI navigation already integrated')
    raise SystemExit('Expected navigation handler not found')
s = s.replace(old, new, 1)
p.write_text(s, encoding='utf-8')
print('TRAI navigation integrated into MainActivity')
