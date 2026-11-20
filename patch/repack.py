import sys, zipfile, os
base, dexdir, out = sys.argv[1], sys.argv[2], sys.argv[3]
repl = {}
for n in os.listdir(dexdir):
    if n.endswith(".dex"):
        repl[n] = os.path.join(dexdir, n)
zin = zipfile.ZipFile(base, "r")
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as zout:
    for item in zin.infolist():
        data = repl.get(item.filename)
        if data:
            with open(data, "rb") as f:
                payload = f.read()
        else:
            payload = zin.read(item.filename)
        zi = zipfile.ZipInfo(item.filename, date_time=item.date_time)
        zi.compress_type = zipfile.ZIP_STORED if item.compress_type == zipfile.ZIP_STORED else zipfile.ZIP_DEFLATED
        zi.external_attr = item.external_attr
        zout.writestr(zi, payload)
print("wrote", out, os.path.getsize(out))
