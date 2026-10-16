AXON - AI Voice Assistant

AXON is an Android AI voice assistant developed using Kotlin. It combines voice interaction, AI-powered responses, and a tool-based architecture to help users perform tasks through natural-language commands.

Technologies

- Kotlin and Android
- Android Speech Recognition
- Android Text-to-Speech
- Cloudflare Workers
- OpenAI API
- Cloudflare KV for memory storage

Features

Features implemented in the current version include voice interaction, AI response generation, and selected Android device-control tools. Additional capabilities include notes, reminders, and memory functionality, subject to the current implementation and testing status.

Architecture

The Android application communicates with a backend service hosted on Cloudflare Workers. The backend handles AI requests and connects to configured services.

Security

API credentials must be stored in server-side secrets and must never be committed to the repository. Device actions should respect Android permissions and applicable authorization and confirmation policies.

Project Status

Actively developed. Feature completeness, compatibility, and automated test coverage are being evaluated.

Developer

Pratik Khandare.
