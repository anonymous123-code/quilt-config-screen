package io.github.anonymous123_code.quilt_config_screen.option;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.option.SpruceOption;
import dev.lambdaurora.spruceui.widget.SpruceWidget;
import dev.lambdaurora.spruceui.widget.text.SpruceNamedTextFieldWidget;
import dev.lambdaurora.spruceui.widget.text.SpruceTextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LongInputOption extends SpruceOption {
    private final Supplier<Long> getter;
    private final Consumer<Long> setter;

    public LongInputOption(String key, Supplier<Long> getter, Consumer<Long> setter, @Nullable Text tooltip) {
        super(key);
        this.getter = getter;
        this.setter = setter;
        this.setTooltip(tooltip);
    }

    @Override
    public SpruceWidget createWidget(Position position, int width) {
        var textField = new SpruceTextFieldWidget(position, width, 20, this.getPrefix());
        textField.setText(String.valueOf(this.get()));
        textField.setTextPredicate(SpruceTextFieldWidget.INTEGER_INPUT_PREDICATE);
        textField.setRenderTextProvider((displayedText, offset) -> {
            try {
                Long.parseLong(textField.getText());
                return OrderedText.forward(displayedText, Style.EMPTY);
            } catch (NumberFormatException e) {
                return OrderedText.forward(displayedText, Style.EMPTY.withColor(Formatting.RED));
            }
        });
        textField.setChangedListener(input -> {
            try {
                long value = Long.parseLong(input);
                this.set(value);
            } catch (NumberFormatException e) {
                this.set(0);
            }
        });
        this.getOptionTooltip().ifPresent(textField::setTooltip);
        return new SpruceNamedTextFieldWidget(textField);
    }

    public void set(long value) {
        this.setter.accept(value);
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public long get() {
        return this.getter.get();
    }
}
