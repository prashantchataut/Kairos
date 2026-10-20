# Kairos • a little garden of time

A hopeful, tactile companion for a small daily act of learning and kindness.
Warm paper and ink green lead; peach and sage watercolor washes illustrate growth.
Lora gives words room to breathe; Android's native sans keeps controls familiar.
The signature is a seed held in an orbit: a moment kept, a thought growing.
Glass belongs to floating navigation and controls. Reading surfaces remain opaque.
Motion is interruptible and brief; Android's disabled animator setting is respected.

## Core experience
- Today: one word, one attributed thought, a small invitation to reflect.
- Words: searchable offline vocabulary, saved collection, recall practice, Word Prints.
- Capsules: compose a dated letter with durable private attachments; sealed until due.
- Your space: access existing journal, settings and learning tools.

## Audit
The existing app is Kotlin/Compose, Room/Hilt, with bundled Lora fonts and an offline
catalog. Existing daily recommendations persist by user and local date; keep this
working storage and migration history. The previous primary UI uses a blue palette
and several competing feature systems. Sharing hardcodes the release provider
authority (broken in debug); attachments are transient picker URIs. Prioritize
reliable sharing, durable attachments and date-gated opening alongside the new UI.

## Product decisions
No compulsory streaks or guilt copy. No AI credential needed for core features.
Capsules stay on this device; uninstalling removes local files. No claim of cloud
backup or guaranteed exact notification timing. Dates use the device's local zone.
