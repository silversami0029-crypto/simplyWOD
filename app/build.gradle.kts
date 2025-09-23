plugins {

    id("com.android.application") version "8.13.0"
    id("org.jetbrains.kotlin.android") version "2.2.0"
    //id("androidx.navigation.safeargs")
}

android {
    namespace = "com.bessadi.fitwod"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bessadi.fitwod"
        minSdk = 26
        targetSdk = 36
        versionCode = 14
        versionName = "14"
        multiDexEnabled = true


        buildFeatures {
            buildConfig = true  //  Add this line
            viewBinding = true
        }
        // Add this to access API key

       buildConfigField("String", "NUTRITION_API_APP_ID", "\"YOUR_APP_ID\"")
        buildConfigField("String", "NUTRITION_API_KEY", "\"YOUR_API_KEY\"")
       // testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    lint {
        baseline = file("lint-baseline.xml")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt")
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
    sourceSets {
        getByName("main") {
            res {
                srcDirs("src\\main\\res", "src\\main\\res\\values-ar")
            }
        }
    }
    packagingOptions {
        jniLibs {
            useLegacyPackaging = true // Change from 'false' to 'true'
        }

        
    }


}

dependencies {
    // --- AndroidX ---
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.7.0") // Use a consistent, newer version
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.activity:activity:1.9.0") // CRITICAL: Upgrade for EdgeToEdge fix
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.3")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    // --- Google Material Design ---
    implementation("com.google.android.material:material:1.12.0") // Use the highest version specified (1.12.0)

    // --- Google Play Services & Billing ---
    implementation("com.google.android.gms:play-services-ads:24.5.0")
    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.android.billingclient:billing:6.2.1") // Consider upgrading

    // --- CameraX (use consistent versions) ---

    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("androidx.camera:camera-extensions:1.4.2")

    // --- ML & Barcode ---
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("org.tensorflow:tensorflow-lite:2.17.0") // Use your preferred version
    // implementation("org.tensorflow:tensorflow-lite-support:0.4.4") // Uncomment and use a newer version if needed

    // --- Networking ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // WARNING: Version 3.0.0 is incorrect/not released. Use 2.9.x.
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.1.0")
    implementation("com.google.code.gson:gson:2.13.1")

    // --- Firebase ---
    implementation("com.google.firebase:firebase-analytics:21.3.0")
    implementation("com.google.firebase:firebase-firestore:24.8.1")

    // --- UI & Utilities ---
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0") // Upgraded
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.guava:guava:33.4.8-android")

    // --- Lint (Keep as compileOnly) ---
    compileOnly("com.android.tools.lint:lint-api:31.13.0")
    compileOnly("com.android.tools.lint:lint-checks:31.13.0")

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")


    implementation ("com.squareup.okhttp3:okhttp:4.9.3")



}
