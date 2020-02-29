# ChatAndroid

In your root-level "build.gradle" file, put:

    allprojects {
            repositories {
                jcenter()
                maven { url "https://jitpack.io" }
            }
       }

   
   
   In your app-level "build.gradle" file, put:
   

       dependencies {
            implementation 'com.github.EdenSharoni:ChatAndroid:0.0.2'
       }
