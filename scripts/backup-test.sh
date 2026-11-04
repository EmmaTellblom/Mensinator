#!/usr/bin/env bash
# Checks app data is backed up only when the transport encrypts client-side (Seedvault-style),
# and that an encrypted backup restores every file byte for byte.
set -euxo pipefail
PKG=com.mensinator.app.debug
TRANSPORT=com.android.localtransport/.LocalTransport
TMP=$(mktemp -d)

app() { adb shell "run-as $PKG sh -c '$1'"; }
# "size sha256 path" for every file Android auto-backup should cover.
SNAP='find databases shared_prefs files -type f 2>/dev/null | sort | while read f; do echo "$(stat -c %s "$f") $(sha256sum "$f")"; done'
snapshot() { app "$SNAP" | tr -d '\r'; }
has_db() { app 'ls databases' 2>/dev/null | grep -q periods.db; }
restore() {
  adb shell pm clear $PKG
  token=$(adb shell bmgr list sets | awk 'NF{print $1; exit}')
  [ -n "$token" ] || return 1
  adb shell bmgr restore "$token" $PKG
}

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p $PKG -c android.intent.category.LAUNCHER 1
for _ in $(seq 30); do has_db && break; sleep 1; done
has_db
adb shell am force-stop $PKG

# Seed known rows into the real DB (pull, edit on host, push back).
adb exec-out run-as $PKG cat databases/periods.db > "$TMP/periods.db"
if app 'ls databases' | grep -q 'periods.db-wal'; then
  adb exec-out run-as $PKG cat databases/periods.db-wal > "$TMP/periods.db-wal"
fi
python3 -I - "$TMP/periods.db" <<'EOF'
import sqlite3, sys
db = sqlite3.connect(sys.argv[1])
db.execute("INSERT INTO periods (date, period_id) VALUES ('2000-01-01', 999)")
db.execute("INSERT INTO ovulations (date) VALUES ('2000-01-15')")
db.commit()
db.close()
EOF
adb push "$TMP/periods.db" /data/local/tmp/periods.db
adb shell chmod 644 /data/local/tmp/periods.db
app 'rm -f databases/periods.db-wal databases/periods.db-shm databases/periods.db-journal'
app 'cp /data/local/tmp/periods.db databases/periods.db'
adb shell rm /data/local/tmp/periods.db

# Markers in other domains: files/ and shared_prefs/ must be restored, cache/ and no_backup/ must not.
app 'mkdir -p files shared_prefs cache no_backup'
app 'echo backup-marker > files/marker.txt'
app 'echo "<map><string name=\"k\">v</string></map>" > shared_prefs/backup_test.xml'
app 'echo x > cache/marker.txt; echo x > no_backup/marker.txt'

snapshot > "$TMP/before.txt"
cat "$TMP/before.txt"
grep -q ' databases/periods.db$' "$TMP/before.txt"
grep -q ' files/marker.txt$' "$TMP/before.txt"
grep -q ' shared_prefs/backup_test.xml$' "$TMP/before.txt"

adb shell bmgr enable true
adb shell bmgr transport $TRANSPORT

# Encrypted transport: every file must come back identical (size + hash).
adb shell bmgr wipe $TRANSPORT $PKG
adb shell settings put secure backup_local_transport_parameters fake_encryption_flag=true
adb shell bmgr backupnow $PKG | tee "$TMP/backup.txt"
grep -q 'Success' "$TMP/backup.txt"
restore
snapshot > "$TMP/after.txt"
diff "$TMP/before.txt" "$TMP/after.txt"
if app 'ls cache/marker.txt no_backup/marker.txt' 2>/dev/null | grep -q marker; then
  echo "cache/ or no_backup/ was restored"; exit 1
fi
adb exec-out run-as $PKG cat databases/periods.db > "$TMP/restored.db"
python3 -I - "$TMP/restored.db" <<'EOF'
import sqlite3, sys
db = sqlite3.connect(sys.argv[1])
assert db.execute("PRAGMA integrity_check").fetchone()[0] == "ok"
assert db.execute("SELECT 1 FROM periods WHERE date='2000-01-01' AND period_id=999").fetchone()
assert db.execute("SELECT 1 FROM ovulations WHERE date='2000-01-15'").fetchone()
EOF

# App must start fine on restored data.
adb logcat -c
adb shell monkey -p $PKG -c android.intent.category.LAUNCHER 1
sleep 5
adb shell pidof $PKG
if adb logcat -d -b crash | grep -q "$PKG"; then adb logcat -d -b crash; exit 1; fi

# Unencrypted transport: nothing must be backed up.
adb shell am force-stop $PKG
adb shell bmgr wipe $TRANSPORT $PKG
adb shell settings put secure backup_local_transport_parameters fake_encryption_flag=false
adb shell bmgr backupnow $PKG || true
restore || true
if [ -n "$(snapshot)" ]; then snapshot; echo "Data was backed up without encryption"; exit 1; fi
