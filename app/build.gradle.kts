plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "org.techtown.medicheck"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.techtown.medicheck"
        minSdk =26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation("org.apache.poi:poi:5.2.3")
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)

        implementation ("com.google.android.material:material:1.11.0")
        implementation ("org.apache.poi:poi:5.2.3")
        implementation ("org.apache.poi:poi-ooxml:5.2.3")
        implementation ("androidx.core:core:1.9.0")
        implementation ("com.google.firebase:firebase-database:20.3.0")
        implementation ("com.google.firebase:firebase-analytics:21.3.0")
    }
}





