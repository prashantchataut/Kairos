// Top-level build file. Kairos 2.0 "Aquarelle" — single module, no annotation-processor
// heavy plugins beyond Room's KSP.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
