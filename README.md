# StudyNotes - AI-Powered Markdown Study App

An Android app that opens `.md` files, renders them beautifully, and lets you highlight text to ask Google Gemini AI for explanations, analogies, quizzes, and flashcards — all designed for effective study workflows.

## Features (MVP)

- **Android SAF File Picker** — Open any `.md` file from your device
- **High-Fidelity Markdown Rendering** — Headings, lists, code blocks, tables, math via Markwon
- **Long-Press + Ask AI** — Select text → context menu → AI explanation in a bottom sheet
- **Google Gemini Integration** — Structured pedagogical system prompt for study-focused responses
- **Local Sidecar Saving** — AI responses and annotations saved alongside source documents
- **Dark Mode** — Material You dynamic color support
- **Offline Cache** — Previously loaded documents available without network
- **Smart Context** — Auto-includes ±2 surrounding paragraphs for AI queries

## Setup Instructions

### 1. Get a Gemini API Key

1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Create an API key (free tier: 60 requests/min)
3. Copy the key

### 2. Configure the Project

Open `local.properties` in the project root and add your API key:

```properties
gemini.api.key=YOUR_API_KEY_HERE
```

> ⚠️ **Never commit `local.properties` to version control.** It's in `.gitignore`.

### 3. Open in Android Studio

1. Open Android Studio
2. File → Open → select the `LearningApp` folder
3. Wait for Gradle sync to complete
4. Connect an Android device (API 26+) or start an emulator
5. Click **Run** (▶️)

### 4. Use the App

1. Tap the **+** button to open a `.md` file
2. Long-press text to select → tap **"Ask AI"** from the context menu
3. Choose a prompt template (e.g., "Explain simply", "Test me")
4. Read the AI response → tap **Save** to persist it locally

## Project Structure

```
LearningApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/selfproject/learningapp/
│   │   │   ├── data/               # Repositories (file, AI)
│   │   │   ├── model/              # Data models & UI states
│   │   │   ├── ui/
│   │   │   │   ├── components/     # Compose UI components
│   │   │   │   ├── navigation/     # NavHost
│   │   │   │   ├── screens/        # Screen composables
│   │   │   │   └── theme/          # Material3 theme
│   │   │   ├── viewmodel/          # MainViewModel
│   │   │   ├── MainActivity.kt
│   │   │   └── LearningAppApplication.kt
│   │   ├── res/                    # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material3 |
| Markdown | Markwon 4.6.2 |
| AI | Google Gemini 2.0 Flash |
| Local Storage | DataStore + Room |
| Architecture | MVVM + Repository |
| Min SDK | 26 (Android 8.0) |

## Development Roadmap

### Phase 1 ✅ (Current)
- Foundation, file picker, markdown rendering, AI integration, sidecar saving

### Phase 2 (Planned)
- Context chunking, bookmarking, flashcard/quiz generation, response caching, document search, custom prompt templates

### Phase 3 (Planned)
- PDF/DOCX import, voice I/O, spaced repetition, study analytics, multi-document workspace, export utilities

## License

Personal project — not for distribution.
