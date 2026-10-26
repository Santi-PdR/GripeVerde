#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# Espejo Python de los 8 sellos Gradle (verifyGripeVerde*) de build.gradle.
# No sustituye al build; reproduce sus chequeos para validar en el sandbox.
# v0.25.2: movido de /home/user/verify_mirrors.py (fuera del repo, se perdia)
#          a tools/verify_mirrors.py (versionado). El sello 8 (literales
#          Java) quedo anunciado en v0.24.1 pero nunca se escribio: entra en
#          vigor aqui, dentro del repo.
# Uso: python3 tools/verify_mirrors.py   (desde cualquier cwd)
import json, os, re, sys, glob, struct

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG_DIR = os.path.join(ROOT, "src/main/resources/assets/gripeverde/lang")
CHG_DIR = os.path.join(ROOT, "src/main/resources/assets/gripeverde/changelog")
SOUNDS_JSON = os.path.join(ROOT, "src/main/resources/assets/gripeverde/sounds.json")
SOUNDS_BASE = os.path.join(ROOT, "src/main/resources/assets/gripeverde/sounds")
JAVA_DIR = os.path.join(ROOT, "src/main/java")
REQUIRED = ['en_us', 'es_es']

fails = []

def read(p):
    with open(p, encoding="utf-8") as f: return f.read()

# 1) verifyGripeVerdeLocalization
build = read(os.path.join(ROOT, "build.gradle"))
m = re.search(r"^version = '([^']+)'", build, re.M); assert m, "version not found"
VERSION = m.group(1)

lang_files = {os.path.basename(p)[:-5] for p in glob.glob(LANG_DIR + "/*.json")}
chg_files = {os.path.basename(p)[:-3] for p in glob.glob(CHG_DIR + "/*.md")}
if set(REQUIRED) - lang_files: fails.append("missing langs: %s" % sorted(set(REQUIRED)-lang_files))
if lang_files - set(REQUIRED): fails.append("unexpected langs: %s" % sorted(lang_files-set(REQUIRED)))
if set(REQUIRED) - chg_files: fails.append("missing changelogs: %s" % sorted(set(REQUIRED)-chg_files))
if chg_files - set(REQUIRED): fails.append("unexpected changelogs: %s" % sorted(chg_files-set(REQUIRED)))

baseline = json.loads(read(os.path.join(LANG_DIR, "en_us.json")))
bkeys = set(baseline)
EXPECTED = 165
loc_problems = []
if len(bkeys) != EXPECTED:
    loc_problems.append("en_us: expected %d keys, found %d" % (EXPECTED, len(bkeys)))
langs = {}
for loc in REQUIRED:
    try:
        entries = json.loads(read(os.path.join(LANG_DIR, loc + ".json")))
        assert isinstance(entries, dict)
        langs[loc] = entries
        keys = set(entries)
        if bkeys - keys: loc_problems.append("%s: missing %s" % (loc, sorted(bkeys-keys)))
        if keys - bkeys: loc_problems.append("%s: unexpected %s" % (loc, sorted(keys-bkeys)))
        bad = [k for k, v in entries.items() if not isinstance(v, str) or not v.strip()]
        if bad: loc_problems.append("%s: blank %s" % (loc, sorted(bad)))
    except Exception as e:
        loc_problems.append("%s: invalid JSON (%s)" % (loc, e))
    chg = read(os.path.join(CHG_DIR, loc + ".md"))
    if ("## Version " + VERSION) not in chg:
        loc_problems.append("%s: changelog missing Version %s" % (loc, VERSION))
if loc_problems: fails.append("LOCALIZATION:\n" + "\n".join(loc_problems))

