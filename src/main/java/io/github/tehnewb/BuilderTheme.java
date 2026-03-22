package io.github.tehnewb;

import valthorne.asset.Assets;
import valthorne.graphics.Color;
import valthorne.graphics.font.Font;
import valthorne.graphics.font.FontData;
import valthorne.graphics.texture.NinePatchDrawable;
import valthorne.graphics.texture.TextureData;
import valthorne.ui.nodes.Button;
import valthorne.ui.nodes.Label;
import valthorne.ui.nodes.TextField;
import valthorne.ui.theme.StyleState;
import valthorne.ui.theme.Theme;
import valthorne.ui.theme.ThemeData;

public final class BuilderTheme implements Theme {

    @Override
    public ThemeData create() {
        ThemeData theme = new ThemeData();
        Font font = new Font(Assets.get("font", FontData.class));

        NinePatchDrawable buttonBackground = new NinePatchDrawable(Assets.get("button-background", TextureData.class), 2, 2, 2, 2);
        NinePatchDrawable buttonHovered = new NinePatchDrawable(Assets.get("button-hovered", TextureData.class), 2, 2, 2, 2);
        NinePatchDrawable buttonPressed = new NinePatchDrawable(Assets.get("button-pressed", TextureData.class), 2, 2, 2, 2);
        NinePatchDrawable textFieldBackground = new NinePatchDrawable(Assets.get("textfield-unfocused", TextureData.class), 2, 2, 2, 2);
        NinePatchDrawable textFieldFocused = new NinePatchDrawable(Assets.get("textfield-focused", TextureData.class), 2, 2, 2, 2);


        theme.rule(Label.class)
                .set(Label.FONT_KEY, font)
                .set(Label.COLOR_KEY, Color.WHITE);

        theme.rule(Button.class)
                .set(Button.BACKGROUND_KEY, buttonBackground);

        theme.rule(Button.class, null, StyleState.HOVERED)
                .set(Button.BACKGROUND_KEY, buttonHovered);

        theme.rule(Button.class, null, StyleState.PRESSED)
                .set(Button.BACKGROUND_KEY, buttonPressed);

        theme.rule(TextField.class)
                .set(TextField.BACKGROUND_KEY, textFieldBackground)
                .set(TextField.FONT_KEY, font)
                .set(TextField.COLOR_KEY, Color.WHITE)
                .set(TextField.PLACEHOLDER_COLOR_KEY, new Color(0.8f, 0.8f, 0.8f, 1f))
                .set(TextField.PADDING_KEY, 10f)
                .set(TextField.DEFAULT_SELECTION_COLOR_KEY, new Color(1f, 1f, 1f, 0.35f));

        theme.rule(TextField.class, null, StyleState.FOCUSED)
                .set(TextField.BACKGROUND_KEY, textFieldFocused);

        return theme;
    }
}