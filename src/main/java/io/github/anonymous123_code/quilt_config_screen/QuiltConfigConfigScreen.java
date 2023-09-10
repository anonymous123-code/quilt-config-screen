package io.github.anonymous123_code.quilt_config_screen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.option.*;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
import io.github.anonymous123_code.quilt_config_screen.option.LongInputOption;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.metadata.Comments;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueTreeNode;

import java.util.ArrayList;

public class QuiltConfigConfigScreen extends SpruceScreen {
    private final ArrayList<Config> configs;
    private SpruceTabbedWidget tabbedWidget;
    private final Screen parent;

    protected QuiltConfigConfigScreen(Text title, ArrayList<Config> configs, Screen parent) {
        super(title);
        this.configs = configs;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        if (this.configs.size() > 1) {
            this.tabbedWidget = new SpruceTabbedWidget(Position.of(this, 0, 4), this.width, this.height - 35 - 4, this.title);
            for (Config config : configs) {
                this.tabbedWidget.addTabEntry(Text.of(convertIdToName(config.id())), null, (width1, height1) -> createConfigWidget(config, Position.origin(), width1, height1));
            }
            this.addDrawableChild(this.tabbedWidget);
        } else {
            this.addDrawableChild(createConfigWidget(configs.get(0), Position.of(this, 0, 4), this.width, this.height - 35 - 4));
        }

        // Add done button.
        this.addDrawableChild(new SpruceButtonWidget(Position.of(this, this.width / 2 - 75, this.height - 29), 150, 20, SpruceTexts.GUI_DONE,
                btn -> this.client.setScreen(this.parent)).asVanilla());
    }

    protected SpruceContainerWidget createConfigWidget(Config config, Position position, int width, int height) {
        var containerWidget = new SpruceContainerWidget(position, width, height);
        containerWidget.addChildren(((containerWidth, containerHeight, widgetAdder) -> {
            var optionListWidget = new SpruceOptionListWidget(Position.origin(), width, height);
            this.addOptions(config.nodes(), optionListWidget);
            widgetAdder.accept(optionListWidget);
        }));
        return containerWidget;
    }

    protected void addOptions(Iterable<ValueTreeNode> nodes, SpruceOptionListWidget optionListWidget) {
        for (ValueTreeNode node : nodes) {
            if (node instanceof TrackedValue<?> trackedValue) {
                optionListWidget.addSingleOptionEntry(getOptionByTrackedValue(trackedValue));
                QuiltConfigScreen.LOGGER.info(trackedValue.toString());
            } else if (node instanceof ValueTreeNode.Section section) {
                optionListWidget.addSingleOptionEntry(new SpruceSeparatorOption(convertIdToName(node.key().getLastComponent()), true, buildTooltip(section.metadata(Comment.TYPE))));
                this.addOptions(section, optionListWidget);
            } else {
                QuiltConfigScreen.LOGGER.warn("Unknown value tree node: " + node.toString());
            }
        }
    }

    protected SpruceOption getOptionByTrackedValue(TrackedValue value) {
        String name = convertIdToName(value.key().getLastComponent());
        Object defaultValue = value.getDefaultValue();
        Text tooltip = buildTooltip((Comments) value.metadata(Comment.TYPE));
        if (defaultValue instanceof Boolean) {
            return new SpruceCheckboxBooleanOption(name, () -> (Boolean) value.value(), value::setOverride, tooltip);
        } else if (defaultValue instanceof Integer) {
            return new SpruceIntegerInputOption(name, () -> (Integer) value.value(), value::setOverride, tooltip);
        } else if (defaultValue instanceof Long) {
            return new LongInputOption(name, () -> (long) value.value(), value::setOverride, tooltip);
        } else if (defaultValue instanceof Float) {
            return new SpruceFloatInputOption(name, () -> (float) value.value(), value::setOverride, tooltip);
        } else if (defaultValue instanceof Double) {
            return new SpruceDoubleInputOption(name, () -> (double) value.value(), value::setOverride, tooltip);
        } else if (defaultValue instanceof String) {
            return new SpruceStringOption(name, () -> (String) value.value(), value::setOverride, s -> value.checkForFailingConstraints(s).isEmpty(), tooltip);
        } else {
            return new SpruceSeparatorOption("Currently unsupported conplex value", true, tooltip);
        }
    }

    protected String convertIdToName(String id) {
        return id.replace("_", " ");
    }

    protected Text buildTooltip(Comments comments) {
        StringBuilder result = new StringBuilder();
        for (String comment : comments) {
            result.append(comment);
            result.append("\n");
        }
        return Text.of(result.toString());
    }

    static class Factory implements ConfigScreenFactory<QuiltConfigConfigScreen> {
        public final String title;
        private final ArrayList<Config> configs = new ArrayList<>();

        Factory(String title) {
            this.title = title;
        }

        @Override
        public QuiltConfigConfigScreen create(Screen parent) {
            return new QuiltConfigConfigScreen(Text.of(title), configs, parent);
        }

        void addConfig(Config config) {
            configs.add(config);
        }
    }
}
