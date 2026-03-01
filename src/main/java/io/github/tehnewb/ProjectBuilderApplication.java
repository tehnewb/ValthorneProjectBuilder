package io.github.tehnewb;

import valthorne.Application;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class ProjectBuilderApplication implements Application {

    private static final String TEMPLATE_ROOT = "project_template";
    private static final String TEMPLATE_BUILD_GRADLE = TEMPLATE_ROOT + "/template_build.gradle";
    private static final String TEMPLATE_SETTINGS_GRADLE = TEMPLATE_ROOT + "/template_settings.gradle";
    private static final String TEMPLATE_GITIGNORE = TEMPLATE_ROOT + "/template_gitignore";
    private static final String TEMPLATE_README = TEMPLATE_ROOT + "/template_README.md";
    private static final String TEMPLATE_APPLICATION = TEMPLATE_ROOT + "/template_application.java";
    private static final String TEMPLATE_LAUNCHER = TEMPLATE_ROOT + "/template_launcher.java";

    private UI ui;

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

        Assets.prepare(FontParameters.fromClasspath("ui/font.ttf", "font", 24));
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
                buildProject(message, applicationClass.getText(), projectName.getText(), packageName.getText(), projectPath.getText());
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

    @Override
    public void render() {
        ui.draw();
    }

    @Override
    public void update(float delta) {
        ui.update(delta);
    }

    @Override
    public void dispose() {
        ui.dispose();
    }

    private void buildProject(Label message, String applicationClass, String projectName, String packageName, String projectPath) {
        message.setText("Building project...");

        CompletableFuture.supplyAsync(() -> {
            String gradleTemplate = ValthorneFiles.readString(TEMPLATE_BUILD_GRADLE)
                    .replace("PACKAGE_NAME", packageName)
                    .replace("PROJECT_NAME", projectName)
                    .replace("APPLICATION_CLASS", applicationClass)
                    .replace("VALTHORNE_VERSION", Global.VALTHORNE_VERSION)
                    .replace("SOURCE_DIRECTORY", packageName.replace(".", "/"));

            String settingsGradleTemplate = ValthorneFiles.readString(TEMPLATE_SETTINGS_GRADLE)
                    .replace("PROJECT_NAME", projectName);

            String gitIgnoreTemplate = ValthorneFiles.readString(TEMPLATE_GITIGNORE);

            String readmeTemplate = ValthorneFiles.readString(TEMPLATE_README)
                    .replace("PACKAGE_NAME", packageName)
                    .replace("PROJECT_NAME", projectName)
                    .replace("APPLICATION_CLASS", applicationClass);

            String applicationTemplate = ValthorneFiles.readString(TEMPLATE_APPLICATION)
                    .replace("PACKAGE_NAME", packageName)
                    .replace("APPLICATION_CLASS", applicationClass);

            String launcherTemplate = ValthorneFiles.readString(TEMPLATE_LAUNCHER)
                    .replace("PACKAGE_NAME", packageName)
                    .replace("APPLICATION_NAME", applicationClass)
                    .replace("PROJECT_NAME", projectName);

            Path root = Paths.get(projectPath).resolve(projectName);
            Path assets = root.resolve("assets");
            Path javaDir = root.resolve(Paths.get("src", packageName.replace(".", "/")));

            if (Files.exists(root)) {
                return "Project already exists at " + projectPath;
            }

            try {
                // Ensure directories exist
                Files.createDirectories(root);
                Files.createDirectories(assets);
                Files.createDirectories(javaDir);

                // Write top-level files
                Files.writeString(root.resolve("build.gradle"), gradleTemplate);
                Files.writeString(root.resolve("settings.gradle"), settingsGradleTemplate);
                Files.writeString(root.resolve(".gitignore"), gitIgnoreTemplate);
                Files.writeString(root.resolve("README.md"), readmeTemplate);

                // Write sources
                Files.writeString(javaDir.resolve(applicationClass + ".java"), applicationTemplate);
                Files.writeString(javaDir.resolve("Launcher.java"), launcherTemplate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Project built successfully to " + projectPath;
        }).whenComplete((result, _) -> JGL.runTask(() -> message.setText(result)));
    }
}