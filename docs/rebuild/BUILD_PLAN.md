# Kairos: a little room to grow

Direction: hopeful, tactile, playful. A painted pocket garden under glass controls;
blue pond ink, green leaves, rose petals, paper white. Rounded Android lettering
and bundled Lora for reflection. Large readable controls and quiet, responsive motion.

## Investigation
- Kotlin 2.0.21, Compose, Room + SQLCipher, Hilt; min API 24, target API 35.
- Existing daily-plan and spaced-repetition engines can be retained while the user
  experience is rebuilt. Existing user data must not be destructively migrated.
- Capsule screens had no routes in the active NavHost.
- Practice mode route escaped interpolation, silently falling back to MIXED.
- Temporary attachment grants could expire long before capsule delivery.
- Capsule reveal accepted IDs without enforcing the delivery date.
- Game downloads had no active catalog or distribution contracts.

## Implementation sequence
1. Restore capsule navigation, fix practice routing and durable attachment access.
2. Painted home, unified palette and rounded typography; original Word Garden.
3. Capsule delivery/reveal tests, build, lint, CI; review visuals on Android.
4. Creator-managed game discovery first; direct game downloads require verified
   binaries, checksums, permission to distribute, Android compatibility and updates.

## Product recommendations
- Keep core learning and letters local and usable without paid AI.
- Treat reminders as optional; do not punish users for missing a day.
- Preserve dates as instants; display in the device timezone.
- Offer capsule export/backup next: this installation currently disables Android backup.
- Do not promise cloud delivery or recovery without a real backend.
- Add content provenance and editorial review rather than presenting invented quotes
  as historical quotations.
