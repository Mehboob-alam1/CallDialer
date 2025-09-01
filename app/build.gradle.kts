import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.easyranktools.callhistoryforanynumber"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.easyranktools.callhistoryforanynumber"
        minSdk = 24
        targetSdk = 36
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

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.auth)
    
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.core.ktx)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    // Cashfree SDK - Re-enabled for Web Checkout
    implementation("com.cashfree.pg:api:2.2.8")

    implementation("com.airbnb.android:lottie:6.6.7")

    implementation("com.intuit.sdp:sdp-android:1.1.1")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    implementation ("androidx.room:room-runtime:2.5.2")
    annotationProcessor ("androidx.room:room-compiler:2.5.2")
    implementation("com.hbb20:ccp:2.7.0")
    
    // Additional dependencies for enhanced features
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.11.0")


    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")

    // BlurView
    implementation("com.github.Dimezis:BlurView:version-3.1.0")
    implementation("jp.wasabeef:blurry:4.0.1")

    // Admob
    implementation("com.google.android.gms:play-services-ads:23.1.0")

    implementation("com.google.firebase:firebase-messaging")
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))


//    implementation(platform ( "com.google.firebase:firebase-bom:33.2"))


}