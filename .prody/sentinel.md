## 2025-05-15 - Harden Network Security and Journal Privacy
**Vulnerability:** Placeholder certificate pins in `network_security_config.xml` and missing `FLAG_SECURE` on sensitive journal screens.
**Learning:** Sequential placeholders like `GThRnpaJ1x8I2c4e8p5h6k7l8m9n0o1p2q3r4s5t6u8=` were used for certificate pinning, which provides no real security against MITM attacks. Additionally, highly private user journal content was exposed to screenshots and screen recordings.
**Prevention:** Always use real SHA-256 pins from production certificates and include intermediates/backup pins for resilience. Use `FLAG_SECURE` on all screens displaying or capturing sensitive user data.

## 2026-01-31 - Credential and Privacy Hardening
**Vulnerability:** Leaking API keys in Logcat via OkHttp interceptors and debug logs, and missing UI protection for therapeutic chats.
**Learning:** Prody's `OpenRouterService` used `HttpLoggingInterceptor.Level.BODY` in debug mode without redacting the `Authorization` header, exposing API keys to anybody with ADB access. Additionally, Haven't therapeutic screens lacked `FLAG_SECURE`, risking user privacy.
**Prevention:** Always use `redactHeader("Authorization")` in network interceptors. Remove logs that print partial secrets. Enforce `FLAG_SECURE` on all therapeutic and reflection screens by default.

## 2025-06-10 - Secure Biometric Gating for Sensitive Content
**Vulnerability:** Privacy locks for Journal and Future Messages were settings-only with no actual biometric enforcement, leaving sensitive personal data exposed if the device was left unlocked.
**Learning:** Implementing `BiometricPrompt` in a Compose-based Clean Architecture requires careful handling of `FragmentActivity` context (using `ContextWrapper` unwrapping) and state preservation via `rememberSaveable` to avoid re-authenticating on every rotation. Additionally, a fallback check for device security availability is mandatory to prevent locking out users on devices without PINs/biometrics.
**Prevention:** Use a dedicated `BiometricAuthenticator` utility for lifecycle-safe auth and gate sensitive Composables using a reusable `RequireBiometricAuth` wrapper. Combine this with `PreventScreenshots` for defense-in-depth.
