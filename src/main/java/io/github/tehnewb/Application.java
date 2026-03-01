package io.github.tehnewb;

import valthorne.JGL;
import valthorne.Window;
import valthorne.asset.Assets;
import valthorne.camera.OrthographicCamera2D;
import valthorne.graphics.font.FontData;
import valthorne.graphics.font.FontParameters;
import valthorne.graphics.texture.*;
import valthorne.io.file.ValthorneFiles;
import valthorne.ui.Layout;
import valthorne.ui.UI;
import valthorne.ui.Value;
import valthorne.ui.elements.Button;
import valthorne.ui.elements.Image;
import valthorne.ui.elements.Label;
import valthorne.ui.elements.TextField;
import valthorne.ui.enums.AlignItems;
import valthorne.ui.enums.JustifyContent;
import valthorne.ui.flex.FlexBox;
import valthorne.ui.flex.FlexDirection;
import valthorne.ui.styles.ButtonStyle;
import valthorne.ui.styles.LabelStyle;
import valthorne.ui.styles.TextFieldStyle;
import valthorne.viewport.ScreenViewport;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * UI-driven project scaffolding tool for Valthorne projects.
 *
 * <h2>What it does</h2>
 * <ul>
 *     <li>Shows a form to collect: application class name, project name, package name, and project path.</li>
 *     <li>Validates those inputs with Java and filesystem rules.</li>
 *     <li>Copies template files from a classpath or working-directory folder into a new project folder.</li>
 *     <li>Performs file I/O on a background thread and posts UI updates back to the render thread.</li>
 * </ul>
 *
 * <h2>Threading model</h2>
 * <ul>
 *     <li>UI event handlers run on the main thread.</li>
 *     <li>Project file creation runs on {@link CompletableFuture#supplyAsync}.</li>
 *     <li>UI label updates are posted back via {@link JGL#runTask(Runnable)}.</li>
 * </ul>
 *
 * <h2>Template expectations</h2>
 * <ul>
 *     <li>Templates are read via {@link ValthorneFiles#readString(String)}.</li>
 *     <li>The {@code TEMPLATE_ROOT} folder must exist and contain the referenced template files.</li>
 *     <li>Tokens inside templates are replaced before writing the output files.</li>
 * </ul>
 *
 * @author Albert Beaupre
 * @since March 1st, 2026
 */
public class Application implements valthorne.Application {

    private static final String TEMPLATE_ROOT = "project_template"; // Root folder that contains all project template files.
    private static final String TEMPLATE_BUILD_GRADLE = TEMPLATE_ROOT + "/template_build.gradle"; // Template path for build.gradle content.
    private static final String TEMPLATE_SETTINGS_GRADLE = TEMPLATE_ROOT + "/template_settings.gradle"; // Template path for settings.gradle content.
    private static final String TEMPLATE_GITIGNORE = TEMPLATE_ROOT + "/template_gitignore"; // Template path for .gitignore content.
    private static final String TEMPLATE_README = TEMPLATE_ROOT + "/template_README.md"; // Template path for README.md content.
    private static final String TEMPLATE_APPLICATION = TEMPLATE_ROOT + "/template_application.java"; // Template path for the generated Application class.
    private static final String TEMPLATE_LAUNCHER = TEMPLATE_ROOT + "/template_launcher.java"; // Template path for the generated Launcher class.

    private UI ui; // Root UI container for the builder screen.

    /**
     * Initializes the builder UI, loads assets, and wires event handlers.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Locks the window size for a fixed layout.</li>
     *     <li>Starts async loading for textures and fonts.</li>
     *     <li>Builds the UI once assets are loaded.</li>
     *     <li>Registers a resize listener to keep viewport, camera, and UI in sync.</li>
     * </ul>
     *
     * <p>Asset loading is asynchronous. UI creation is posted to the render thread using {@link JGL#runTask(Runnable)}.</p>
     */
    @Override
    public void init() {
        Window.setResizable(false);

        Assets.loadAsync(TextureParameters.fromClasspath("icons/96.png", "icon", false), TextureData.class)
                .whenComplete((textureData, _) -> JGL.runTask(() -> Window.setIcon(textureData)));

        ScreenViewport viewport = new ScreenViewport(Window.getWidth(), Window.getHeight());
        OrthographicCamera2D camera2D = new OrthographicCamera2D();
        viewport.setSize(Window.getWidth(), Window.getHeight());
        viewport.setCamera(camera2D);
        camera2D.setCenter(viewport.getWorldWidth() * 0.5f, viewport.getWorldHeight() * 0.5f);

        ui = new UI();
        ui.setViewport(viewport);

        Assets.prepare(FontParameters.fromClasspath("ui/font.ttf", "font", 20));
        Assets.prepare(TextureParameters.fromClasspath("ui/background.png", "background"));
        Assets.prepare(TextureParameters.fromClasspath("ui/textfield-focused.png", "textfield-focused"));
        Assets.prepare(TextureParameters.fromClasspath("ui/textfield-unfocused.png", "textfield-unfocused"));
        Assets.prepare(TextureParameters.fromClasspath("ui/button-background.png", "button-background"));
        Assets.prepare(TextureParameters.fromClasspath("ui/button-pressed.png", "button-pressed"));
        Assets.prepare(TextureParameters.fromClasspath("ui/button-hovered.png", "button-hovered"));

        Assets.load().thenAccept(_ -> JGL.runTask(() -> {
            Texture background = new Texture(Assets.get("background", TextureData.class));
            NinePatchTexture textfieldFocused = new NinePatchTexture(Assets.get("textfield-focused", TextureData.class), 2, 2, 2, 2);
            NinePatchTexture textfieldUnfocused = new NinePatchTexture(Assets.get("textfield-unfocused", TextureData.class), 2, 2, 2, 2);
            NinePatchTexture buttonBackground = new NinePatchTexture(Assets.get("button-background", TextureData.class), 2, 2, 2, 2);
            NinePatchTexture buttonPressed = new NinePatchTexture(Assets.get("button-pressed", TextureData.class), 2, 2, 2, 2);
            NinePatchTexture buttonHovered = new NinePatchTexture(Assets.get("button-hovered", TextureData.class), 2, 2, 2, 2);

            FontData fontData = Assets.get("font", FontData.class);

            LabelStyle labelStyle = LabelStyle.of()
                    .fontData(fontData);

            ButtonStyle buttonStyle = ButtonStyle.of()
                    .fontData(fontData)
                    .background(new NinePatchDrawable(buttonBackground))
                    .pressed(new NinePatchDrawable(buttonPressed))
                    .hovered(new NinePatchDrawable(buttonHovered));

            TextFieldStyle textFieldStyle = TextFieldStyle.of()
                    .fontData(fontData)
                    .background(new NinePatchDrawable(textfieldUnfocused))
                    .focused(new NinePatchDrawable(textfieldFocused));

            FlexBox form = new FlexBox()
                    .setFlexDirection(FlexDirection.COLUMN)
                    .setGap(14f)
                    .setWrap(false)
                    .setJustifyContent(JustifyContent.CENTER)
                    .setAlignItems(AlignItems.CENTER);

            form.setLayout(new Layout()
                    .width(Value.percentage(1f))
                    .height(Value.percentage(1f)));

            Image image = new Image(background);
            Label title = new Label("Welcome to the Valthorne Project Builder", labelStyle);
            Label message = new Label("", labelStyle);

            TextField projectName = new TextField("Project_Name", textFieldStyle, _ -> {});
            TextField projectPath = new TextField("C:/Project/Path", textFieldStyle, _ -> {});
            TextField packageName = new TextField("package.name", textFieldStyle, _ -> {});
            TextField applicationClass = new TextField("ApplicationClassName", textFieldStyle, _ -> {});

            Button build = new Button("Build Project", _ -> {
                buildProject(
                        message,
                        applicationClass.getText(),
                        projectName.getText(),
                        packageName.getText(),
                        projectPath.getText()
                );
            }, buttonStyle);

            Layout textfieldLayout = Layout.of()
                    .width(Value.percentage(0.70f))
                    .height(Value.pixels(40f));

            projectName.setLayout(textfieldLayout);
            projectPath.setLayout(textfieldLayout);
            packageName.setLayout(textfieldLayout);
            applicationClass.setLayout(textfieldLayout);

            build.setLayout(Layout.of()
                    .width(Value.pixels(150f))
                    .height(Value.pixels(40f)));

            image.setLayout(Layout.of()
                    .x(Value.pixels(0))
                    .y(Value.pixels(0))
                    .width(Value.percentage(1f))
                    .height(Value.percentage(1f)));

            message.setLayout(Layout.of().padTop(Value.pixels(10f)));

            form.addElement(title);
            form.addElement(applicationClass);
            form.addElement(projectName);
            form.addElement(packageName);
            form.addElement(projectPath);
            form.addElement(build);
            form.addElement(message);

            ui.addElement(image);
            ui.addElement(form);
        }));

        Window.addWindowResizeListener(event -> {
            viewport.update(event.getNewWidth(), event.getNewHeight());
            ui.setSize(event.getNewWidth(), event.getNewHeight());
            camera2D.setCenter(viewport.getWorldWidth() * 0.5f, viewport.getWorldHeight() * 0.5f);
        });
    }

    /**
     * Renders the UI.
     *
     * <p>This is expected to be called every frame by the engine loop.</p>
     * <p>Drawing order is the UI tree order established during {@link #init()}.</p>
     */
    @Override
    public void render() {
        ui.draw();
    }

    /**
     * Updates the UI input and layout state.
     *
     * <p>This is expected to be called every frame by the engine loop.</p>
     * <p>The {@code delta} value should be in seconds.</p>
     *
     * @param delta frame delta time in seconds
     */
    @Override
    public void update(float delta) {
        ui.update(delta);
    }

    /**
     * Disposes UI resources created by this application.
     *
     * <p>This should be called when the app is shutting down.</p>
     * <p>It delegates to {@link UI#dispose()}.</p>
     */
    @Override
    public void dispose() {
        ui.dispose();
    }

    /**
     * Trims a string and converts empty results into null.
     *
     * <p>This is used to normalize user input before validation.</p>
     * <p>If {@code s} is null, it returns null.</p>
     *
     * @param s input string
     * @return trimmed string, or null if null/empty after trimming
     */
    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Checks whether a string contains any whitespace characters.
     *
     * <p>This checks all Unicode whitespace, not just spaces.</p>
     * <p>Used for project naming rules and path sanity checks.</p>
     *
     * @param s input string (must be non-null)
     * @return true if any character is whitespace
     */
    private static boolean containsWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) return true;
        }
        return false;
    }

    /**
     * Validates a Java identifier using {@link Character} rules.
     *
     * <p>This validates a single identifier token.</p>
     * <p>It does not allow dots. That is handled by package validation.</p>
     *
     * @param s candidate identifier
     * @return true if it is a valid Java identifier
     */
    private static boolean isValidJavaIdentifier(String s) {
        if (s == null || s.isEmpty()) return false;
        if (!Character.isJavaIdentifierStart(s.charAt(0))) return false;
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Validates a project folder name for safe folder creation.
     *
     * <p>Rules:</p>
     * <ul>
     *     <li>Must not be empty.</li>
     *     <li>Must not contain whitespace.</li>
     *     <li>Must not contain path separators.</li>
     * </ul>
     *
     * @param s project name
     * @return true if valid
     */
    private static boolean isValidProjectName(String s) {
        if (s == null || s.isEmpty()) return false;
        if (containsWhitespace(s)) return false;
        return !s.contains("/") && !s.contains("\\");
    }

    /**
     * Validates a Java package name.
     *
     * <p>Rules:</p>
     * <ul>
     *     <li>Must be dot-separated.</li>
     *     <li>Each segment must be a valid Java identifier.</li>
     *     <li>No whitespace is allowed.</li>
     * </ul>
     *
     * @param s package name string
     * @return true if valid
     */
    private static boolean isValidPackageName(String s) {
        if (s == null || s.isEmpty()) return false;
        if (containsWhitespace(s)) return false;

        String[] parts = s.split("\\.");
        for (String p : parts) {
            if (p.isEmpty() || !isValidJavaIdentifier(p)) return false;
        }
        return true;
    }

    /**
     * Validates the application class name token.
     *
     * <p>This validates the simple class name only.</p>
     * <p>It does not include a package prefix.</p>
     *
     * @param s class name token
     * @return true if valid
     */
    private static boolean isValidApplicationClassName(String s) {
        if (s == null || s.isEmpty()) return false;
        if (containsWhitespace(s)) return false;
        return isValidJavaIdentifier(s);
    }

    /**
     * Validates all user inputs and returns an error message for UI display.
     *
     * <p>This method normalizes each value using {@link #trimToNull(String)}.</p>
     * <p>It returns the first error message encountered.</p>
     * <p>If everything is valid, it returns null.</p>
     *
     * @param applicationClass application class name input
     * @param projectName      project name input
     * @param packageName      package name input
     * @param projectPath      base filesystem path input
     * @return error message string, or null if valid
     */
    private static String validateInputs(String applicationClass, String projectName, String packageName, String projectPath) {
        applicationClass = trimToNull(applicationClass);
        projectName = trimToNull(projectName);
        packageName = trimToNull(packageName);
        projectPath = trimToNull(projectPath);

        if (applicationClass == null) return "Application class is required.";
        if (!isValidApplicationClassName(applicationClass))
            return "Application class must be a valid Java identifier (no spaces/symbols). \nExample: MyGameApp";

        if (projectName == null) return "Project name is required.";
        if (!isValidProjectName(projectName)) {
            return "Project name can't contain spaces or slashes. \nExample: MyGameProject";
        }

        if (packageName == null) return "Package name is required.";
        if (!isValidPackageName(packageName)) {
            return "Package name must contain dots and no spaces or invalid identifiers. \nExample: io.github.myname";
        }

        if (projectPath == null) return "Project path is required.";
        if (containsWhitespace(projectPath))
            return "Project path must not contain spaces. \nExample: C:/Dev/Projects";

        try {
            Paths.get(projectPath);
        } catch (InvalidPathException ex) {
            return "Project path is not a valid path: " + ex.getMessage();
        }
        return null;
    }

    /**
     * Builds a new project folder using the current template set.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Validates all input and updates the message label on failure.</li>
     *     <li>Starts a background task to perform file system operations.</li>
     *     <li>Reads templates as strings, performs token replacement, and writes output files.</li>
     *     <li>Creates the assets folder and the Java source folder for the chosen package.</li>
     * </ul>
     *
     * <p>UI updates are posted back to the render thread using {@link JGL#runTask(Runnable)}.</p>
     *
     * @param message          UI label used to display progress and errors
     * @param applicationClass application class name entered by the user
     * @param projectName      project name entered by the user
     * @param packageName      Java package name entered by the user
     * @param projectPath      base path where the project folder will be created
     */
    private void buildProject(Label message, String applicationClass, String projectName, String packageName, String projectPath) {
        String validationError = validateInputs(applicationClass, projectName, packageName, projectPath);
        if (validationError != null) {
            message.setText(validationError);
            return;
        }

        final String appClass = applicationClass.trim();
        final String projName = projectName.trim();
        final String pkgName = packageName.trim();
        final String projPath = projectPath.trim();

        message.setText("Building project...");

        CompletableFuture.supplyAsync(() -> {
            final Path basePath;
            try {
                basePath = Paths.get(projPath);
            } catch (InvalidPathException ex) {
                return "Error: Project path is not a valid path: " + ex.getMessage();
            }

            if (!Files.exists(basePath)) {
                return "Error: Base project path does not exist: " + basePath;
            }
            if (!Files.isDirectory(basePath)) {
                return "Error: Base project path is not a directory: " + basePath;
            }

            String gradleTemplate = ValthorneFiles.readString(TEMPLATE_BUILD_GRADLE)
                    .replace("PACKAGE_NAME", pkgName)
                    .replace("PROJECT_NAME", projName)
                    .replace("APPLICATION_CLASS", appClass)
                    .replace("SOURCE_DIRECTORY", pkgName.replace(".", "/"));

            String settingsGradleTemplate = ValthorneFiles.readString(TEMPLATE_SETTINGS_GRADLE)
                    .replace("PROJECT_NAME", projName);

            String gitIgnoreTemplate = ValthorneFiles.readString(TEMPLATE_GITIGNORE);

            String readmeTemplate = ValthorneFiles.readString(TEMPLATE_README)
                    .replace("PACKAGE_NAME", pkgName)
                    .replace("PROJECT_NAME", projName)
                    .replace("APPLICATION_CLASS", appClass);

            String applicationTemplate = ValthorneFiles.readString(TEMPLATE_APPLICATION)
                    .replace("PACKAGE_NAME", pkgName)
                    .replace("APPLICATION_CLASS", appClass);

            String launcherTemplate = ValthorneFiles.readString(TEMPLATE_LAUNCHER)
                    .replace("PACKAGE_NAME", pkgName)
                    .replace("APPLICATION_NAME", appClass)
                    .replace("PROJECT_NAME", projName);

            Path root = basePath.resolve(projName);
            Path assets = root.resolve("assets");
            Path javaDir = root.resolve(Paths.get("src", pkgName.replace(".", "/")));

            if (Files.exists(root)) {
                return "Error: Project already exists at " + root;
            }

            try {
                Files.createDirectories(root);
                Files.createDirectories(assets);
                Files.createDirectories(javaDir);

                Files.writeString(root.resolve("build.gradle"), gradleTemplate);
                Files.writeString(root.resolve("settings.gradle"), settingsGradleTemplate);
                Files.writeString(root.resolve(".gitignore"), gitIgnoreTemplate);
                Files.writeString(root.resolve("README.md"), readmeTemplate);

                Files.writeString(javaDir.resolve(appClass + ".java"), applicationTemplate);
                Files.writeString(javaDir.resolve("Launcher.java"), launcherTemplate);
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: Failed to build project: " + e.getMessage();
            }

            return "Project built successfully to " + root;
        }).whenComplete((result, _) -> JGL.runTask(() -> message.setText(result)));
    }
}