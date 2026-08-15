# Agentic Marketer for Android

> An AI-powered content marketing solution for Android, created as a Final Year Project by Ali Hassan and Aneeq Rabbani at Abbottabad University of Science and Technology.

Agentic Marketer is a mobile-first content studio for small businesses and solo marketers. It brings AI-assisted blog writing, social-media captions, hashtags, and marketing visuals into one focused workflow.

## This repository contains the real source

The supplied `Agentic_Marketer_HuggingFace.zip` included a complete Android source tree with Kotlin activities, fragments, ViewModels, repositories, Retrofit services, XML layouts, navigation resources, animations, tests, Gradle configuration, and an `agentic_marketer_backend` directory. Generated build outputs, signing files, Firebase service files, local properties, environment files, and hard-coded credentials were removed before publication preparation.

## Core features

- User registration, login, password reset, and profile flows.
- AI blog generation and social-media caption/hashtag generation.
- AI marketing image generation through the Hugging Face inference endpoint.
- Dashboard, recent campaigns, history, scheduler, and profile screens.
- Firebase Authentication, Firestore, and Storage integration.
- Material Design layouts, Lottie animation, shimmer loading states, and image loading.
- Repository/ViewModel separation with Retrofit API services.

## Technology stack

| Area | Technology |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI | XML layouts, Material Design |
| Architecture | MVVM + Repository pattern |
| Authentication | Firebase Authentication |
| Persistence | Cloud Firestore |
| AI text | OpenRouter / Gemini service integrations |
| AI images | Hugging Face inference endpoint |
| Networking | Retrofit + OkHttp |
| Images | Glide |
| Animations | Lottie + Shimmer |
| Build | Gradle Kotlin DSL |

## Project structure

```text
app/src/main/java/com/example/agenticmarketer/
├── api/            Retrofit service definitions and clients
├── models/         Request and response data models
├── repositories/   AI and data-access boundaries
├── ui/             Activities, fragments, and adapters
├── viewmodels/     UI state and orchestration
└── utils/          App configuration and local helpers

app/src/main/res/
├── layout/         Android XML screens and list items
├── navigation/     Navigation graph
├── drawable/       Icons and background resources
└── values/         Strings, colors, dimensions, and themes

agentic_marketer_backend/  Supporting backend/agent source from the supplied archive
docs/                      Thesis and architecture notes
```

## Safe local setup

1. Open the project root in Android Studio.
2. Add your own `google-services.json` under `app/` from a private Firebase project.
3. Configure provider credentials locally; never put real values in `Config.kt` or commit them.
4. Review the API endpoints and Firebase security rules before testing.
5. Build the debug variant and test authentication, content generation, image generation, and history flows.

The checked-in `Config.kt` intentionally contains replacement markers. It is a public-safe template, not a working credential store.

## Documentation

- `docs/Agentic_Marketer_Thesis.pdf` — full Final Year Project report.
- `docs/architecture.md` — concise architecture and request-flow notes.
- `kotlin-samples/` — small representative architecture samples.

## Security warning

The source archive contained credential-like values and private configuration files. Those values were not copied into the public repository preparation. Because credentials may have been exposed in the supplied archive, rotate them in OpenRouter, Google AI, Hugging Face, and Firebase before using the project again.

## Academic context

Final Year Project Report, Bachelor of Science in Software Engineering, Session 2022–2026, Abbottabad University of Science and Technology.
