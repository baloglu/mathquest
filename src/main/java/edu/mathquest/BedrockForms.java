package edu.mathquest;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;
import java.util.function.IntConsumer;

/** Floodgate form support. Only loaded when the floodgate plugin is present. */
final class BedrockForms {
    private BedrockForms() {}

    static boolean isBedrock(UUID id) {
        return FloodgateApi.getInstance().isFloodgatePlayer(id);
    }

    /** Shows a button form; onClick receives the button index (called off the main thread). */
    static void show(UUID id, Ui.Screen screen, IntConsumer onClick) {
        SimpleForm.Builder b = SimpleForm.builder().title(screen.title()).content(screen.body());
        for (Ui.Button button : screen.buttons()) {
            if (button.image() == null) b.button(button.label());
            else b.button(button.label(), FormImage.Type.PATH, button.image());
        }
        b.validResultHandler(response -> onClick.accept(response.clickedButtonId()));
        FloodgateApi.getInstance().sendForm(id, b);
    }
}
