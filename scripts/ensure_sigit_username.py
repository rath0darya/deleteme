from pathlib import Path

p = Path('android/app/src/main/java/com/rath0darya/deleteme/MainActivity.java')
s = p.read_text(encoding='utf-8')
old = 'private void loadUsername(String u)throws Exception{String q=URLEncoder.encode(u,"UTF-8");if(code(GH+"users/"+q)==200)publicMatches.add(new PublicMatch("GitHub","Public profile","https://github.com/"+u,"Public username match."));String gl=GL+"users?username="+q;if(code(gl)==200){JSONArray a=new JSONArray(get(gl));if(a.length()>0)publicMatches.add(new PublicMatch("GitLab","Public profile","https://gitlab.com/"+u,"Public username match."));}}'
new = 'private void loadUsername(String u)throws Exception{for(SigitUserRecon.Match m:SigitUserRecon.scan(u))publicMatches.add(new PublicMatch(m.source,"Public username match",m.url,m.status+". Results are public-profile matches, not identity proof."));}'
if 'SigitUserRecon.scan(u)' in s:
    print('SIGIT username reconnaissance already integrated')
elif old in s:
    s=s.replace(old,new,1)
    p.write_text(s,encoding='utf-8')
    print('SIGIT username reconnaissance integrated successfully.')
else:
    raise SystemExit('Expected username implementation not found; refusing to modify source.')

# Keep the Scan screen description aligned with the native SIGIT integration.
old_help = 'Checks public GitHub and GitLab profiles. Matches are possible matches, not identity proof.'
new_help = 'Checks the integrated SIGIT public username-recon source list. Matches are possible matches, not identity proof.'
if old_help in s:
    s=s.replace(old_help,new_help,1)
    p.write_text(s,encoding='utf-8')
    print('Username search help text updated for SIGIT.')
elif new_help in s:
    print('Username search help text already reflects SIGIT.')
else:
    raise SystemExit('Expected username help text not found; refusing to modify source.')
