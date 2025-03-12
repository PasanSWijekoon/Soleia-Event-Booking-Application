plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.strawhats.soleia"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.strawhats.soleia"
        minSdk = 28
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0")) // Updated BOM

    implementation("com.google.firebase:firebase-firestore") // Removed version
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.firebase:firebase-messaging") // removed version

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation (libs.credentials)
    implementation (libs.credentials.play.services.auth)
    implementation (libs.googleid)
    implementation ("com.google.firebase:firebase-auth") // removed version
    implementation (libs.play.services.auth )
    implementation (libs.android.gif.drawable)
    implementation ("com.airbnb.android:lottie:5.2.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation ("androidx.work:work-runtime:2.7.1")
    implementation ("com.google.guava:guava:31.0.1-android")
    implementation ("com.github.PayHereDevs:payhere-android-sdk:v3.0.17")
    implementation ("com.google.code.gson:gson:2.8.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.google.zxing:core:3.4.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.google.mlkit:barcode-scanning:17.2.0")
    implementation ("com.google.android.gms:play-services-location:18.0.0")
    implementation ("com.github.imperiumlabs:GeoFirestore-Android:v1.5.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation ("com.github.yalantis:ucrop:2.2.8")
    implementation ("com.hbb20:ccp:2.6.0")
    implementation ("com.google.android.libraries.places:places:3.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}