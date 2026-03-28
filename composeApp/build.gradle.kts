import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

// Определяем ОС для автоматического выбора параметров
val osName = System.getProperty("os.name").lowercase()
val isWindows = osName.contains("windows")

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.calc.MainKt"
        nativeDistributions {
            // АВТОМАТИЧЕСКИЙ ВЫБОР ФОРМАТА
            targetFormats(
                if (isWindows) TargetFormat.Exe else TargetFormat.Deb
            )

            packageName = "calckb"
            packageVersion = "1.0.0"
            vendor = "Erlbriton"
            copyright = "© 2026 Vasiltsov Yurii"

            // ОБЩАЯ ОПТИМИЗАЦИЯ ДЛЯ ВСЕХ ОС (Минус 15-20 МБ)
            buildTypes.release.proguard {
                isEnabled.set(true)
                optimize.set(true)
                configurationFiles.from(project.file("proguard-rules.pro"))
            }

            // Оставляем только жизненно важные части Java
            modules("java.desktop", "java.logging", "java.xml")

            // Специфические настройки для Linux (Kubuntu)
            linux {
                shortcut = true
                menuGroup = "Utility"
                debMaintainer = "erlbriton@example.com"

                // Если захочешь иконку в Linux, положи PNG в composeApp
                 iconFile.set(project.file("CalcKb.png"))
            }

            // Специфические настройки для Windows
            windows {
                shortcut = true
                menu = true
                // Указываем иконку только если мы на Windows
                if (isWindows && project.file("CalcKb.ico").exists()) {
                    iconFile.set(project.file("CalcKb.ico"))
                }
            }
        }
    }
}