# 2) verifyGripeVerdeSoundAssets
snd_problems = []
parsed = json.loads(read(SOUNDS_JSON))
referenced = set()
for ev, payload in parsed.items():
    if not re.fullmatch(r"[a-z0-9/._-]+", ev) or ev.startswith("/") or any(s in ("..",".") for s in ev.split("/")):
        snd_problems.append("invalid event id %s" % ev); continue
    if not isinstance(payload, dict) or not isinstance(payload.get("sounds"), list) or not payload["sounds"]:
        snd_problems.append("%s: sounds must be a non-empty list" % ev); continue
    for i, entry in enumerate(payload["sounds"]):
        raw = entry if isinstance(entry, str) else (entry.get("name") if isinstance(entry, dict) else None)
        typ = entry.get("type", "file") if isinstance(entry, dict) else "file"
        if not isinstance(raw, str) or not raw.strip():
            snd_problems.append("%s[%d]: blank name" % (ev, i)); continue
        if typ not in ("file", "event"):
            snd_problems.append("%s[%d]: bad type %s" % (ev, i, typ)); continue
        raw = raw.strip()
        ns, _, path = raw.partition(":")
        ns = ns or "gripeverde"
        path = path or raw
        if not re.fullmatch(r"[a-z0-9_.-]+", ns) or not re.fullmatch(r"[a-z0-9/._-]+", path) \
           or path.startswith("/") or any(s in ("..",".") for s in path.split("/")):
            snd_problems.append("%s[%d]: invalid id %s" % (ev, i, raw)); continue
        if ns != "gripeverde": continue
        if typ == "event":
            if raw.split(":",1)[-1] not in parsed: snd_problems.append("%s[%d]: unknown event %s" % (ev, i, raw))
            continue
        rel = path + ".ogg"
        referenced.add(rel)
        if not os.path.isfile(os.path.join(SOUNDS_BASE, rel)):
            snd_problems.append("%s[%d]: missing %s" % (ev, i, rel))
available = set()
for dp, _, fns in os.walk(SOUNDS_BASE):
    for fn in fns:
        if fn.endswith(".ogg"):
            available.add(os.path.relpath(os.path.join(dp, fn), SOUNDS_BASE).replace("\\", "/"))
orphans = available - referenced
if orphans: snd_problems.append("unreferenced OGG: " + ", ".join(sorted(orphans)))
if snd_problems: fails.append("SOUND ASSETS:\n" + "\n".join(snd_problems))

# 3) verifyGripeVerdeNoDeadCode
banned = ['ThemeManager','GhoulPalette','ScreenLayout','ScreenTheme','AbstractScreenTheme',
 'UIComponent','UIScale','IconButton','SilentButton','DiarySeparator','gripeverde_steel',
 'steel_pack','bg_town','BG_TOWN','hasBgTown','ResourcePackHandler','ContainerOverlayHandler',
 'setHoverSound','setClickSound','gripeverde.mixins','gripeverde.refmap','VictorianFrame',
 'AbstractDiaryScreen','DebugCanvasScreen','SoundLabScreen','DiaryDecorator','CandleLight',
 'InkEffects','DiaryVideoSettingsScreen','drawVictorianFrame','drawQuillCursor',
 'drawVerticalDivider','drawDarkLeatherBackground','drawVersionPanel','getMoonPhaseIndex',
 'getOpenCount','drawSolidBlob','drawRowBands','openTelemetry','scrollToSection','drawGreeting',
 'drawSelectionLedger','drawHeraldTicker','drawStatusPill','drawCorrespondenceRack',
 'drawSignalStation','drawBottomNotice','drawSpecimenFooter','computePublicSignal',
 'drawLedgerDetails','drawOrnamentalDivider']
pat = re.compile(r"(?i)\b(?:" + "|".join(banned) + r")\b")
dc_problems = []
java_files = glob.glob(JAVA_DIR + "/**/*.java", recursive=True)
for f in java_files:
    for n, line in enumerate(read(f).splitlines(), 1):
        t = line.strip()
        if t.startswith("*") or t.startswith("//") or t.startswith("/*"): continue
        if pat.search(t): dc_problems.append("%s:%d: %s" % (os.path.basename(f), n, t))
if dc_problems: fails.append("DEAD CODE:\n" + "\n".join(dc_problems))

# 4) verifyGripeVerdeVersionSync
gsrc = read(os.path.join(JAVA_DIR, "com/eternalcraft/gripeverde/GripeVerdeMod.java"))
mm = re.search(r'VERSION\s*=\s*"([^"]+)"', gsrc); assert mm, "VERSION const missing"
if mm.group(1) != VERSION:
    fails.append("VERSION SYNC: GripeVerdeMod.VERSION=%s vs build.gradle=%s" % (mm.group(1), VERSION))

