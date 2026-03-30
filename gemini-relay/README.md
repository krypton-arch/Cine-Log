# Gemini Relay

This relay keeps `GEMINI_API_KEY` on the server side so the Android app never ships it in the APK.

## Run locally

1. Copy `.env.example` to `.env`
2. Start the relay:

```bash
node server.mjs
```

3. Point the Android app at it:

```properties
GEMINI_PROXY_BASE_URL=http://10.0.2.2:8787
```

`10.0.2.2` lets the Android emulator talk to a server running on your machine.
For a physical Android device on the same Wi-Fi, use your computer's LAN IP instead of `10.0.2.2`.

Keep the real `GEMINI_API_KEY` only in `.env` or in your hosting provider's secret settings. Do not commit it to `.env.example`.
