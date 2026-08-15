# Agentic Marketer for Android

> An AI-powered content marketing solution for Android, created as a Final Year Project by Ali Hassan and Aneeq Rabbani at Abbottabad University of Science and Technology.

Agentic Marketer is a mobile-first content studio for small businesses and solo marketers. It brings AI-assisted blog writing, social-media captions, hashtags, and marketing visuals into one focused workflow.

## What the project demonstrates

The project demonstrates Android development with Kotlin, MVVM architecture, the Repository pattern, Retrofit networking, Firebase Authentication, Cloud Firestore persistence, external AI API integration, loading states, retry handling, and black-box/white-box testing.

## Core features

- User registration and login with Firebase Authentication.
- SEO-oriented blog post generation.
- Social-media caption and hashtag generation.
- AI marketing image generation.
- Content history stored in Cloud Firestore.
- Humanize workflow for blog content.
- Error handling and retry logic for API rate limits.
- Material Design 3 interface with focused loading states.

## Architecture

```text
UI screens → ViewModels → Repositories → Retrofit services
                         ↘ Firebase Auth / Firestore
                         ↘ Gemini text API / Hugging Face image API
```

The supplied thesis describes the architecture and testing work in detail. This repository includes the thesis PDF, architecture documentation, and representative Kotlin samples. It does not claim to contain the complete original Android Studio source because that source was not included with the thesis document.

## Technology stack

| Area | Technology |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI | XML layouts, Material Design 3 |
| Architecture | MVVM + Repository pattern |
| Authentication | Firebase Authentication |
| Persistence | Cloud Firestore |
| Text generation | Google Gemini 1.5 Pro |
| Image generation | Hugging Face FLUX.1-schnell |
| Networking | Retrofit |
| Testing | Black-box and white-box testing |

## Performance notes

The thesis reports typical text generation in approximately 5–7 seconds and image generation in approximately 15–20 seconds on a stable network. It also describes retry handling for Hugging Face rate-limit responses and composite Firestore indexes for history queries.

## Repository contents

- `docs/Agentic_Marketer_Thesis.pdf` — full academic report.
- `docs/architecture.md` — concise architecture and module notes.
- `kotlin-samples/ContentRepository.kt` — representative safe sample of the repository abstraction.
- `kotlin-samples/FirestoreManager.kt` — representative persistence boundary sample.

## Security note

Do not commit API keys, `google-services.json`, Firebase service-account files, or local secrets. Use `local.properties`, environment variables, or a private secrets manager for real deployments.

## Future directions

Potential extensions include social-media publishing and scheduling, analytics, multimodal input, brand voice personalization, short-form video generation, and collaborative workspaces.

## Academic context

Final Year Project Report, Bachelor of Science in Software Engineering, Session 2022–2026, Abbottabad University of Science and Technology.
