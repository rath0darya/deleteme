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