# 5) verifyGripeVerdeMusicDurations
def ogg_duration_ms(path):
    data = open(path, "rb").read(); ln = len(data)
    rate = None; last_granule = -1; pos = 0
    while True:
        idx = data.find(b"OggS", max(0, pos))
        if idx < 0 or idx + 27 > ln: break
        granule = struct.unpack_from("<q", data, idx + 6)[0]
        nsegs = data[idx + 26]
        seg_start = idx + 27
        if seg_start + nsegs > ln: break
        body_len = sum(data[seg_start:seg_start + nsegs])
        body_start = seg_start + nsegs
        if rate is None and body_start + 16 <= ln and data[body_start:body_start+7] == b"\x01vorbis":
            rate = struct.unpack_from("<I", data, body_start + 12)[0]
        if granule >= 0: last_granule = granule
        pos = body_start + body_len
    assert rate and last_granule >= 0, "cannot parse %s" % path
    return last_granule / rate * 1000.0

mus = read(os.path.join(JAVA_DIR, "com/eternalcraft/gripeverde/client/audio/MenuMusic.java"))
tm = re.search(r"TRACK_DURATIONS_MS\s*=\s*\{([^}]*)\}", mus); assert tm, "durations table missing"
declared = [int(x.replace("_", "")) for x in re.findall(r"(\d[\d_]*)", tm.group(1))]
music_files = sorted(glob.glob(os.path.join(SOUNDS_BASE, "music", "*.ogg")))
mus_problems = []
if len(declared) != len(music_files):
    mus_problems.append("table %d vs files %d" % (len(declared), len(music_files)))
for idx, d in enumerate(declared):
    fp = os.path.join(SOUNDS_BASE, "music", "music_title_%d.ogg" % (idx + 1))
    if not os.path.isfile(fp): mus_problems.append("missing music_title_%d.ogg" % (idx+1)); continue
    real = round(ogg_duration_ms(fp))
    if abs(real - d) > 1500:
        mus_problems.append("music_title_%d.ogg: real=%dms vs declared=%dms" % (idx+1, real, d))
if mus_problems: fails.append("MUSIC DURATIONS:\n" + "\n".join(mus_problems))

# 6) verifyGripeVerdeSubtitles
sub_problems = []
for ev, payload in parsed.items():
    sub = payload.get("subtitle") if isinstance(payload, dict) else None
    if isinstance(sub, str) and sub.strip():
        for loc in REQUIRED:
            v = langs.get(loc, {}).get(sub)
            if not isinstance(v, str) or not v.strip():
                sub_problems.append("%s: subtitle %s missing/blank in %s" % (ev, sub, loc))
if sub_problems: fails.append("SUBTITLES:\n" + "\n".join(sub_problems))

# 7) verifyGripeVerdeSoundUsage
# v0.26.0: allowlist vacia — los 8 legacy quedaron cableados a sus eventos
# en UiSounds (V2-resto). Si uno queda huerfano otra vez, el sello falla.
legacy = set()
modsrc = read(os.path.join(JAVA_DIR, "com/eternalcraft/gripeverde/client/audio/ModSounds.java"))
fields = set(re.findall(r"RegistryObject<SoundEvent>\s+(\w+)\s*=\s*reg\(", modsrc))
code = "".join(read(f) for f in java_files if not f.endswith("ModSounds.java"))
unused = {f for f in fields if f not in code}
use_problems = []
for f in sorted(unused - legacy):
    use_problems.append("%s: registrado sin uso" % f)
for f in sorted(legacy - unused):
    use_problems.append("%s: ya en uso, sacar del allowlist" % f)
if use_problems: fails.append("SOUND USAGE:\n" + "\n".join(use_problems))

# 8) verifyJavaLiterals (v0.24.1 anunciado / v0.25.2 escrito de verdad):
#    errata 0xD20PL — la P no es digito hex y compileJava cayo. Lint de todos
#    los literales numericos de los .java tras retirar comentarios y cadenas
#    (preservando saltos de linea para reportar la linea exacta).
def _blank(mobj):
    return re.sub(r"[^\n]", " ", mobj.group(0))

