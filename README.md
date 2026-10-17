# My Library

A very basic Android app (Kotlin, Jetpack Compose, Room) for keeping track of the books you own.

- Add, edit and delete books (title, author)
- Mark each book **To read**, **Reading** or **Read**
- Lend a book to someone and mark it returned; the list shows who has it and since when
- Search by title or author

Everything is stored on the phone; there is no account or internet access.

## Build and run

Requires JDK 17 and the Android SDK (`ANDROID_HOME` set, or a `local.properties` with `sdk.dir=...`).

```sh
./gradlew assembleDebug        # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # install on a connected phone or emulator
./gradlew testDebugUnitTest    # unit tests (run on the JVM, no device needed)
```

Or open the folder in Android Studio and press Run.

## Code

| File | What it does |
|---|---|
| `data/Book.kt` | The `books` table and the reading-status enum |
| `data/BookDao.kt` | Queries: search (also used to list all), insert, update, delete |
| `data/LibraryDatabase.kt` | Room database setup |
| `ui/LibraryViewModel.kt` | Search state plus save / delete / lend / return actions |
| `ui/LibraryScreen.kt` | The single screen and its dialogs |
| `MainActivity.kt` | App entry point |
