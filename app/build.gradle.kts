plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.s23010222.coconet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.s23010222.coconet"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("androidx.viewpager2:viewpager2:1.1.0")
    implementation ("com.google.android.gms:play-services-maps:19.2.0")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("androidx.appcompat:appcompat:1.7.1")
    implementation ("com.google.android.gms:play-services-base:18.4.0")
    implementation ("com.google.android.gms:play-services-auth:21.3.0")
    implementation ("com.google.android.libraries.places:places:3.4.0")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("com.google.android.gms:play-services-maps:19.0.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.material:material:1.11.0")
    implementation (platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation ("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-firestore:24.10.0")
    implementation ("com.google.firebase:firebase-auth:22.1.1")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.google.android.material:material:1.10.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.28")
    implementation("com.google.code.gson:gson:2.10.1")
}