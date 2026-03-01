package io.github.tehnewb;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * The {@code SwingLauncher} class is a Swing-based application to generate
 * and scaffold Java project templates using a custom graphical user interface.
 * This was created to provide support for people using Java 1.8
 *
 * <p>The class provides functionality to specify project details and directory paths
 * via form inputs and triggers a build process to scaffold the project structure.
 * It includes UI components like input fields, buttons, and panels, as well as
 * methods for validation, event handling, and filesystem operations.</p>
 *
 * <p>Key fields include:</p>
 * - {@code TEMPLATE_ROOT}, {@code TEMPLATE_BUILD_GRADLE}, {@code TEMPLATE_SETTINGS_GRADLE},
 * {@code TEMPLATE_GITIGNORE}, {@code TEMPLATE_README}, {@code TEMPLATE_APPLICATION},
 * {@code TEMPLATE_LAUNCHER}: representing paths to essential project template files.
 * - {@code VALTHORNE_VERSION}: specifying the application version.
 * - {@code UTF8}: encoding constant.
 * - UI components like {@code frame}, {@code applicationClassField}, {@code projectNameField},
 * {@code packageNameField}, {@code projectPathField}, {@code buildButton}, and {@code browseButton}.
 *
 * <p>Main methods include:</p>
 * - {@code main}: The entry point for launching the Swing application.
 * - {@code start}: Initializes and shows the main application window.
 * - {@code buildHeader}, {@code buildForm}, {@code buildFooter}: Build the UI sections.
 * - {@code onBrowseClicked}, {@code onBuildClicked}: Event handlers for user interactions.
 * - {@code validateInputs}, {@code validatePreflight}: Validation utilities for user inputs and preflight checks.
 * - {@code buildProject}: Executes the core project scaffold logic.
 * - {@code readTemplate}, {@code writeString}: Template read/write utilities for filesystem operations.
 *
 * <p>Utility methods are provided to handle text input validation, content reading/writing,
 * and user messaging (info and error dialogs).</p>
 *
 * @author Albert Beaupre
 * @since March 1st, 2026
 */
public class SwingLauncher {

    private static final String TEMPLATE_ROOT = "project_template"; // Root folder containing all scaffold templates.
    private static final String TEMPLATE_BUILD_GRADLE = TEMPLATE_ROOT + "/template_build.gradle"; // Template file for build.gradle.
    private static final String TEMPLATE_SETTINGS_GRADLE = TEMPLATE_ROOT + "/template_settings.gradle"; // Template file for settings.gradle.
    private static final String TEMPLATE_GITIGNORE = TEMPLATE_ROOT + "/template_gitignore"; // Template file for .gitignore.
    private static final String TEMPLATE_README = TEMPLATE_ROOT + "/template_README.md"; // Template file for README.md.
    private static final String TEMPLATE_APPLICATION = TEMPLATE_ROOT + "/template_application.java"; // Template file for the generated Application class.
    private static final String TEMPLATE_LAUNCHER = TEMPLATE_ROOT + "/template_launcher.java"; // Template file for the generated Launcher class.

    private static final String VALTHORNE_VERSION = "REPLACE_ME"; // Engine version token substituted into generated build files.
    private static final Charset UTF8 = StandardCharsets.UTF_8; // Charset used for reading and writing templates/output files.

    private JFrame frame; // Main application window.
    private JTextField applicationClassField; // Input field for the generated Application class name.
    private JTextField projectNameField; // Input field for the generated project folder/name.
    private JTextField packageNameField; // Input field for the generated Java package name.
    private JTextField projectPathField; // Input field for the base directory where the project will be created.
    private JButton buildButton; // Button that starts the project generation flow.
    private JButton browseButton; // Button that opens a directory chooser for the project path.

