import sys, glob, re
ver, title, es, en, bullets, ctx = sys.argv[1:7]
for f in glob.glob('src/main/resources/assets/gripeverde/changelog/*.md'):
    text = f'## Version {ver} - {title}\n\n' + (es if '/es_' in f else en).rstrip() + '\n\n'
    s=open(f,encoding='utf-8').read()
    if f'Version {ver}' in s: continue
    open(f,'w',encoding='utf-8').write(text+s)
s=open('CHANGELOG.md',encoding='utf-8').read()
if f'## v{ver}' not in s:
    open('CHANGELOG.md','w',encoding='utf-8').write(f"## v{ver} - {title}\n\n{bullets}\n\n"+s)
p='docs/CONTEXTO_MODMENU.md'
s=open(p,encoding='utf-8').read()
s=re.sub(r'Versión de esta rama: `[0-9.]+`', f'Versión de esta rama: `{ver}`', s, count=1)
lines=s.split('\n')
rows=[(i,int(m.group(1))) for i,l in enumerate(lines) if (m:=re.match(r'^\| (\d+) \|', l))]
idx=max(i for i,_ in rows); last=max(r for _,r in rows)
lines.insert(idx+1, f'| {last+1} | {ctx} | {ver} |')
s='\n'.join(lines)
i=s.index('## Cambios de')
s=s[:i]+f"## Cambios de {ver} — {title}\n\n{bullets}\n\n"+s[i:]
open(p,'w',encoding='utf-8').write(s)
b=open('build.gradle',encoding='utf-8').read()
open('build.gradle','w',encoding='utf-8').write(re.sub(r"version = '[0-9.]+'", f"version = '{ver}'", b, count=1))
p='src/main/java/com/eternalcraft/gripeverde/GripeVerdeMod.java'
s=open(p,encoding='utf-8').read()
open(p,'w',encoding='utf-8').write(re.sub(r'VERSION = "[0-9.]+"', f'VERSION = "{ver}"', s, count=1))
open('release-tag.txt','w',encoding='utf-8').write(f'v{ver}\n')
print('bump', ver)
