# PROJECT_NAME

This project was generated with the **Valthorne Project Builder**.

It uses:

- Gradle (Application plugin)
- ShadowJar (fat jar builder)
- Construo (native distribution packaging)
- Valthorne `VALTHORNE_VERSION`

---

## Requirements

- **Java 23**
- **Git** (optional but recommended)
- Terminal:
    - Windows → PowerShell
    - macOS / Linux → Terminal

> If Gradle toolchains are configured correctly, Gradle will automatically use or download the required JDK.
> If not, install Java 23 and ensure `JAVA_HOME` is set correctly.

---

## Project Structure

```
settings.gradle        → Sets the Gradle project name
build.gradle           → Dependencies + ShadowJar + Construo config
src/main/java/...      → Your game source code
build/libs/            → Built jars
distribution/          → Native packaged output (Construo)
```

---

## Run (Development Mode)

### macOS / Linux

```bash
./gradlew run
```

### Windows (PowerShell)

```powershell
.\gradlew.bat run
```

---

## Build a Runnable Fat Jar (ShadowJar)

This produces a single jar containing your code and all dependencies.

### Build

```bash
./gradlew shadowJar
```

### Output Location

```
build/libs/
```

Example:

```
build/libs/PROJECT_NAME-1.0.jar
```

### Run the Jar

```bash
java -jar build/libs/PROJECT_NAME-1.0.jar
```

---

## Build a Windows x64 Distributable (Construo)

Construo bundles:

- Your application jar
- A Windows runtime (downloaded via the configured `jdkUrl`)
- A Windows launcher (depends on Construo version)

### Step 1 — Build the jar

```bash
./gradlew shadowJar
```

### Step 2 — Create the distributable

```bash
./gradlew createDistributable
```

If that task does not exist, list all available tasks:

```bash
./gradlew tasks --all
```

Look for tasks containing:

- `distributable`
- `construo`
- `winX64`

### Step 3 — Output Location

```
distribution/
```

The output folder will contain:

- Bundled runtime
- Your packaged application
- Windows launcher executable

---

## Clean Build

```bash
./gradlew clean build
```

---

## Troubleshooting

### Permission denied (macOS/Linux)

```bash
chmod +x gradlew
```

### Java not detected

```bash
java -version
```

Ensure Java 23 is installed and available in your PATH.

---

## Where to Start Coding

Your entry point is:

```
src/main/java/PACKAGE_NAME/MAIN_CLASS.java
```

Implement:

- `init()` — Setup
- `render()` — Drawing
- `update(float delta)` — Game logic

Start building your game.