    /**
     * Application entry point.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Installs FlatLaf dark theme.</li>
     *     <li>Creates the Swing UI on the EDT.</li>
     * </ul>
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new SwingLauncher().start());
    }

    /**
     * Builds and shows the main window.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Creates a fixed-size frame.</li>
     *     <li>Adds header, form, and footer panels.</li>
     *     <li>Packs and centers the window.</li>
     * </ul>
     */
    private void start() {
        frame = new JFrame("Valthorne Project Builder");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Creates the header section (title and subtitle).
     *
     * <p>Returns a vertically stacked panel containing:</p>
     * <ul>
     *     <li>Bold title label.</li>
     *     <li>Muted subtitle label.</li>
     * </ul>
     *
     * @return header component
     */
    private JComponent buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel title = new JLabel("Valthorne Project Builder");
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 6f));

        JLabel subtitle = new JLabel("Generate a Gradle project from templates.");
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));

        header.add(title);
        header.add(subtitle);
        return header;
    }

    /**
     * Creates the form section containing all text inputs and the browse button.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Creates labeled rows using {@link GridBagLayout}.</li>
     *     <li>Initializes default placeholder values and tooltips.</li>
     *     <li>Wires the browse button to open a directory chooser.</li>
     *     <li>Sets the build button as the default action for Enter.</li>
     * </ul>
     *
     * @return form component
     */
    private JComponent buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(6, 0, 12, 0));

        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy = 0;

        final int vPad = 10;
        final int labelToFieldGap = 18;

        applicationClassField = new JTextField("ApplicationClassName", 30);
        projectNameField = new JTextField("Project_Name", 30);
        packageNameField = new JTextField("package.name", 30);
        projectPathField = new JTextField("C:/Project/Path", 30);

        applicationClassField.setToolTipText("Example: MyGameApp");
        projectNameField.setToolTipText("Example: MyGameProject (no spaces)");
        packageNameField.setToolTipText("Example: io.github.myname");
        projectPathField.setToolTipText("Example: C:/Dev/Projects (base folder)");

        addFormRow(form, gc, vPad, labelToFieldGap, 0, "Application class", applicationClassField, null);
        addFormRow(form, gc, vPad, labelToFieldGap, 1, "Project name", projectNameField, null);
        addFormRow(form, gc, vPad, labelToFieldGap, 2, "Package name", packageNameField, null);

        browseButton = new JButton("Browse...");
        browseButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onBrowseClicked();
            }
        });

        addFormRow(form, gc, vPad, labelToFieldGap, 3, "Project path", projectPathField, browseButton);

        frame.getRootPane().setDefaultButton(getOrCreateBuildButton());

        return form;
    }

    /**
     * Creates the footer section containing the Build and Quit buttons.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Creates a centered row of buttons.</li>
     *     <li>Wires Quit to dispose the frame.</li>
     * </ul>
     *
     * @return footer component
     */
    private JComponent buildFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton build = getOrCreateBuildButton();
        JButton quit = new JButton("Quit");
        quit.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });

        buttons.add(build);
        buttons.add(quit);

        footer.add(buttons);
        return footer;
    }

    /**
     * Returns the build button, creating it if necessary.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Creates the button once.</li>
     *     <li>Marks it as the default-style FlatLaf button.</li>
     *     <li>Wires it to start the build flow.</li>
     * </ul>
     *
     * @return build button instance
     */
    private JButton getOrCreateBuildButton() {
        if (buildButton != null) return buildButton;

        buildButton = new JButton("Build Project");
        buildButton.putClientProperty("JButton.buttonType", "default");
        buildButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onBuildClicked();
            }
        });

        return buildButton;
    }

    /**
     * Adds a single row to the form.
     *
     * <p>Row layout:</p>
     * <ul>
     *     <li>Column 0: label</li>
     *     <li>Column 1: field</li>
     *     <li>Column 2: optional trailing component (e.g. Browse button)</li>
     * </ul>
     *
     * @param panel           parent container
     * @param gc              shared constraints object (mutated)
     * @param vPad            vertical padding inside row insets
     * @param labelToFieldGap horizontal gap between label and field
     * @param row             row index
     * @param labelText       label text (without colon)
     * @param field           main input component
     * @param trailing        optional trailing component (may be null)
     */
    private void addFormRow(JPanel panel, GridBagConstraints gc, int vPad, int labelToFieldGap, int row, String labelText, JComponent field, JComponent trailing) {
        JLabel label = new JLabel(labelText + ":");
        label.setLabelFor(field);

        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        gc.gridwidth = 1;
        gc.insets = new Insets(vPad, 0, vPad, labelToFieldGap);
        panel.add(label, gc);

        gc.gridx = 1;
        gc.gridy = row;
        gc.weightx = 1;
        gc.gridwidth = 1;
        gc.insets = new Insets(vPad, 0, vPad, 10);
        panel.add(field, gc);

        if (trailing != null) {
            gc.gridx = 2;
            gc.gridy = row;
            gc.weightx = 0;
            gc.gridwidth = 1;
            gc.insets = new Insets(vPad, 0, vPad, 0);
            panel.add(trailing, gc);
        }
    }

    /**
     * Opens a directory chooser and writes the selected folder to the path field.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Uses {@link JFileChooser#DIRECTORIES_ONLY}.</li>
     *     <li>Attempts to start from the current path field, if it exists.</li>
     *     <li>Normalizes path separators to forward slashes for display.</li>
     * </ul>
     */
    private void onBrowseClicked() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select base folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        try {
            String current = trimToNull(projectPathField.getText());
            if (current != null) {
                Path p = Paths.get(current);
                if (Files.exists(p)) chooser.setCurrentDirectory(p.toFile());
            }
        } catch (Exception ignored) {}

        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            if (dir != null) projectPathField.setText(dir.getAbsolutePath().replace('\\', '/'));
        }
    }

    /**
     * Handles the Build button press.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Validates input formatting rules.</li>
     *     <li>Runs preflight checks (paths + templates) before starting background work.</li>
     *     <li>Starts a {@link SwingWorker} to run filesystem operations off the EDT.</li>
     *     <li>Shows a progress dialog and reports success/failure at the end.</li>
     * </ul>
     */
    private void onBuildClicked() {
        final String appClass = applicationClassField.getText();
        final String projName = projectNameField.getText();
        final String pkgName = packageNameField.getText();
        final String projPath = projectPathField.getText();

        String validationError = validateInputs(appClass, projName, pkgName, projPath);
        if (validationError != null) {
            showError("Invalid input", validationError);
            return;
        }

        validationError = validatePreflight(projName, projPath);
        if (validationError != null) {
            showError("Cannot build", validationError);
            return;
        }

        final String finalAppClass = appClass.trim();
        final String finalProjName = projName.trim();
        final String finalPkgName = pkgName.trim();
        final String finalProjPath = projPath.trim();

        setBusy(true);

        final JDialog progress = createProgressDialog(frame, "Building project...");
        progress.setModalityType(Dialog.ModalityType.MODELESS);
        progress.setVisible(true);

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    return buildProject(finalAppClass, finalProjName, finalPkgName, finalProjPath);
                } catch (Throwable t) {
                    t.printStackTrace();
                    return "Error: " + t.getMessage();
                }
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String result = get();
                            if (result != null && result.startsWith("Error:")) {
                                showError("Build failed", result);
                            } else {
                                showInfo("Build complete", result);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showError("Build failed", "Unexpected error: " + ex.getMessage());
                        } finally {
                            progress.dispose();
                            setBusy(false);
                        }
                    }
                });
            }
        };

        worker.execute();
    }

    /**
     * Creates a small indeterminate progress dialog.
     *
     * <p>This dialog:</p>
     * <ul>
     *     <li>Is non-resizable.</li>
     *     <li>Disables closing while work is running.</li>
     *     <li>Shows a label and an indeterminate progress bar.</li>
     * </ul>
     *
     * @param owner   owner window for centering
     * @param message message to display
     * @return dialog instance (not yet shown)
     */
    private static JDialog createProgressDialog(Window owner, String message) {
        final JDialog dialog = new JDialog(owner, "Working", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 10));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel label = new JLabel(message);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        root.add(label, BorderLayout.NORTH);
        root.add(bar, BorderLayout.CENTER);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        return dialog;
    }

    /**
     * Shows an informational message box.
     *
     * @param title   dialog title
     * @param message dialog message (null becomes empty)
     */
    private void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(frame, message == null ? "" : message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows an error message box.
     *
     * @param title   dialog title
     * @param message dialog message (null becomes empty)
     */
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(frame, message == null ? "" : message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Enables or disables the UI controls and sets the cursor to indicate work in progress.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Disables input fields and buttons while building.</li>
     *     <li>Applies {@link Cursor#WAIT_CURSOR} during work.</li>
     * </ul>
     *
     * @param busy true to disable controls, false to re-enable
     */
    private void setBusy(boolean busy) {
        if (buildButton != null) buildButton.setEnabled(!busy);
        if (browseButton != null) browseButton.setEnabled(!busy);

        applicationClassField.setEnabled(!busy);
        projectNameField.setEnabled(!busy);
        packageNameField.setEnabled(!busy);
        projectPathField.setEnabled(!busy);

        frame.setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private static String trimToNull(String s) { // Normalizes user input by trimming; empty becomes null.
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static boolean containsWhitespace(String s) { // Returns true if any character is whitespace.
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) return true;
        }
        return false;
    }

    private static boolean isValidJavaIdentifier(String s) { // Validates a single Java identifier token.
        if (s == null || s.isEmpty()) return false;
        if (!Character.isJavaIdentifierStart(s.charAt(0))) return false;
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) return false;
        }
        return true;
    }

    private static boolean isValidProjectName(String s) { // Validates a project folder name (no whitespace or separators).
        if (s == null || s.isEmpty()) return false;
        if (containsWhitespace(s)) return false;
        return !s.contains("/") && !s.contains("\\");
    }

    private static boolean isValidPackageName(String s) { // Validates a dot-separated Java package name.
        if (s == null || s.isEmpty()) return false;
        if (containsWhitespace(s)) return false;

        String[] parts = s.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            if (p.isEmpty() || !isValidJavaIdentifier(p)) return false;
        }
        return true;
    }

    private static boolean isValidApplicationClassName(String s) { // Validates a simple class name as a Java identifier.
        if (s == null || s.isEmpty()) return false;
        if (containsWhitespace(s)) return false;
        return isValidJavaIdentifier(s);
    }

    /**
     * Validates raw text inputs and returns a human-readable error string for UI display.
     *
     * <p>This method checks:</p>
     * <ul>
     *     <li>Required fields are present.</li>
     *     <li>Application class is a valid identifier.</li>
     *     <li>Project name has no spaces or slashes.</li>
     *     <li>Package name is dot-separated valid identifiers.</li>
     *     <li>Project path is parseable and contains no whitespace.</li>
     * </ul>
     *
     * @param applicationClass user-entered application class name
     * @param projectName      user-entered project name
     * @param packageName      user-entered package name
     * @param projectPath      user-entered base folder path
     * @return error message, or null if valid
     */
    private static String validateInputs(String applicationClass, String projectName, String packageName, String projectPath) {
        applicationClass = trimToNull(applicationClass);
        projectName = trimToNull(projectName);
        packageName = trimToNull(packageName);
        projectPath = trimToNull(projectPath);

        if (applicationClass == null) return "Application class is required.\nExample: MyGameApp";
        if (!isValidApplicationClassName(applicationClass))
            return "Application class must be a valid Java identifier.\nExample: MyGameApp";

        if (projectName == null) return "Project name is required.\nExample: MyGameProject";
        if (!isValidProjectName(projectName))
            return "Project name cannot contain spaces or slashes.\nExample: MyGameProject";

        if (packageName == null) return "Package name is required.\nExample: io.github.myname";
        if (!isValidPackageName(packageName))
            return "Package name must contain dots and valid identifiers.\nExample: io.github.myname";

        if (projectPath == null) return "Project path is required.\nExample: C:/Dev/Projects";
        if (containsWhitespace(projectPath)) return "Project path must not contain spaces.\nExample: C:/Dev/Projects";

        try {
            Paths.get(projectPath);
        } catch (InvalidPathException ex) {
            return "Project path is not valid: " + ex.getMessage();
        }

        return null;
    }

    /**
     * Validates filesystem state and template presence before a background build is started.
     *
     * <p>This method checks:</p>
     * <ul>
     *     <li>The base project path exists.</li>
     *     <li>The base project path is a directory.</li>
     *     <li>The target project directory does not already exist.</li>
     *     <li>All required templates exist either on the classpath or in the filesystem fallback.</li>
     * </ul>
     *
     * @param projectName project name text (may contain whitespace prior to trimming)
     * @param projectPath base folder path text (may contain whitespace prior to trimming)
     * @return error message, or null if preflight passes
     */
    private static String validatePreflight(String projectName, String projectPath) {
        projectName = trimToNull(projectName);
        projectPath = trimToNull(projectPath);

        if (projectName == null || projectPath == null) return "Missing required fields.";

        final Path basePath;
        try {
            basePath = Paths.get(projectPath);
        } catch (InvalidPathException ex) {
            return "Project path is not a valid path: " + ex.getMessage();
        }

        if (!Files.exists(basePath)) {
            return "Base project path does not exist:\n" + basePath;
        }

        if (!Files.isDirectory(basePath)) {
            return "Base project path is not a directory:\n" + basePath;
        }

        Path root = basePath.resolve(projectName);
        if (Files.exists(root)) {
            return "Project already exists at:\n" + root;
        }

        String missing = firstMissingTemplate();
        if (missing != null) {
            return "Missing template:\n" + missing + "\n\nEnsure templates exist on the classpath or next to the application.";
        }

        return null;
    }

    /**
     * Finds the first missing template path, checking classpath first and filesystem fallback second.
     *
     * <p>This is used by {@link #validatePreflight(String, String)} so you can show a specific missing file.</p>
     *
     * @return missing template path, or null if all templates are present
     */
    private static String firstMissingTemplate() {
        String[] templates = {TEMPLATE_BUILD_GRADLE, TEMPLATE_SETTINGS_GRADLE, TEMPLATE_GITIGNORE, TEMPLATE_README, TEMPLATE_APPLICATION, TEMPLATE_LAUNCHER};

        ClassLoader cl = SwingLauncher.class.getClassLoader();

        for (int i = 0; i < templates.length; i++) {
            String path = templates[i];

            InputStream in = cl.getResourceAsStream(path);
            if (in != null) {
                try {in.close();} catch (IOException ignored) {}
                continue;
            }

            if (!Files.exists(Paths.get(path))) {
                return path;
            }
        }

        return null;
    }

    private String buildProject(String appClass, String projName, String pkgName, String projPath) { // Creates folders/files for a new project using templates.
        final Path basePath = Paths.get(projPath);

        String gradleTemplate = readTemplate(TEMPLATE_BUILD_GRADLE)
                .replace("PACKAGE_NAME", pkgName)
                .replace("PROJECT_NAME", projName)
                .replace("APPLICATION_CLASS", appClass)
                .replace("VALTHORNE_VERSION", VALTHORNE_VERSION)
                .replace("SOURCE_DIRECTORY", pkgName.replace(".", "/"));

        String settingsGradleTemplate = readTemplate(TEMPLATE_SETTINGS_GRADLE)
                .replace("PROJECT_NAME", projName);

        String gitIgnoreTemplate = readTemplate(TEMPLATE_GITIGNORE);

        String readmeTemplate = readTemplate(TEMPLATE_README)
                .replace("PACKAGE_NAME", pkgName)
                .replace("PROJECT_NAME", projName)
                .replace("APPLICATION_CLASS", appClass);

        String applicationTemplate = readTemplate(TEMPLATE_APPLICATION)
                .replace("PACKAGE_NAME", pkgName)
                .replace("APPLICATION_CLASS", appClass);

        String launcherTemplate = readTemplate(TEMPLATE_LAUNCHER)
                .replace("PACKAGE_NAME", pkgName)
                .replace("APPLICATION_NAME", appClass)
                .replace("PROJECT_NAME", projName);

        Path root = basePath.resolve(projName);
        Path assets = root.resolve("assets");
        Path javaDir = root.resolve(Paths.get("src", pkgName.replace(".", "/")));

        try {
            Files.createDirectories(root);
            Files.createDirectories(assets);
            Files.createDirectories(javaDir);

            writeString(root.resolve("build.gradle"), gradleTemplate);
            writeString(root.resolve("settings.gradle"), settingsGradleTemplate);
            writeString(root.resolve(".gitignore"), gitIgnoreTemplate);
            writeString(root.resolve("README.md"), readmeTemplate);

            writeString(javaDir.resolve(appClass + ".java"), applicationTemplate);
            writeString(javaDir.resolve("Launcher.java"), launcherTemplate);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Failed to build project: " + e.getMessage();
        }

        return "Project built successfully to:\n" + root;
    }

    private static String readTemplate(String relativePath) { // Loads a template from the classpath or filesystem fallback.
        InputStream in = SwingLauncher.class.getClassLoader().getResourceAsStream(relativePath);

        if (in != null) {
            try {
                return readAllToString(in, UTF8);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read classpath template: " + relativePath, e);
            } finally {
                try {in.close();} catch (IOException ignored) {}
            }
        }

        Path p = Paths.get(relativePath);
        if (!Files.exists(p)) {
            throw new RuntimeException("Missing template: " + relativePath);
        }

        try (BufferedReader br = Files.newBufferedReader(p, UTF8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = br.read(buf)) != -1) {
                sb.append(buf, 0, n);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read template: " + relativePath, e);
        }
    }

    private static String readAllToString(InputStream in, Charset charset) throws IOException { // Reads an entire stream into a String using a charset.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        return new String(baos.toByteArray(), charset);
    }

    private static void writeString(Path path, String content) throws IOException { // Writes a String to disk using UTF-8, replacing existing content.
        try (BufferedWriter bw = Files.newBufferedWriter(path, UTF8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            bw.write(content);
        }
    }
}