def strip_for_literals(txt):
    txt = re.sub(r"/\*.*?\*/", _blank, txt, flags=re.S)
    txt = re.sub(r"//[^\n]*", _blank, txt)
    txt = re.sub(r'"(?:\\.|[^"\\\n])*"', _blank, txt)
    txt = re.sub(r"'(?:\\.|[^'\\\n])*'", _blank, txt)
    return txt

NUM_TOKEN = re.compile(r"(?<![\w$.])[0-9][0-9A-Za-z_.]*")

def valid_num(tok):
    t = tok.replace("_", "")
    tl = t.lower()
    if tl.startswith("0x"):
        body = t[2:]
        if not body: return False
        if "p" in tl or "." in body:
            # hex float: sufijo solo f/d; el L es ilegal en flotantes
            if body[-1] in "lL": return False
            core = body[:-1] if body[-1] in "fFdD" else body
            return bool(re.fullmatch(r"[0-9A-Fa-f]*(?:\.[0-9A-Fa-f]*)?[pP][+-]?[0-9]+", core))
        # F/D aqui son digitos hex, no sufijos; solo L es sufijo valido
        core = body[:-1] if body[-1] in "lL" else body
        return bool(re.fullmatch(r"[0-9A-Fa-f]+", core))
    if tl.startswith("0b"):
        body = t[2:]
        if not body: return False
        core = body[:-1] if body[-1] in "lL" else body
        return bool(re.fullmatch(r"[01]+", core))
    # decimal (o artefacto raro que no se juzga)
    suf = ""
    if t and t[-1] in "fFdDlL":
        suf, t = t[-1], t[:-1]
    if not t: return False
    if t[-1] in "eE": return True  # fragmento e+/e- de notacion cientifica; la P suelta si es errata real
    if suf and suf in "lL" and re.search(r"[.eE]", t): return False
    if not re.fullmatch(r"[0-9]+(?:\.[0-9]*)?(?:[eE][+-]?[0-9]+)?", t): return False
    core = re.split(r"[.eE]", t)[0]
    if len(core) > 1 and core[0] == "0" and "." not in t and "e" not in tl:
        if re.search(r"[89]", core): return False  # octal ilegal
    return True

lit_problems = []
checked_literals = 0
for f in java_files:
    raw = read(f)
    stripped = strip_for_literals(raw)
    for mobj in NUM_TOKEN.finditer(stripped):
        tok = mobj.group(0).rstrip(".")
        if not tok: continue
        checked_literals += 1
        if not valid_num(tok):
            line = stripped[:mobj.start()].count("\n") + 1
            lit_problems.append("%s:%d: literal invalido %s" % (os.path.basename(f), line, tok))
if lit_problems: fails.append("JAVA LITERALS:\n" + "\n".join(lit_problems))

# Extra sandbox-only: balance sintactico crudo de llaves/parentesis en Java
bal_problems = []
for f in java_files:
    txt = read(f)
    stripped = re.sub(r"/\*.*?\*/", "", txt, flags=re.S)
    stripped = re.sub(r"//[^\n]*", "", stripped)
    stripped = re.sub(r'"(?:\\.|[^"\\\n])*"', '""', stripped)
    stripped = re.sub(r"'(?:\\.|[^'\\\n])*'", "''", stripped)
    for a, b, name in [("{", "}", "braces"), ("(", ")", "parens")]:
        if stripped.count(a) != stripped.count(b):
            bal_problems.append("%s: %s %d vs %d" % (os.path.basename(f), name, stripped.count(a), stripped.count(b)))
if bal_problems: fails.append("JAVA BALANCE (sandbox-extra):\n" + "\n".join(bal_problems))

print("Version:", VERSION)
print("Java files:", len(java_files), "| langs:", len(langs), "| baseline keys:", len(bkeys),
      "| sound events:", len(parsed), "| OGG refs:", len(referenced), "| OGG files:", len(available),
      "| music:", len(declared), "| literals:", checked_literals)
if fails:
    print("\n=== FALLOS ===")
    for f in fails: print(f, "\n---")
    sys.exit(1)
print("8/8 sellos espejo OK + balance sintactico OK")
