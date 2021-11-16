# CareDrivers Demo

## APK link
https://drive.google.com/file/d/1wSlLPH7Grgl3woWdU8sFBMAP71I_KUBe/view?usp=sharing
(This is a debug build)

## Notable libraries used
  * **Ktor Client** for API calls
    * Also familiar with Retrofit
  * **kotlinx-serialization** for serialization
    * Also familiar with Gson
  * **kotlinx-datetime** for date/time utilities
    * Also familiar Java 8+ `java.time` and legacy `java.util.Date`/`java.util.Calendar`
  * **Kotlin Couroutines** for concurrency and reactive streams
    * Also familiar with RxJava 1/2
  * **Jetpack Navigation** for inter-fragment navigation
  * **osmdroid** for OpenStreetMap map rendering

## Improvements that could be made
  * Adding unit tests for ViewModels and UI automation tests for the app as a whole
  * Dependency injection framework (e.g. Dagger/Hilt) to make dependency management easier
  * Another layer of models
    * Ideally, models deserialized directly from API responses would be converted to models designed for consumption by the business layer (ViewModels) so the API can change in trivial ways without breaking business logic
  * Use Google Maps insead of OpenStreetMap
    * No doubt a better API, but I didn't want to deal with real API keys for this demo project :P
  * Better use of theming system, implementation of dark mode
  * Gradle improvements: using Gradle Kotlin DSL, using a version catalog to manage version numbers shared between dependencies
