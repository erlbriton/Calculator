import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    // Стандартная цель для Desktop, ищет исходники в src/jvmMain
    jvm()

    sourceSets {
        // commonMain — здесь лежат общие зависимости для всех платформ
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
            }
        }

        // jvmMain — просто добавляем специфические зависимости для Desktop
        // Gradle САМ сделает его зависимым от commonMain автоматически
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        // Полный путь к классу с учетом пакета, который мы видели на скриншоте
        mainClass = "com.example.calc.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Deb)

            packageName = "CalcKb"
            packageVersion = "1.0.0"
            vendor = "erlbriton"
            description = "Calculator Application"

            // Настройка для Windows
            windows {
                shortcut = true
                // Путь к .ico файлу
                iconFile.set(project.file("src/jvmMain/composeResources/CalcKb.ico"))
                // Чтобы не создавалось лишнее консольное окно
                console = false
            }

            // Настройка для Linux
            linux {
                shortcut = true
                packageName = "calckb"
                appCategory = "Utility"
                // Путь к .png файлу
                iconFile.set(project.file("src/jvmMain/resources/CalcKb.png"))
            }
        }

        // Аргументы JVM для стабильности на Windows
        jvmArgs += "-Dskiko.renderApi=SOFTWARE"
    }
}