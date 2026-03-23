plugins {
    // Оставляем только те плагины, которые есть в нашем libs.versions.toml
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
}