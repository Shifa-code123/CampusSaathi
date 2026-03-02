plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.campussaathi"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.campussaathi"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.cardview:cardview:1.0.0")


    // 🔥 Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Firebase Storage (profile image)
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.android.gms:play-services-location:21.0.1")

// Glide (image loading)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")


    // 🔥 Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // 🔐 Firebase Auth
    implementation("com.google.firebase:firebase-auth-ktx")

    // 🗄️ Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")

    // 📦 Firebase Storage (VERY IMPORTANT FOR YOUR ISSUE)
    implementation("com.google.firebase:firebase-storage-ktx")

    // 🖼️ Glide (OK as is)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")



    implementation("androidx.recyclerview:recyclerview:1.3.2")

}
apply(plugin = "com.google.gms.google-services")
