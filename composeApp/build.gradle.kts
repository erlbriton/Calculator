import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

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
        commonTest.dependencies {
            implementation(libs.kotlin.test)
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
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "calckb"
            packageVersion = "1.0.0"
            description = "Калькулятор с поддержкой HEX/DEC и быстрым копированием"
            copyright = "© 2026 Vasiltsov Yurii"
            vendor = "Erlbriton" // Твоё имя или бренд

            linux {
                shortcut = true // Создает ярлык в меню приложений
                menuGroup = "Utility" // Группа в меню (Утилиты)
                iconFile.set(project.file("metadata/CalcKb.png")) // ПУТЬ К ТВОЕЙ ИКОНКЕ
            }
        }
    }
}
