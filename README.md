<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="100" />
</p>

# DiaTonomy

DiaTonomy reads dose history off a NovoPen 6 (or NovoPen Echo Plus) over NFC and syncs it to a self-hosted [Nightscout](https://github.com/nightscout/cgm-remote-monitor) instance — no manual logging, no proprietary cloud app required.

It supports two pens simultaneously (e.g. a bolus pen and a basal pen), keeps a local journal of everything scanned, and queues doses for retry if your Nightscout server is temporarily unreachable.

## How it works

```
NovoPen 6 (NFC tap)
      |
      v
Android app (Kotlin, via nvplib-core / nvplib-nfc)
      |
      v
Local dose journal (Room database)
      |
      v
POST to your Nightscout instance
```

Nothing is sent anywhere until you've configured a Nightscout URL and API secret in the app's Settings screen, and until each pen you scan has been registered as Bolus or Basal.

## Prerequisites

- A running Nightscout instance you control (self-hosted or otherwise), reachable from your phone
- Your Nightscout `API_SECRET` (the plain-text value — DiaTonomy hashes it automatically)
- A NovoPen 6 or NovoPen Echo Plus
- An Android phone with NFC
- Android Studio, if you want to build from source

## Setting up Nightscout (if you don't already have an instance)

This project was built and tested against a self-hosted Nightscout running via Docker/Podman Compose. A minimal `docker-compose.yml`:

```yaml
services:
  mongo:
    image: mongo:6
    restart: always
    volumes:
      - ./mongo-data:/data/db

  nightscout:
    image: nightscout/cgm-remote-monitor:latest
    restart: always
    depends_on:
      - mongo
    ports:
      - "1337:1337"
    environment:
      NODE_ENV: production
      MONGO_CONNECTION: mongodb://mongo:27017/nightscout
      API_SECRET: "changeThisToSomethingLongAndRandom"
      DISPLAY_UNITS: mmol
      ENABLE: "treatmentnotify basal careportal"
      AUTH_DEFAULT_ROLES: denied
      INSECURE_USE_HTTP: "true"
```

`API_SECRET` must be at least 12 characters. `INSECURE_USE_HTTP` is needed unless you're putting a TLS-terminating reverse proxy in front of Nightscout — without it, Nightscout will redirect every request to HTTPS and the app won't be able to connect.

Bring it up with `docker compose up -d` (or `podman-compose up -d`), and confirm it's reachable at `http://<your-server-ip>:1337` before moving on to the app.

If your Nightscout server only runs on your home network (as is common for a self-hosted setup like this), the app will only be able to sync while your phone is on the same network — doses scanned while away are queued locally and sync automatically the next time you're back on that network.

## Building the app

1. Clone this repo and open it in Android Studio
2. Let Gradle sync (this pulls in `nvplib-core`/`nvplib-nfc` from Maven Central, along with Room, OkHttp, and WorkManager)
3. Connect an Android phone via USB with Developer Options + USB debugging enabled
4. Run the app (Shift+F10), or build a standalone APK via `Build → Generate Signed App Bundle/APK` if you want something installable without a cable

## First-run setup

1. Open the app → **Settings**
2. Enter your Nightscout URL (e.g. `http://192.168.1.x:1337`) and your plain-text `API_SECRET`
3. Save, then tap your first pen against your phone's NFC antenna
4. You'll be prompted to register the pen as **Bolus** or **Basal**, with an optional nickname — this only happens once per pen
5. Repeat for a second pen if you use separate pens for bolus and basal insulin

From then on, tapping a registered pen reads its dose history, logs any new doses to the on-device journal, and syncs them to Nightscout.

## Notes on how dose tracking works

- The pen's memory is read-only — scanning it never clears or modifies anything on the pen itself, so re-scanning is always safe
- Duplicate syncing is avoided using a per-pen "last synced" watermark plus a local dedup check, so tapping the same pen repeatedly won't create duplicate Nightscout entries
- If a sync fails (e.g. phone off Wi-Fi, server unreachable), the doses are queued locally and retried automatically via a background worker once connectivity returns
- Primer/test shots taken before a real injection are logged like any other dose — the app doesn't attempt to filter them out, since the pen doesn't reliably distinguish them from a real dose in its own data

## Disclaimer

This is a personal project, not a medical device, and is not affiliated with or endorsed by Novo Nordisk, Dexcom, or the Nightscout Foundation. Use at your own risk. Always verify displayed data against your pen's own screen if you have any doubt about accuracy.

## License

MIT — see [LICENSE](LICENSE).
