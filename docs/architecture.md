# Agentic Marketer Architecture Notes

## Module boundaries

The thesis describes four primary modules: authentication, content engine, history, and user interface. Authentication delegates identity to Firebase Authentication. The content engine coordinates Gemini text generation and Hugging Face image generation through service abstractions. The history module stores and retrieves user-owned generated content from Cloud Firestore. The UI module presents these capabilities through Material Design 3 screens and ViewModels.

## Request flow

1. The user selects a content type and enters a brief.
2. The ViewModel validates the input and asks the repository to generate content.
3. The repository calls the appropriate Retrofit service.
4. The response is validated and converted into a UI-safe result.
5. Successful content is persisted under the authenticated user’s history.
6. The ViewModel updates loading, success, or error state for the screen.

## Reliability decisions

The project used incremental development so the team could validate authentication and basic blog generation before expanding into social captions, history, and image generation. The thesis also describes retry handling with exponential backoff for image-generation rate limits and composite Firestore indexes for history queries.

## Security decisions

Authentication and Firestore security rules must scope content to the authenticated user. API keys should not be hard-coded into source files or committed to GitHub. A production implementation should place secrets behind a secure backend or protected build configuration rather than exposing them in a distributed APK.
