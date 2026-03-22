package io.github.tehnewb;

import valthorne.JGL;
import valthorne.Window;
import valthorne.asset.Assets;
import valthorne.graphics.Color;
import valthorne.graphics.font.FontParameters;
import valthorne.graphics.texture.Texture;
import valthorne.graphics.texture.TextureData;
import valthorne.graphics.texture.TextureParameters;
import valthorne.io.file.ValthorneFiles;
import valthorne.ui.UIRoot;
import valthorne.ui.nodes.*;
import valthorne.viewport.ScreenViewport;
import valthorne.viewport.Viewport;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class Application implements valthorne.Application {

    private static final String TEMPLATE_ROOT = "project_template";
    private static final String TEMPLATE_BUILD_GRADLE = TEMPLATE_ROOT + "/template_build.gradle";
    private static final String TEMPLATE_SETTINGS_GRADLE = TEMPLATE_ROOT + "/template_settings.gradle";
    private static final String TEMPLATE_GITIGNORE = TEMPLATE_ROOT + "/template_gitignore";
    private static final String TEMPLATE_PROPERTIES_GRADLE = TEMPLATE_ROOT + "/template_gradle.properties";
    private static final String TEMPLATE_README = TEMPLATE_ROOT + "/template_README.md";
    private static final String TEMPLATE_APPLICATION = TEMPLATE_ROOT + "/template_application.java";
    private static final String TEMPLATE_LAUNCHER = TEMPLATE_ROOT + "/template_launcher.java";

    private UIRoot ui;
    private Viewport viewport;
    private Texture backgroundTexture;
    private Label messageLabel;

    @Override
    public void init() {
        Window.setResizable(false);

        Assets.loadAsync(TextureParameters.fromClasspath("icons/96.png", "icon", false), TextureData.class)
                .whenComplete((textureData, _) -> JGL.runTask(() -> Window.setIcon(textureData)));

        viewport = new ScreenViewport(Window.getWidth(), Window.getHeight());

        Assets.prepare(FontParameters.fromClasspath("ui/font.ttf", "font", 18));
        Assets.prepare(TextureParameters.fromClasspath("ui/background.png", "background"));
        Assets.prepare(TextureParameters.fromClasspath("ui/textfield-focused.png", "textfield-focused"));
        Assets.prepare(TextureParameters.fromClasspath("ui/textfield-unfocused.png", "textfield-unfocused"));
        Assets.prepare(TextureParameters.fromClasspath("ui/button-background.png", "button-background"));
        Assets.prepare(TextureParameters.fromClasspath("ui/button-pressed.png", "button-pressed"));
        Assets.prepare(TextureParameters.fromClasspath("ui/button-hovered.png", "button-hovered"));

        Assets.load().whenComplete((_, _) -> JGL.runTask(this::buildUI));
    }

    @Override
    public void render() {
        Window.clear(Color.BLACK);

        if (ui != null)
            ui.draw();
    }

    @Override
    public void update(float delta) {
        if (ui != null)
            ui.update(delta);
    }

    @Override
    public void dispose() {
        if (ui != null)
            ui.dispose();

        if (backgroundTexture != null)
            backgroundTexture.dispose();
    }

    private void buildUI() {
        backgroundTexture = new Texture(Assets.get("background", TextureData.class));

        ui = new UIRoot();
        ui.setViewport(viewport);
        ui.setTheme(new BuilderTheme().create());

        Image background = new Image(backgroundTexture);
        background.getLayout()
                .absolute()
                .left(0)
                .top(0)
                .width(Window.getWidth())
                .height(Window.getHeight())
                .noGrow()
                .noShrink();

        Panel center = new Panel();
        center.getLayout()
                .fill()
                .grow()
                .justifyCenter()
                .itemsCenter();

        Panel form = new Panel();
        form.setStyleName("window");
        form.getLayout()
                .column()
                .noGrow()
                .noShrink()
                .justifyCenter()
                .itemsCenter()
                .gap(14)
                .padding(24)
                .width(760)
                .heightAuto();

        Label title = new Label("Welcome to the Valthorne Project Builder");
        title.getLayout()
                .widthAuto()
                .heightAuto()
                .noGrow()
                .noShrink();

        TextField applicationClass = new TextField("ApplicationClassName");
        applicationClass.getLayout()
                .width(520)
                .height(40)
                .noGrow()
                .noShrink();

        TextField projectName = new TextField("Project_Name");
        projectName.getLayout()
                .width(520)
                .height(40)
                .noGrow()
                .noShrink();

        TextField packageName = new TextField("package.name");
        packageName.getLayout()
                .width(520)
                .height(40)
                .noGrow()
                .noShrink();

        TextField projectPath = new TextField("C:/Project/Path");
        projectPath.getLayout()
                .width(520)
                .height(40)
                .noGrow()
                .noShrink();

        Button build = new Button("Build Project");
        build.getLayout()
                .width(180)
                .height(40)
                .noGrow()
                .noShrink();

        messageLabel = new Label("");
        messageLabel.getLayout()
                .width(520)
                .heightAuto()
                .noGrow()
                .noShrink();

        build.action(button -> buildProject(
                messageLabel,
                applicationClass.getText(),
                projectName.getText(),
                packageName.getText(),
                projectPath.getText()
        ));

        form.add(title);
        form.add(applicationClass);
        form.add(projectName);
        form.add(packageName);
        form.add(projectPath);
        form.add(build);
        form.add(messageLabel);

        center.add(form);

        ui.add(background);
        ui.add(center);
        ui.layout();
    }

    private static String trimToNull(String s) {
        if (s == null)
            return null;

        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static boolean containsWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i)))
                return true;
        }
        return false;
    }

    private static boolean isValidJavaIdentifier(String s) {
        if (s == null || s.isEmpty())
            return false;
        if (!Character.isJavaIdentifierStart(s.charAt(0)))
            return false;
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i)))
                return false;
        }
        return true;
    }

    private static boolean isValidProjectName(String s) {
        if (s == null || s.isEmpty())
            return false;
        if (containsWhitespace(s))
            return false;
        return !s.contains("/") && !s.contains("\\");
    }

    private static boolean isValidPackageName(String s) {
        if (s == null || s.isEmpty())
            return false;
        if (containsWhitespace(s))
            return false;

        String[] parts = s.split("\\.");
        for (String p : parts) {
            if (p.isEmpty() || !isValidJavaIdentifier(p))
                return false;
        }
        return true;
    }

    private static boolean isValidApplicationClassName(String s) {
        if (s == null || s.isEmpty())
            return false;
        if (containsWhitespace(s))
            return false;
        return isValidJavaIdentifier(s);
    }

    private static String validateInputs(String applicationClass, String projectName, String packageName, String projectPath) {
        applicationClass = trimToNull(applicationClass);
        projectName = trimToNull(projectName);
        packageName = trimToNull(packageName);
        projectPath = trimToNull(projectPath);

        if (applicationClass == null)
            return "Application class is required.";
        if (!isValidApplicationClassName(applicationClass))
            return "Application class must be a valid Java identifier. Example: MyGameApp";

        if (projectName == null)
            return "Project name is required.";
        if (!isValidProjectName(projectName))
            return "Project name cannot contain spaces or slashes. Example: MyGameProject";

        if (packageName == null)
            return "Package name is required.";
        if (!isValidPackageName(packageName))
            return "Package name must be dot-separated and valid. Example: io.github.myname";

        if (projectPath == null)
            return "Project path is required.";
        if (containsWhitespace(projectPath))
            return "Project path must not contain spaces. Example: C:/Dev/Projects";

        try {
            Paths.get(projectPath);
        } catch (InvalidPathException ex) {
            return "Project path is not valid: " + ex.getMessage();
        }

        return null;
    }

    private void buildProject(Label message, String applicationClass, String projectName, String packageName, String projectPath) {
        String validationError = validateInputs(applicationClass, projectName, packageName, projectPath);
        if (validationError != null) {
            message.text(validationError);
            ui.layout();
            return;
        }

        final String appClass = applicationClass.trim();
        final String projName = projectName.trim();
        final String pkgName = packageName.trim();
        final String projPath = projectPath.trim();

        message.text("Building project...");
        ui.layout();

        CompletableFuture.supplyAsync(() -> {
            final Path basePath;
            try {
                basePath = Paths.get(projPath);
            } catch (InvalidPathException ex) {
                return "Error: Project path is not valid: " + ex.getMessage();
            }

            if (!Files.exists(basePath))
                return "Error: Base project path does not exist: " + basePath;

            if (!Files.isDirectory(basePath))
                return "Error: Base project path is not a directory: " + basePath;

            String gradleTemplate = ValthorneFiles.readString(TEMPLATE_BUILD_GRADLE)
                    .replace("PACKAGE_NAME", pkgName)
                    .replace("PROJECT_NAME", projName)
                    .replace("APPLICATION_CLASS", appClass)
                    .replace("SOURCE_DIRECTORY", pkgName.replace(".", "/"));

            String settingsGradleTemplate = ValthorneFiles.readString(TEMPLATE_SETTINGS_GRADLE)
                    .replace("PROJECT_NAME", projName);

            String gradleProperties = ValthorneFiles.readString(TEMPLATE_PROPERTIES_GRADLE);

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

            if (Files.exists(root))
                return "Error: Project already exists at " + root;

            try {
                Files.createDirectories(root);
                Files.createDirectories(assets);
                Files.createDirectories(javaDir);

                Files.writeString(root.resolve("build.gradle"), gradleTemplate);
                Files.writeString(root.resolve("settings.gradle"), settingsGradleTemplate);
                Files.writeString(root.resolve("gradle.properties"), gradleProperties);
                Files.writeString(root.resolve(".gitignore"), gitIgnoreTemplate);
                Files.writeString(root.resolve("README.md"), readmeTemplate);
                Files.writeString(javaDir.resolve(appClass + ".java"), applicationTemplate);
                Files.writeString(javaDir.resolve("Launcher.java"), launcherTemplate);
            } catch (Exception e) {
                return "Error: Failed to build project: " + e.getMessage();
            }

            return "Project built successfully to " + root;
        }).whenComplete((result, _) -> JGL.runTask(() -> {
            if (message != null) {
                message.text(result);
                if (ui != null)
                    ui.layout();
            }
        }));
    }
}