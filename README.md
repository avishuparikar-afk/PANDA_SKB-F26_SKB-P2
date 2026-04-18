## 👥 Team Details

**Team Name:** PANDA  
**Team ID:** SKB-F26  

### 👨‍💻 Team Leader:
- AVISH UPARIKAR  

### 👨‍💻 Team Members:
1. SAGAR SHESHRAO MANKAR  
2. SAURABH MESHRAM  
3. DIPAK SUNIL DHURVE  
4. PRAJWAL MUNDRE  


---

## 🎯 What this is

An offline-first Android app for rural farmers to detect livestock diseases
early, get home-care guidance, and know when to call the vet — all designed
for low-literacy users (image-first UI, big buttons, Hindi / Marathi /
English labels).

The signature moment is **Pashupatinath Mode** — a cinematic cosmic Shiva
blessing sequence that plays after a successful herd scan.

---

## 📁 Project layout

```
PASHU-RAKSHA/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/pashuraksha/
│       │   ├── SplashActivity.kt
│       │   ├── MainActivity.kt           (bottom-nav host)
│       │   ├── CosmicEnergyActivity.kt   ← Pashupatinath Mode (video + particles)
│       │   ├── SplashAnimationView.kt    (custom-drawn energy lines / orb / spiral)
│       │   ├── HomeFragment.kt / ScanFragment.kt / DiseaseDetectionFragment.kt …
│       │   └── data/
│       │       ├── Models.kt                  (Disease, Symptom, Urgency, DiagnosisResult)
│       │       └── OfflineDataRepository.kt   (CSV loader + offline inference engine)
│       ├── assets/
│       │   └── data/
│       │       ├── diseases.csv        ← 52 diseases (cow / buffalo / goat / sheep / chicken / duck / pig / dog)
│       │       ├── symptoms.csv        ← 20 visual symptom tiles (EN / HI / MR)
│       │       ├── symptom_rules.csv   ← offline rule-based AI mappings
│       │       ├── remedies.csv        ← action tiles (isolate / call vet / clean / feed …)
│       │       ├── pregnancy.csv       ← birth guide
│       │       └── chatbot.csv         ← seed keyword → response pairs
│       └── res/
│           ├── raw/
│           │   └── cosmic_shiva.mp4    ← Pashupatinath Mode background video
│           ├── layout/
│           │   └── activity_cosmic_energy.xml   ← VideoView + vignette + particles stack
│           ├── drawable/
│           │   └── cosmic_vignette.xml          ← radial gradient edge-blend
│           └── values/, font/, menu/, navigation/
└── build.gradle, settings.gradle
```

---

## 🔱 Pashupatinath Mode — how the video integrates

The provided `Cosmic_Shiva_480P.mp4` was preprocessed with ffmpeg:

1. **Watermark removed** — bottom 60 px cropped out (removed `@sk_91193`).
2. **Background crushed to pure black** — via a curves filter:
   ```
   curves=all='0/0 0.15/0 0.5/0.55 1/1'
   ```
   This ensures the video's dark areas become identical to the app's
   `deep_space_black` (`#0A0A0F`) container colour. No chroma-key library
   needed — the video blends seamlessly when alpha = 0.95 and the `SCREEN`-
   like effect is achieved purely through matching backgrounds.
3. **Saturation + contrast boosted** on the glowing Shiva silhouette.
4. **Audio stripped** (`-an`) — the sequence plays silently.
5. **Re-encoded** at CRF 26, `faststart` for instant playback.

At runtime (`CosmicEnergyActivity.kt`):

- `VideoView` plays the looped video at α = 0.95 in the background.
- A radial `cosmic_vignette.xml` drawable sinks the video edges into black.
- The original `SplashAnimationView` (energy lines → orb → Fibonacci
  spiral) is overlaid on top for an extra motion layer.
- Sanskrit blessing *"सर्वे भवन्तु सुखिनः"* + tagline fade in at 4.5 s.
- Auto-returns to `MainActivity` at 11 s.
- Falls back gracefully to the original particle-only animation if
  `cosmic_shiva.mp4` ever fails to load.

---

## 🧠 Offline AI — how disease detection works now

`OfflineDataRepository.diagnose(selectedSymptoms, animal)`:

1. Loads `symptom_rules.csv` on first use (cached in memory).
2. For every rule, computes the intersection between the user's selected
   symptoms and the rule's required symptoms.
3. Scales the rule's base confidence by how many required symptoms actually
   matched (`coverage = matched / required`).
4. Returns the top 5 results, highest confidence first, deduped by disease,
   with full home-care + vet-advice payload.

**Example:**
```
selectedSymptoms = { "fever", "mouth_sores", "limping" }
→ Foot and Mouth Disease — 85% — vet: "vaccine and supportive medicine"
```

### Swapping in a real ML model later

Keep the same `diagnose(...)` signature. Inside, replace the rule loop with
a TFLite invocation:

```kotlin
val interpreter = Interpreter(loadModelFile(context, "pashu_model.tflite"))
interpreter.run(inputTensor, outputTensor)
```

Every UI screen already consumes `DiagnosisResult` — no refactor needed.

---

## 🌐 Where to add more data / images

| What you want to add       | Drop it here                              |
| -------------------------- | ----------------------------------------- |
| More diseases              | `app/src/main/assets/data/diseases.csv`   |
| More symptom tiles         | `app/src/main/assets/data/symptoms.csv`   |
| More inference rules       | `app/src/main/assets/data/symptom_rules.csv` |
| Disease photos             | `app/src/main/assets/images/diseases/`    |
| Action icons (call vet …)  | `app/src/main/assets/images/actions/`     |
| Feed / fodder images       | `app/src/main/assets/images/feed/`        |
| Warning badges             | `app/src/main/assets/images/warnings/`    |
| Replace cosmic video       | `app/src/main/res/raw/cosmic_shiva.mp4`   |

---

## 🌍 Multilingual

`symptoms.csv` and `remedies.csv` already carry `label_en / label_hi /
label_mr` columns. Fragments can switch at render time based on the user's
language preference (stored in `SettingsFragment`). Sarvam AI hooks can
replace the static labels later for speech-to-text / text-to-speech.

---

## 🛠 Build

Open in Android Studio → let Gradle sync → run on device / emulator
(API 24+). No API keys required. Tested layout for compile:
`./gradlew assembleDebug`.

---

## 🏆 Hackathon pitch line

> "Pashu-Raksha gives every farmer a vet in their pocket — even with no
> signal. AI for diagnosis, reverence for the animal."

**Har Har Mahadev 🔱**
