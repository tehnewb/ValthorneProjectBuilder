# Valthorne Project Builder

A fast, zero‑friction way to bootstrap a ready‑to‑run Gradle project that uses the Valthorne game framework. This tool launches a small GUI where you fill in a few fields (project name, package name, etc.), then it generates a complete project scaffold with sensible defaults so you can start coding immediately.

## Using the GUI

When the GUI opens, you’ll see a simple form with the following fields:

- Application Class Name: The name of your main application class (e.g., `MyGame`).
- Project Name: The Gradle project name and base folder name (e.g., `MyGameProject`).
- Package Name: Your Java package (e.g., `com.example.mygame`).
- Project Path: Absolute path to where the project should be created (e.g., `C:/dev/` on Windows or `/Users/you/dev/` on macOS/Linux). The builder will create a subfolder using your Project Name.

Click "Build Project" to generate the project. Status messages will appear at the bottom of the window.

If any folders don’t exist, the builder will attempt to create them.

---

## What Gets Generated

The builder copies a set of templates and replaces placeholders like `PROJECT_NAME`, `PACKAGE_NAME`, `VALTHORNE_VERSION`, and `MAIN_CLASS` where appropriate. A typical generated project looks like this:

```
MyGameProject/
├─ settings.gradle                 → Sets the Gradle project name
├─ build.gradle                    → Dependencies + ShadowJar + (optionally) Construo config
├─ .gitignore                      → Standard ignores for Gradle/Java projects
├─ README.md                       → Quick start for your new project
├─ assets/                         → Place runtime assets here (if present in template)
├─ src/
│  └─ PACKAGE_NAME/
│     ├─ Launcher.java             → Contains the `main` that boots JGL/Valthorne
│     └─ MAIN_CLASS.java           → Your game `Application` implementation
```

Notes:
- The exact layout may evolve slightly as templates improve; consult the generated `README.md` for authoritative instructions inside your new project.
- Java 25 is configured via Gradle toolchains in the template. You don't have to manage JAVA_HOME manually if you let Gradle handle it.

---

## Customizing the Templates (for advanced users)

If you intend to modify how new projects are scaffolded, adjust the template files inside the builder’s resources:

```
src/main/resources/project_template/
├─ template_build.gradle
├─ template_settings.gradle
├─ template_gitignore
├─ template_README.md
├─ template_launcher.java
└─ template_application.java
```

These contain placeholders such as:
- `PROJECT_NAME`
- `PACKAGE_NAME`
- `VALTHORNE_VERSION`
- `MAIN_CLASS`

The Builder replaces them using the values you enter in the GUI along with its own internal version information.

---

## Troubleshooting

- Gradle wrapper permission denied (macOS/Linux):
  ```bash
  chmod +x gradlew
  ```
- Java not detected:
  ```bash
  java -version
  ```
  Install JDK 25 and/or ensure your PATH and `JAVA_HOME` are set. Or allow Gradle toolchains to download JDK 25.
- Paths with spaces on Windows:
  Use quotes, e.g., `"C:/Users/you/My Projects"`.
- Antivirus/quarantine prevents running the jar:
  Add an exclusion for your project or the `build/libs` folder.

---

## FAQ

- Can I rename my package or main class later?
  Yes, but remember to update references (e.g., `application.mainClass` in `build.gradle`) if you relocate classes.

- Does the generated project work on macOS/Linux?
  Yes. Development commands (`gradlew run`, `shadowJar`) are cross‑platform. The included native packaging example targets Windows x64 via Construo; you can add additional targets as needed.

- Which Java version should I code against?3
  Java 25. The template configures Gradle toolchains accordingly.

---

## License

This repository’s license applies to the builder and its templates. If you generate a project, you own your project; choose and add a license file in your generated repository as appropriate.

