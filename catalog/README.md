# Kairos game catalog

The app reads this file from `main` (`https://raw.githubusercontent.com/prashantchataut/Kairos/main/catalog/games.json`)
and merges it over the copy bundled in the APK, so you can add or fix games without shipping an update.
You can point the app at any other catalog URL in **Settings → Game catalog**.

## Entry kinds

| kind | what the app does |
|---|---|
| `original` | A built-in Kairos game (`word-bloom`, `pip-raindrops`, `memory-pond`). |
| `web` | Opens `url` full-screen inside Kairos. |
| `download` | Downloads `url` (or the newest GitHub release asset of `github`, matched by `assetPattern`) in the background, then installs it (`.apk`) or hands it to `runner`. |
| `guide` | Links to the official `homepage` and shows `steps` for running it with `runner`. |

```json
{
  "id": "my-game",
  "title": "My Game",
  "author": "Me",
  "kind": "download",
  "github": "me/my-game",
  "assetPattern": "\\.apk$",
  "runner": "apk",
  "accent": "sky",
  "tagline": "One line.",
  "description": "A paragraph.",
  "tags": ["fan game"]
}
```

Only list games from their authors' official sources. Fan games are unofficial and not affiliated
with Nintendo, Game Freak or The Pokémon Company.
