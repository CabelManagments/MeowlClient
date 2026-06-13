package ru.whylol.client.ui.glint;

import net.glintwein.platform.GlintImage;
import net.glintwein.platform.Platform;
import net.glintwein.ui.GlobalUIState;
import net.glintwein.ui.data.BorderRadius;
import net.glintwein.ui.data.Bounds;
import net.glintwein.ui.data.Display;
import net.glintwein.ui.data.Edge;
import net.glintwein.ui.data.Gradient;
import net.glintwein.ui.data.Overflow;
import net.glintwein.ui.data.PositionType;
import net.glintwein.ui.element.Element;
import net.glintwein.ui.element.component.ColorPicker;
import net.glintwein.ui.render.command.Context;
import net.glintwein.ui.render.command.DrawRectBuilder;
import net.glintwein.ui.render.font.MdsfFont;
import net.glintwein.ui.render.texture.TextureSimple;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import ru.whylol.Xeron;
import ru.whylol.api.storages.implement.ThemeStorage;
import ru.whylol.api.storages.implement.helpertstorages.Theme;
import ru.whylol.api.utils.input.KeyBoardUtils;
import ru.whylol.client.modules.Module;
import ru.whylol.client.modules.settings.Setting;
import ru.whylol.client.modules.settings.implement.BindSetting;
import ru.whylol.client.modules.settings.implement.BooleanSetting;
import ru.whylol.client.modules.settings.implement.FloatSetting;
import ru.whylol.client.modules.settings.implement.ListSetting;
import ru.whylol.client.modules.settings.implement.ModeSetting;
import ru.whylol.client.modules.settings.implement.TextSetting;
import ru.whylol.client.ui.clickgui.ClickGuiState;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GlintweinClickGuiElement extends Element {
    private static final float WIDTH = 1120.0f;
    private static final float POPOVER_SPACE = 260.0f;
    private static final float ELEMENT_WIDTH = WIDTH + POPOVER_SPACE;
    private static final float HEIGHT = 540.25f;
    private static final float SIDEBAR_W = 199.27f;
    private static final float MODULE_W = 548.77f;
    private static final float DETAILS_W = WIDTH - SIDEBAR_W - MODULE_W;
    private static final float TOPBAR_H = 74.88f;
    private static final float DETAILS_HEADER_H = 75.35f;
    private static final float PAD = 6.46f;
    private static final float ROW_H = 34.42f;
    private static final float GAP = 5.03f;
    private static final float MENU_RADIUS = 26.0f;

    private static final float TITLE_FONT = 22.37f;
    private static final float CATEGORY_FONT = 14.78f;
    private static final float TOPBAR_FONT = 21.22f;
    private static final float MODULE_FONT = 15.35f;
    private static final float BIND_FONT = 13.67f;
    private static final float DETAILS_TITLE_FONT = 20.36f;
    private static final float DETAILS_DESC_FONT = 12.53f;
    private static final float SETTING_FONT = 15.22f;
    private static final float SMALL_FONT = 12.96f;

    private static final float TITLE_Y = -4.93f;
    private static final float CATEGORY_Y = -2.83f;
    private static final float TOPBAR_Y = -5.03f;
    private static final float MODULE_Y = -4.46f;
    private static final float BIND_Y = -6.18f;
    private static final float DETAILS_TITLE_Y = -4.84f;
    private static final float DETAILS_DESC_Y = -7.32f;
    private static final float SETTING_Y = -1.40f;
    private static final float SMALL_Y = -2.26f;

    private static final int TEXT = 0xfff3f4f8;
    private static final int MUTED = 0xffa5a8b4;
    private static final int SOFT = 0xff686d7a;
    private static final int LINE = 0x20ffffff;
    private static final int LINE_STRONG = 0x35ffffff;

    private final ClickGuiState state = new ClickGuiState();
    private final List<Hit> hits = new ArrayList<>();
    private final List<SliderHit> sliderHits = new ArrayList<>();
    private final Map<Object, Float> animations = new HashMap<>();
    private final Map<String, TextureSimple> iconTextures = new HashMap<>();
    private int customColorOne = 0xffff6f8e;
    private int customColorTwo = 0xff57d6c4;
    private final ColorPicker customColorPickerOne = new ColorPicker("Color 1", customColorOne);
    private final ColorPicker customColorPickerTwo = new ColorPicker("Color 2", customColorTwo);

    private View activeView = View.MODULES;
    private Module settingsModule;
    private Module bindingModule;
    private BindSetting bindingSetting;
    private FloatSetting draggingSlider;
    private SliderHit draggingLooseSlider;
    private ModeSetting openModeSetting;
    private ConfigEntry selectedConfig;

    private float moduleScrollTarget;
    private float moduleScroll;
    private float detailScrollTarget;
    private float detailScroll;
    private float lastMouseX;
    private float lastMouseRawX;
    private String search = "";
    private boolean searchActive;
    private boolean settingsOpen;
    private float guiSize = 1.0f;
    private float panelOpacity = 0.92f;
    private ThemeStorage.Themes capturedTheme;
    private int capturedAccentColor = 0xffff6f8e;
    private int capturedSecondAccentColor = 0xff57d6c4;
    private int editingCustomColor = 0;
    private int toastColor = 0xfff3f4f8;
    private String toastText = "";
    private long toastUntil;

    public GlintweinClickGuiElement() {
        setPositionType(PositionType.ABSOLUTE);
        setSize(ELEMENT_WIDTH, HEIGHT);
        setOverflow(Overflow.HIDDEN);
        setBorderRadius(BorderRadius.of(MENU_RADIUS));
        setupColorPicker(customColorPickerOne);
        setupColorPicker(customColorPickerTwo);
        addChild(customColorPickerOne);
        addChild(customColorPickerTwo);
        setOnMousePress(this::mousePressed);
        setOnMouseRelease((mouseX, mouseY, button) -> {
            draggingSlider = null;
            draggingLooseSlider = null;
            return true;
        });
        setOnMouseScroll((mouseX, mouseY, horizontal, vertical) -> {
            handleScroll(mouseX + borderBox.x, mouseY + borderBox.y, vertical);
            return true;
        });
    }

    private void setupColorPicker(ColorPicker picker) {
        picker.setPositionType(PositionType.ABSOLUTE);
        picker.setDisplay(Display.NONE);
        picker.setOverflow(Overflow.VISIBLE);
    }

    private void updateColorPickerLayout() {
        float pickerLeft = POPOVER_SPACE - 244.0f + 12.0f;
        float pickerTop = 54.0f + 181.0f;
        customColorPickerOne.setPosition(Edge.LEFT, pickerLeft);
        customColorPickerOne.setPosition(Edge.TOP, pickerTop);
        customColorPickerTwo.setPosition(Edge.LEFT, pickerLeft);
        customColorPickerTwo.setPosition(Edge.TOP, pickerTop);
        boolean showPicker = settingsOpen && editingCustomColor > 0;
        customColorPickerOne.setDisplay(showPicker && editingCustomColor == 1 ? Display.FLEX : Display.NONE);
        customColorPickerTwo.setDisplay(showPicker && editingCustomColor == 2 ? Display.FLEX : Display.NONE);
    }

    private void syncCustomColorPicker() {
        if (!settingsOpen || editingCustomColor == 0) {
            return;
        }
        int selected = editingCustomColor == 1 ? customColorPickerOne.getSelectedColor() : customColorPickerTwo.getSelectedColor();
        if (editingCustomColor == 1 && selected != customColorOne) {
            customColorOne = selected;
            applyCustomTheme();
        } else if (editingCustomColor == 2 && selected != customColorTwo) {
            customColorTwo = selected;
            applyCustomTheme();
        }
    }

    public void open() {
        state.refreshModules();
        activeView = View.MODULES;
        settingsModule = null;
        bindingModule = null;
        bindingSetting = null;
        draggingSlider = null;
        draggingLooseSlider = null;
        openModeSetting = null;
        moduleScroll = 0.0f;
        moduleScrollTarget = 0.0f;
        detailScroll = 0.0f;
        detailScrollTarget = 0.0f;
        search = "";
        searchActive = false;
        settingsOpen = false;
        captureThemeColors();
    }

    @Override
    public void tick() {
        setSize(menuWidth() + POPOVER_SPACE, menuHeight());
        setBorderRadius(BorderRadius.of(menuRadius()));
        setPosition(Edge.LEFT, GlobalUIState.getScaledWidth() * 0.5f - menuWidth() * 0.5f - POPOVER_SPACE);
        setPosition(Edge.TOP, GlobalUIState.getScaledHeight() * 0.5f - menuHeight() * 0.5f);
        updateColorPickerLayout();
        moduleScroll += (moduleScrollTarget - moduleScroll) * 0.25f;
        detailScroll += (detailScrollTarget - detailScroll) * 0.25f;
        if (draggingSlider != null) {
            updateSliderValue(draggingSlider, lastMouseX);
        }
        if (draggingLooseSlider != null && draggingLooseSlider.consumer != null) {
            updateLooseSliderValue(draggingLooseSlider, sliderMouseX(draggingLooseSlider));
        }
        super.tick();
        syncCustomColorPicker();
    }

    @Override
    protected void drawContent(Context ctx) {
        hits.clear();
        sliderHits.clear();

        float x = contentBox.x + POPOVER_SPACE;
        float y = contentBox.y;
        MdsfFont font = MdsfFont.defaultFont();
        int accent = accentColor();
        int accentTwo = secondAccentColor();

        drawBackground(ctx, x, y, accent, accentTwo);
        drawSidebar(ctx, font, x, y, accent);
        drawTopbar(ctx, font, x, y, accent);
        if (activeView == View.CONFIGS) {
            drawConfigs(ctx, font, x, y, accent);
        } else {
            drawModuleList(ctx, font, x, y, accent);
            drawModuleDetails(ctx, font, x, y, accent);
        }
        if (settingsOpen) {
            drawSettingsPopover(ctx, font, x, y, accent, accentTwo);
        }
        drawToast(ctx, font);
    }

    private void drawBackground(Context ctx, float x, float y, int accent, int accentTwo) {
        float mw = menuWidth();
        float mh = menuHeight();
        float sw = sidebarWidth();
        float modW = moduleWidth();
        float dw = detailsWidth();
        float th = topbarHeight();
        float radius = menuRadius();
        shadow(ctx, x + 8.0f, y + 12.0f, mw - 16.0f, mh - 16.0f, radius);
        rect(ctx, x - 1.0f, y - 1.0f, mw + 2.0f, mh + 2.0f, radius, LINE_STRONG);
        rect(ctx, x, y, mw, mh, radius, applyOpacity(0xff0f1016));
        rect(ctx, x, y, sw, mh, 0.0f, applyOpacity(0x8a0c0d12));
        rect(ctx, x + sw, y, 1.0f, mh, 0.0f, LINE);
        if (activeView == View.CONFIGS) {
            rect(ctx, x + sw, y, mw - sw, mh, 0.0f, applyOpacity(0x61111219));
        } else {
            rect(ctx, x + sw + modW, y, 1.0f, mh, 0.0f, LINE);
            rect(ctx, x + sw, y, modW, mh, 0.0f, applyOpacity(0x61111219));
            rect(ctx, x + sw + modW, y, dw, mh, 0.0f, applyOpacity(0x700b0c11));
        }
        rect(ctx, x + sw, y + th, mw - sw, 1.0f, 0.0f, LINE);
        rect(ctx, x + sw - 17.0f, y + 27.0f, 7.0f, 7.0f, 3.5f, accentTwo);
    }

    private void drawSidebar(Context ctx, MdsfFont font, float x, float y, int accent) {
        float sw = sidebarWidth();
        float mh = menuHeight();
        text(ctx, font, "Xeron", x + 18.0f, y + 21.0f + TITLE_Y, TITLE_FONT, TEXT);
        float navY = y + 58.0f;
        for (Module.ModuleCategory category : Module.ModuleCategory.values()) {
            boolean active = activeView == View.MODULES && category == state.getActiveCategory();
            drawNavButton(ctx, font, x + 12.0f, navY, sw - 24.0f, category.getName(), countModules(category), active, accent);
            hits.add(new Hit(x + 12.0f, navY, sw - 24.0f, 34.0f, button -> {
                if (button != 0) {
                    return;
                }
                activeView = View.MODULES;
                state.setActiveCategory(category);
                settingsModule = null;
                resetScroll();
                search = "";
                searchActive = false;
            }));
            navY += 40.0f;
        }

        float configY = y + mh - 92.0f;
        drawBottomButton(ctx, font, x + 12.0f, configY, "Configs", activeView == View.CONFIGS, accent);
        hits.add(new Hit(x + 12.0f, configY, sw - 24.0f, 34.0f, button -> {
            if (button != 0) {
                return;
            }
            activeView = View.CONFIGS;
            settingsModule = null;
            bindingModule = null;
            bindingSetting = null;
            search = "";
            searchActive = false;
            resetScroll();
            selectedConfig = firstConfig();
        }));

        float settingsY = y + mh - 52.0f;
        drawBottomButton(ctx, font, x + 12.0f, settingsY, "Settings", settingsOpen, accent);
        hits.add(new Hit(x + 12.0f, settingsY, sw - 24.0f, 34.0f, button -> {
            if (button == 0) {
                settingsOpen = !settingsOpen;
            }
        }));

        rect(ctx, x + 12.0f, y + mh - 12.0f, sw - 24.0f, 1.0f, 0.0f, 0x00ffffff);
    }

    private void drawTopbar(Context ctx, MdsfFont font, float x, float y, int accent) {
        float topX = x + sidebarWidth();
        String title = activeView == View.CONFIGS ? "Configs" : state.getActiveCategory().getName();
        text(ctx, font, title, topX + 18.0f, y + 23.0f + TOPBAR_Y, TOPBAR_FONT, TEXT);

        float searchW = activeView == View.CONFIGS ? 150.0f : 180.0f;
        float actionsW = activeView == View.CONFIGS ? 176.0f : 0.0f;
        float searchX = activeView == View.CONFIGS
            ? x + menuWidth() - searchW - 16.0f - actionsW
            : x + menuWidth() - detailsWidth() - searchW - 16.0f - actionsW;
        float searchY = y + 15.0f;
        drawInput(ctx, font, searchX, searchY, searchW, search.isEmpty() ? (activeView == View.CONFIGS ? "Search configs" : "Search modules") : search, searchActive, accent);
        hits.add(new Hit(searchX, searchY, searchW, 32.0f, button -> {
            if (button == 0) {
                searchActive = true;
                bindingModule = null;
                bindingSetting = null;
            }
        }));

        if (activeView == View.CONFIGS) {
            float createX = searchX + searchW + 8.0f;
            drawTopButton(ctx, font, createX, searchY, 72.0f, "Create", accent);
            hits.add(new Hit(createX, searchY, 72.0f, 32.0f, button -> {
                if (button == 0) {
                    createConfig();
                }
            }));
            float folderX = createX + 80.0f;
            drawTopButton(ctx, font, folderX, searchY, 96.0f, "Open folder", accent);
            hits.add(new Hit(folderX, searchY, 96.0f, 32.0f, button -> {
                if (button == 0) {
                    openConfigFolder();
                }
            }));
        }
    }

    private void drawModuleList(Context ctx, MdsfFont font, float x, float y, int accent) {
        List<Module> modules = filteredModules();
        float pad = padding();
        float gap = gap();
        float rowH = rowHeight();
        float listX = x + sidebarWidth() + pad;
        float listY = y + topbarHeight() + pad;
        float listW = moduleWidth() - pad * 2.0f;
        float listH = menuHeight() - topbarHeight() - pad * 2.0f;
        float totalH = modules.size() * (rowH + gap);
        moduleScrollTarget = MathHelper.clamp(moduleScrollTarget, 0.0f, Math.max(0.0f, totalH - listH + gap));

        if (!ctx.pushScissor(Bounds.fromMinMax(listX, listY, listX + listW, listY + listH))) {
            return;
        }
        if (modules.isEmpty()) {
            drawCentered(ctx, font, "No modules found", listX + listW * 0.5f, listY + listH * 0.5f - 6.0f, 15.0f, MUTED);
        }
        float rowY = listY - moduleScroll;
        for (Module module : modules) {
            if (rowY + rowH >= listY - 8.0f && rowY <= listY + listH + 8.0f) {
                drawModuleRow(ctx, font, module, listX, rowY, listW, accent);
            }
            rowY += rowH + gap;
        }
        ctx.popScissor();
        drawScrollbar(ctx, listX + listW + 5.0f, listY, listH, moduleScroll, Math.max(0.0f, totalH - listH + gap));
    }

    private void drawModuleRow(Context ctx, MdsfFont font, Module module, float x, float y, float w, int accent) {
        boolean enabled = module.isEnable();
        boolean selected = module == settingsModule;
        float enabledProgress = animate(module, enabled ? 1.0f : 0.0f, 0.18f);
        int bg = selected ? 0x3321262f : mixColor(0x0dffffff, colorWithAlpha(accent, 0.18f), enabledProgress);
        int border = selected ? colorWithAlpha(accent, 0.55f) : mixColor(LINE, colorWithAlpha(accent, 0.42f), enabledProgress);
        float rowH = rowHeight();
        outline(ctx, x, y, w, rowH, 7.0f, border);
        rect(ctx, x + 0.5f, y + 0.5f, w - 1.0f, rowH - 1.0f, 7.0f, bg);
        text(ctx, font, module.getDisplayName(), x + 12.0f, y + rowH * 0.5f - MODULE_FONT * 0.35f + MODULE_Y, MODULE_FONT, enabled ? TEXT : 0xcdeeeef4);

        String bind = bindingModule == module ? "Binding..." : module.getKey() == -1 ? "..." : state.toEnglish(KeyBoardUtils.getBindName(module.getKey()));
        float bindW = Math.max(34.0f, Math.min(84.0f, textWidth(font, bind, BIND_FONT) + 16.0f));
        float bindX = x + w - bindW - 42.0f;
        rect(ctx, bindX, y + rowH * 0.5f - 10.0f, bindW, 20.0f, 10.0f, bindingModule == module ? colorWithAlpha(accent, 0.32f) : 0x33000000);
        drawCentered(ctx, font, bind, bindX + bindW * 0.5f, y + rowH * 0.5f - BIND_FONT * 0.2f + BIND_Y, BIND_FONT, MUTED);
        drawToggle(ctx, x + w - 34.0f, y + rowH * 0.5f - 9.0f, enabledProgress, accent);

        hits.add(new Hit(x, y, w, rowH, button -> {
            if (bindingModule != null) {
                return;
            }
            if (button == 0) {
                module.toggle();
            } else if (button == 1) {
                settingsModule = settingsModule == module ? null : module;
                detailScroll = 0.0f;
                detailScrollTarget = 0.0f;
                openModeSetting = null;
            } else if (button == 2) {
                bindingModule = module;
                bindingSetting = null;
                toast("Binding...");
            }
        }));
    }

    private void drawModuleDetails(Context ctx, MdsfFont font, float x, float y, int accent) {
        float sw = sidebarWidth();
        float modW = moduleWidth();
        float dw = detailsWidth();
        float headerH = detailsHeaderHeight();
        float detailsX = x + sw + modW;
        float headY = y;
        rect(ctx, detailsX, headY, dw, headerH, 0.0f, 0x00000000);
        if (settingsModule == null) {
            return;
        }

        text(ctx, font, settingsModule.getDisplayName(), detailsX + 16.0f, y + 20.0f + DETAILS_TITLE_Y, DETAILS_TITLE_FONT, TEXT);
        text(ctx, font, settingsModule.isEnable() ? "ON" : "OFF", detailsX + dw - 42.0f, y + 22.0f + SMALL_Y, SMALL_FONT, settingsModule.isEnable() ? secondAccentColor() : SOFT);
        String description = settingsModule.getDisplayDescription();
        if (description != null && !description.equalsIgnoreCase("NULLABLE")) {
            text(ctx, font, trimToWidth(font, description, dw - 32.0f, DETAILS_DESC_FONT), detailsX + 16.0f, y + 53.0f + DETAILS_DESC_Y, DETAILS_DESC_FONT, MUTED);
        }
        rect(ctx, detailsX, y + headerH, dw, 1.0f, 0.0f, LINE);

        float listX = detailsX + 16.0f;
        float listY = y + headerH + 14.0f;
        float listW = dw - 32.0f;
        float listH = menuHeight() - headerH - 30.0f;
        float totalH = getDetailsHeight(settingsModule, listW);
        detailScrollTarget = MathHelper.clamp(detailScrollTarget, 0.0f, Math.max(0.0f, totalH - listH));

        if (!ctx.pushScissor(Bounds.fromMinMax(listX, listY, listX + listW, listY + listH))) {
            return;
        }
        float sy = listY - detailScroll;
        sy = drawModuleBindCard(ctx, font, settingsModule, listX, sy, listW, accent);
        for (Setting setting : settingsModule.getSettings()) {
            if (setting.visible()) {
                sy = drawDetailSetting(ctx, font, setting, listX, sy, listW, accent);
            }
        }
        ctx.popScissor();
        drawScrollbar(ctx, listX + listW + 5.0f, listY, listH, detailScroll, Math.max(0.0f, totalH - listH));
    }

    private float drawModuleBindCard(Context ctx, MdsfFont font, Module module, float x, float y, float w, int accent) {
        float h = 46.0f;
        drawSettingCard(ctx, x, y, w, h);
        text(ctx, font, "Bind", x + 11.0f, y + 16.0f + SETTING_Y, SETTING_FONT, TEXT);
        String bind = bindingModule == module ? "Binding..." : module.getKey() == -1 ? "None" : state.toEnglish(KeyBoardUtils.getBindName(module.getKey()));
        drawRight(ctx, font, bind, x + w - 11.0f, y + 16.0f + SMALL_Y, SMALL_FONT, MUTED);
        hits.add(new Hit(x, y, w, h, button -> {
            if (button == 0 || button == 2) {
                bindingModule = module;
                bindingSetting = null;
                toast("Binding...");
            }
        }));
        return y + h + 12.0f;
    }

    private float drawDetailSetting(Context ctx, MdsfFont font, Setting setting, float x, float y, float w, int accent) {
        if (setting instanceof BooleanSetting booleanSetting) {
            float h = 46.0f;
            drawSettingCard(ctx, x, y, w, h);
            text(ctx, font, setting.displayName(), x + 11.0f, y + 16.0f + SETTING_Y, SETTING_FONT, TEXT);
            drawSwitch(ctx, x + w - 45.0f, y + 13.0f, booleanSetting.isState() ? 1.0f : 0.0f, accent);
            hits.add(new Hit(x, y, w, h, button -> {
                if (button == 0) {
                    booleanSetting.setState(!booleanSetting.isState());
                }
            }));
            return y + h + 12.0f;
        }
        if (setting instanceof FloatSetting floatSetting) {
            float h = 62.0f;
            drawSettingCard(ctx, x, y, w, h);
            text(ctx, font, setting.displayName(), x + 11.0f, y + 12.0f + SETTING_Y, SETTING_FONT, TEXT);
            drawRight(ctx, font, formatSlider(floatSetting), x + w - 11.0f, y + 12.0f + SMALL_Y, SMALL_FONT, MUTED);
            float sx = x + 11.0f;
            float sw = w - 22.0f;
            float t = (floatSetting.get() - floatSetting.getMin()) / (floatSetting.getMax() - floatSetting.getMin());
            rect(ctx, sx, y + 42.0f, sw, 4.0f, 2.0f, 0x25ffffff);
            rect(ctx, sx, y + 42.0f, sw * t, 4.0f, 2.0f, accent);
            rect(ctx, sx + sw * t - 4.0f, y + 40.0f, 8.0f, 8.0f, 4.0f, 0xffffffff);
            sliderHits.add(new SliderHit(sx, y + 34.0f, sw, 18.0f, floatSetting));
            return y + h + 12.0f;
        }
        if (setting instanceof ModeSetting modeSetting) {
            int columns = 3;
            int rows = Math.max(1, (int) Math.ceil(modeSetting.getMods().size() / (float) columns));
            float h = 42.0f + rows * 32.0f;
            drawSettingCard(ctx, x, y, w, h);
            text(ctx, font, setting.displayName(), x + 11.0f, y + 12.0f + SETTING_Y, SETTING_FONT, TEXT);
            float itemW = (w - 22.0f - (columns - 1) * 6.0f) / columns;
            int index = 0;
            for (String mode : modeSetting.getMods()) {
                int row = index / columns;
                int column = index % columns;
                float itemX = x + 11.0f + column * (itemW + 6.0f);
                float itemY = y + 36.0f + row * 32.0f;
                boolean selected = modeSetting.is(mode);
                rect(ctx, itemX, itemY, itemW, 26.0f, 6.0f, selected ? colorWithAlpha(accent, 0.22f) : 0x09ffffff);
                outline(ctx, itemX, itemY, itemW, 26.0f, 6.0f, selected ? colorWithAlpha(accent, 0.48f) : LINE);
                drawCentered(ctx, font, trimToWidth(font, modeSetting.displayMode(mode), itemW - 8.0f, SMALL_FONT), itemX + itemW * 0.5f, itemY + 8.0f + SMALL_Y, SMALL_FONT, selected ? TEXT : MUTED);
                hits.add(new Hit(itemX, itemY, itemW, 26.0f, button -> {
                    if (button == 0) {
                        modeSetting.set(mode);
                    }
                }));
                index++;
            }
            return y + h + 12.0f;
        }
        if (setting instanceof ListSetting listSetting) {
            float h = 32.0f + listSetting.getSettings().stream().filter(BooleanSetting::visible).count() * 28.0f;
            drawSettingCard(ctx, x, y, w, h);
            text(ctx, font, setting.displayName(), x + 11.0f, y + 10.0f + SETTING_Y, SETTING_FONT, TEXT);
            float ly = y + 34.0f;
            for (BooleanSetting entry : listSetting.getSettings()) {
                if (!entry.visible()) {
                    continue;
                }
                text(ctx, font, entry.displayName(), x + 11.0f, ly + 5.0f + SMALL_Y, SMALL_FONT, MUTED);
                drawDot(ctx, x + w - 21.0f, ly + 9.0f, entry.isState() ? 1.0f : 0.0f, accent);
                float hitY = ly;
                hits.add(new Hit(x, hitY, w, 24.0f, button -> {
                    if (button == 0) {
                        entry.setState(!entry.isState());
                    }
                }));
                ly += 28.0f;
            }
            return y + h + 12.0f;
        }
        if (setting instanceof TextSetting textSetting) {
            float h = 46.0f;
            drawSettingCard(ctx, x, y, w, h);
            text(ctx, font, setting.displayName(), x + 11.0f, y + 16.0f + SETTING_Y, SETTING_FONT, TEXT);
            drawRight(ctx, font, trimToWidth(font, textSetting.get(), 120.0f, SMALL_FONT), x + w - 11.0f, y + 16.0f + SMALL_Y, SMALL_FONT, MUTED);
            return y + h + 12.0f;
        }
        if (setting instanceof BindSetting bindSettingValue) {
            float h = 46.0f;
            drawSettingCard(ctx, x, y, w, h);
            text(ctx, font, setting.displayName(), x + 11.0f, y + 16.0f + SETTING_Y, SETTING_FONT, TEXT);
            String bind = bindingSetting == bindSettingValue ? "Binding..." : state.toEnglish(KeyBoardUtils.getBindName(bindSettingValue.getKey()));
            drawRight(ctx, font, bind, x + w - 11.0f, y + 16.0f + SMALL_Y, SMALL_FONT, MUTED);
            hits.add(new Hit(x, y, w, h, button -> {
                if (button == 0 || button == 2) {
                    bindingSetting = bindSettingValue;
                    bindingModule = null;
                    toast("Binding...");
                }
            }));
            return y + h + 12.0f;
        }
        Color color = setting.color();
        float h = 46.0f;
        drawSettingCard(ctx, x, y, w, h);
        text(ctx, font, setting.displayName(), x + 11.0f, y + 16.0f + SETTING_Y, SETTING_FONT, TEXT);
        rect(ctx, x + w - 31.0f, y + 14.0f, 18.0f, 18.0f, 9.0f, 0xff000000 | color.getRGB());
        return y + h + 12.0f;
    }

    private void drawConfigs(Context ctx, MdsfFont font, float x, float y, int accent) {
        List<ConfigEntry> configs = filteredConfigs();
        float pad = padding();
        float gap = gap();
        float listX = x + sidebarWidth() + pad;
        float listY = y + topbarHeight() + pad;
        float listW = menuWidth() - sidebarWidth() - pad * 2.0f;
        float listH = menuHeight() - topbarHeight() - pad * 2.0f;
        float rowH = 54.0f;
        float totalH = configs.size() * (rowH + gap);
        moduleScrollTarget = MathHelper.clamp(moduleScrollTarget, 0.0f, Math.max(0.0f, totalH - listH + gap));
        if (!ctx.pushScissor(Bounds.fromMinMax(listX, listY, listX + listW, listY + listH))) {
            return;
        }
        if (configs.isEmpty()) {
            drawCentered(ctx, font, "No configs found", listX + listW * 0.5f, listY + listH * 0.5f - 6.0f, 15.0f, MUTED);
        }
        float rowY = listY - moduleScroll;
        for (ConfigEntry config : configs) {
            if (rowY + rowH >= listY - 8.0f && rowY <= listY + listH + 8.0f) {
                drawConfigRow(ctx, font, config, listX, rowY, listW, rowH, accent);
            }
            rowY += rowH + gap;
        }
        ctx.popScissor();
        drawScrollbar(ctx, listX + listW + 5.0f, listY, listH, moduleScroll, Math.max(0.0f, totalH - listH + gap));
    }

    private void drawConfigRow(Context ctx, MdsfFont font, ConfigEntry config, float x, float y, float w, float h, int accent) {
        boolean selected = selectedConfig != null && selectedConfig.name.equals(config.name);
        outline(ctx, x, y, w, h, 7.0f, selected ? colorWithAlpha(accent, 0.55f) : LINE);
        rect(ctx, x + 0.5f, y + 0.5f, w - 1.0f, h - 1.0f, 7.0f, selected ? colorWithAlpha(accent, 0.13f) : 0x0dffffff);
        rect(ctx, x + 12.0f, y + 15.0f, 20.0f, 24.0f, 5.0f, selected ? accent : 0x22ffffff);
        drawResourceIcon(ctx, "nurdildan/icons/gui/copy.png", x + 16.0f, y + 20.0f, 12.0f, selected ? 0xffffffff : MUTED);
        text(ctx, font, config.displayName(), x + 42.0f, y + 12.0f, 13.0f, TEXT);
        text(ctx, font, config.status, x + 42.0f, y + 31.0f, 10.0f, SOFT);

        float deleteW = 54.0f;
        float loadW = 48.0f;
        float saveW = 48.0f;
        float actionY = y + 14.0f;
        float deleteX = x + w - deleteW - 8.0f;
        float loadX = deleteX - loadW - 6.0f;
        float saveX = loadX - saveW - 6.0f;
        drawActionButton(ctx, font, saveX, actionY, saveW, "Save", false, accent);
        drawActionButton(ctx, font, loadX, actionY, loadW, "Load", false, accent);
        drawActionButton(ctx, font, deleteX, actionY, deleteW, "Delete", true, accent);

        hits.add(new Hit(x, y, w, h, button -> {
            if (button == 0) {
                selectedConfig = config;
            }
        }));
        hits.add(new Hit(saveX, actionY, saveW, 25.0f, button -> {
            if (button == 0) {
                saveConfig(config);
            }
        }));
        hits.add(new Hit(loadX, actionY, loadW, 25.0f, button -> {
            if (button == 0) {
                loadConfig(config);
            }
        }));
        hits.add(new Hit(deleteX, actionY, deleteW, 25.0f, button -> {
            if (button == 0) {
                deleteConfig(config);
            }
        }));
    }

    private void drawSettingsPopover(Context ctx, MdsfFont font, float x, float y, int accent, int accentTwo) {
        float px = x - 244.0f;
        float py = y + 54.0f;
        float pw = 226.0f;
        boolean colorEditorOpen = editingCustomColor > 0;
        float ph = colorEditorOpen ? 470.0f : 260.0f;
        ctx.pushDrawPriority(20);
        shadow(ctx, px + 4.0f, py + 6.0f, pw - 8.0f, ph - 8.0f, 8.0f);
        outline(ctx, px, py, pw, ph, 8.0f, LINE);
        rect(ctx, px, py, pw, ph, 8.0f, applyOpacity(0xee101117));
        text(ctx, font, "Settings", px + 12.0f, py + 14.0f, 14.0f, TEXT);
        drawTopButton(ctx, font, px + pw - 38.0f, py + 8.0f, 26.0f, "x", accent);
        hits.add(new Hit(px + pw - 38.0f, py + 8.0f, 26.0f, 26.0f, button -> {
            if (button == 0) {
                settingsOpen = false;
            }
        }));

        text(ctx, font, "Theme", px + 12.0f, py + 50.0f, 12.0f, MUTED);
        drawThemeButtons(ctx, font, px + 12.0f, py + 70.0f, accent);

        text(ctx, font, "Custom theme", px + 12.0f, py + 116.0f, 12.0f, MUTED);
        drawCustomColorPicker(ctx, font, px + 12.0f, py + 137.0f, 98.0f, "Color 1", customColorOne, 1);
        drawCustomColorPicker(ctx, font, px + 116.0f, py + 137.0f, 98.0f, "Color 2", customColorTwo, 2);
        if (colorEditorOpen) {
            text(ctx, font, "Picker", px + 12.0f, py + 181.0f, 11.0f, MUTED);
        }

        float baseY = py + (colorEditorOpen ? 395.0f : 183.0f);
        drawLooseSlider(ctx, font, px + 12.0f, baseY, pw - 24.0f, "GUI size", guiSize * 100.0f, 82.0f, 112.0f, value -> guiSize = value / 100.0f);
        drawLooseSlider(ctx, font, px + 12.0f, baseY + 40.0f, pw - 24.0f, "Opacity", panelOpacity * 100.0f, 74.0f, 98.0f, value -> panelOpacity = value / 100.0f);
        ctx.popDrawPriority(20);
    }

    private void drawThemeButtons(Context ctx, MdsfFont font, float x, float y, int accent) {
        List<ThemeStorage.Themes> themes = (List<ThemeStorage.Themes>) Xeron.INSTANCE.themeStorage.getThemeList();
        float box = 25.0f;
        for (int i = 0; i < Math.min(5, themes.size()); i++) {
            ThemeStorage.Themes theme = themes.get(i + 1 < themes.size() ? i + 1 : i);
            float bx = x + i * 33.0f;
            int color = staticThemeColor(theme, 0);
            rect(ctx, bx, y, box, box, 6.0f, color);
            if (Xeron.INSTANCE.themeStorage.getThemes() == theme) {
                outline(ctx, bx - 2.0f, y - 2.0f, box + 4.0f, box + 4.0f, 7.0f, 0xffffffff);
            }
            hits.add(new Hit(bx, y, box, box, button -> {
                if (button == 0) {
                    Xeron.INSTANCE.themeStorage.setThemes(theme);
                    captureThemeColors();
                    editingCustomColor = 0;
                }
            }));
        }
        float customX = x + 5 * 33.0f;
        gradientRect(ctx, customX, y, box, box, 6.0f, customColorOne, customColorTwo);
        drawCentered(ctx, font, "C", customX + box * 0.5f, y + 7.0f, 11.0f, TEXT);
        if (Xeron.INSTANCE.themeStorage.getThemes() == ThemeStorage.Themes.Custom && !"Rainbow".equals(ThemeStorage.Themes.Custom.getTheme().getName())) {
            outline(ctx, customX - 2.0f, y - 2.0f, box + 4.0f, box + 4.0f, 7.0f, 0xffffffff);
        }
        hits.add(new Hit(customX, y, box, box, button -> {
            if (button == 0) {
                applyCustomTheme();
            }
        }));
    }

    private void drawCustomColorPicker(Context ctx, MdsfFont font, float x, float y, float w, String label, int color, int index) {
        text(ctx, font, label, x, y, 11.0f, MUTED);
        rect(ctx, x, y + 16.0f, w, 30.0f, 6.0f, color);
        outline(ctx, x, y + 16.0f, w, 30.0f, 6.0f, editingCustomColor == index ? 0xffffffff : LINE);
        hits.add(new Hit(x, y + 16.0f, w, 30.0f, button -> {
            if (button == 0) {
                editingCustomColor = index;
                if (index == 1) {
                    customColorPickerOne.updateFromColor(customColorOne);
                } else {
                    customColorPickerTwo.updateFromColor(customColorTwo);
                }
                applyCustomTheme();
            }
        }));
    }

    private void drawSwitchSetting(Context ctx, MdsfFont font, float x, float y, float w, String label, boolean enabled, int accent, Runnable action) {
        drawSettingCard(ctx, x, y, w, 34.0f);
        text(ctx, font, label, x + 9.0f, y + 11.0f, 12.0f, TEXT);
        drawSwitch(ctx, x + w - 45.0f, y + 7.0f, enabled ? 1.0f : 0.0f, accent);
        hits.add(new Hit(x, y, w, 34.0f, button -> {
            if (button == 0) {
                action.run();
            }
        }));
    }

    private void drawLooseSlider(Context ctx, MdsfFont font, float x, float y, float w, String label, float value, float min, float max, FloatConsumer consumer) {
        text(ctx, font, label, x, y, 11.0f, MUTED);
        drawRight(ctx, font, String.format(Locale.US, "%.0f", value), x + w, y, 11.0f, MUTED);
        float t = (value - min) / (max - min);
        rect(ctx, x, y + 18.0f, w, 4.0f, 2.0f, 0x25ffffff);
        rect(ctx, x, y + 18.0f, w * t, 4.0f, 2.0f, accentColor());
        rect(ctx, x + w * t - 4.0f, y + 16.0f, 8.0f, 8.0f, 4.0f, 0xffffffff);
        sliderHits.add(new SliderHit(x, y + 10.0f, w, 20.0f, null, consumer, min, max));
    }

    private boolean mousePressed(float mouseX, float mouseY, int button) {
        float hitMouseX = mouseX + borderBox.x;
        float hitMouseY = mouseY + borderBox.y;
        lastMouseRawX = mouseX;
        lastMouseX = hitMouseX;

        if (bindingModule != null && button >= 0 && button != 1) {
            if (button >= 2) {
                bindingModule.setKey(KeyBoardUtils.createMouseBind(button));
            }
            bindingModule = null;
            return true;
        }
        if (bindingSetting != null && button >= 0 && button != 1) {
            if (button >= 2) {
                bindingSetting.setKey(KeyBoardUtils.createMouseBind(button));
            }
            bindingSetting = null;
            return true;
        }
        if (button == 0) {
            searchActive = false;
        }
        for (SliderHit hit : sliderHits) {
            if (hit.contains(mouseX, hitMouseY) || hit.contains(hitMouseX, hitMouseY)) {
                float sliderX = hit.contains(mouseX, hitMouseY) ? mouseX : hitMouseX;
                if (hit.setting != null) {
                    draggingSlider = hit.setting;
                    updateSliderValue(hit.setting, sliderX);
                } else if (hit.consumer != null) {
                    draggingLooseSlider = hit;
                    updateLooseSliderValue(hit, sliderX);
                }
                return true;
            }
        }
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit hit = hits.get(i);
            if (hit.contains(hitMouseX, hitMouseY)) {
                hit.action.run(button);
                return true;
            }
        }
        openModeSetting = null;
        return true;
    }

    @Override
    protected void handleMouseMoved(float mouseX, float mouseY, boolean canHover) {
        lastMouseRawX = mouseX;
        lastMouseX = mouseX + borderBox.x;
        super.handleMouseMoved(mouseX, mouseY, canHover);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bindingModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule = null;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                bindingModule.setKey(-1);
                bindingModule = null;
            } else {
                bindingModule.setKey(keyCode);
                bindingModule = null;
            }
            return true;
        }
        if (bindingSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                bindingSetting = null;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                bindingSetting.setKey(-1);
                bindingSetting = null;
            } else {
                bindingSetting.setKey(keyCode);
                bindingSetting = null;
            }
            return true;
        }
        if (!searchActive) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            resetScroll();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
            searchActive = false;
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!searchActive || Character.isISOControl(chr) || search.length() >= 32) {
            return false;
        }
        search += chr;
        resetScroll();
        return true;
    }

    private void handleScroll(float mouseX, float mouseY, double vertical) {
        float x = contentBox.x + POPOVER_SPACE;
        float y = contentBox.y;
        float listX = x + sidebarWidth();
        float detailsX = x + sidebarWidth() + moduleWidth();
        if (mouseX >= listX && mouseX <= detailsX && mouseY >= y + topbarHeight() && mouseY <= y + menuHeight()) {
            moduleScrollTarget = Math.max(0.0f, moduleScrollTarget - (float) vertical * 28.0f);
        } else if (mouseX >= detailsX && mouseX <= x + menuWidth() && mouseY >= y + topbarHeight() && mouseY <= y + menuHeight()) {
            detailScrollTarget = Math.max(0.0f, detailScrollTarget - (float) vertical * 28.0f);
        }
    }

    private List<Module> filteredModules() {
        List<Module> source;
        if (search.isBlank()) {
            source = state.getModules(state.getActiveCategory());
        } else {
            source = state.getAllModules();
        }
        if (search.isBlank()) {
            return source;
        }
        String query = search.toLowerCase(Locale.ROOT);
        return source.stream()
            .filter(module -> module.getName().toLowerCase(Locale.ROOT).contains(query)
                || module.getDisplayName().toLowerCase(Locale.ROOT).contains(query)
                || module.getDisplayDescription().toLowerCase(Locale.ROOT).contains(query)
                || module.getCategory().getName().toLowerCase(Locale.ROOT).contains(query))
            .toList();
    }

    private List<ConfigEntry> filteredConfigs() {
        List<ConfigEntry> configs = configs();
        if (search.isBlank()) {
            return configs;
        }
        String query = search.toLowerCase(Locale.ROOT);
        return configs.stream().filter(config -> config.displayName().toLowerCase(Locale.ROOT).contains(query)).toList();
    }

    private List<ConfigEntry> configs() {
        List<ConfigEntry> list = new ArrayList<>();
        File dir = Xeron.INSTANCE.configsDir;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((ignored, name) -> name.toLowerCase(Locale.ROOT).endsWith(".wonder"));
            if (files != null) {
                Arrays.stream(files)
                    .sorted(Comparator.comparing(File::getName))
                    .forEach(file -> {
                        String base = file.getName().substring(0, file.getName().length() - ".wonder".length());
                        String status = base.equals(Xeron.INSTANCE.configStorage.currentConfig) ? "Current config" : "Saved config";
                        list.add(new ConfigEntry(base, status));
                    });
            }
        }
        return list;
    }

    private ConfigEntry firstConfig() {
        List<ConfigEntry> configs = configs();
        return configs.isEmpty() ? null : configs.get(0);
    }

    private void createConfig() {
        String name = nextConfigName();
        ConfigEntry entry = new ConfigEntry(name, "Created just now");
        try {
            Xeron.INSTANCE.configStorage.saveConfig(name);
            selectedConfig = entry;
            toast("Created " + entry.displayName());
        } catch (Exception exception) {
            toast("Create failed");
            exception.printStackTrace();
        }
    }

    private void saveConfig(ConfigEntry config) {
        try {
            Xeron.INSTANCE.configStorage.saveConfig(config.name);
            selectedConfig = new ConfigEntry(config.name, "Saved just now");
            toast("Saved " + config.displayName());
        } catch (Exception exception) {
            toast("Save failed");
            exception.printStackTrace();
        }
    }

    private void loadConfig(ConfigEntry config) {
        try {
            Xeron.INSTANCE.configStorage.loadConfig(config.name);
            selectedConfig = new ConfigEntry(config.name, "Loaded just now");
            toast("Loaded " + config.displayName());
        } catch (Exception exception) {
            toast("Load failed");
            exception.printStackTrace();
        }
    }

    private void deleteConfig(ConfigEntry config) {
        File file = new File(Xeron.INSTANCE.configsDir, config.name + ".wonder");
        if (file.exists() && file.delete()) {
            selectedConfig = firstConfig();
            toast("Deleted " + config.displayName());
        } else {
            toast("Delete failed");
        }
    }

    private void openConfigFolder() {
        try {
            File configsDir = Xeron.INSTANCE.configsDir;
            if (!configsDir.exists()) {
                configsDir.mkdirs();
            }
            new ProcessBuilder("explorer.exe", configsDir.getAbsolutePath()).start();
            toast("Opening folder...");
        } catch (Exception exception) {
            toast("Open folder failed");
            exception.printStackTrace();
        }
    }

    private String nextConfigName() {
        List<String> names = configs().stream().map(config -> config.name).toList();
        int index = 1;
        while (names.contains("NewConfig_" + index)) {
            index++;
        }
        return "NewConfig_" + index;
    }

    private int countModules(Module.ModuleCategory category) {
        return (int) state.getAllModules().stream().filter(module -> module.getCategory() == category).count();
    }

    private float getDetailsHeight(Module module, float w) {
        float h = 58.0f;
        for (Setting setting : module.getSettings()) {
            if (!setting.visible()) {
                continue;
            }
            if (setting instanceof FloatSetting) {
                h += 74.0f;
            } else if (setting instanceof ModeSetting modeSetting) {
                int rows = Math.max(1, (int) Math.ceil(modeSetting.getMods().size() / 3.0f));
                h += 54.0f + rows * 32.0f;
            } else if (setting instanceof ListSetting listSetting) {
                h += 44.0f + listSetting.getSettings().stream().filter(BooleanSetting::visible).count() * 28.0f;
            } else {
                h += 58.0f;
            }
        }
        return h;
    }

    private void updateSliderValue(FloatSetting setting, float mouseX) {
        for (SliderHit hit : sliderHits) {
            if (hit.setting == setting) {
                float value = MathHelper.clamp((sliderMouseX(hit) - hit.x) / hit.w, 0.0f, 1.0f);
                float raw = setting.getMin() + (setting.getMax() - setting.getMin()) * value;
                float inc = setting.getIncrement();
                if (inc > 0.0f) {
                    raw = Math.round(raw / inc) * inc;
                }
                setting.setValue(raw);
                return;
            }
        }
    }

    private void updateLooseSliderValue(SliderHit hit, float mouseX) {
        float amount = MathHelper.clamp((mouseX - hit.x) / hit.w, 0.0f, 1.0f);
        hit.consumer.accept(hit.min + (hit.max - hit.min) * amount);
    }

    private float sliderMouseX(SliderHit hit) {
        return lastMouseRawX >= hit.x && lastMouseRawX <= hit.x + hit.w ? lastMouseRawX : lastMouseX;
    }

    private void applyCustomTheme() {
        ThemeStorage.Themes.Custom.getTheme().setName("Custom");
        ThemeStorage.Themes.Custom.getTheme().setColor(new int[]{customColorOne, customColorTwo});
        Xeron.INSTANCE.themeStorage.setThemes(ThemeStorage.Themes.Custom);
        captureThemeColors();
    }

    private void resetScroll() {
        moduleScroll = 0.0f;
        moduleScrollTarget = 0.0f;
        detailScroll = 0.0f;
        detailScrollTarget = 0.0f;
    }

    private void toast(String text) {
        toastText = text;
        toastUntil = System.currentTimeMillis() + 1800L;
    }

    private int accentColor() {
        if (capturedTheme != Xeron.INSTANCE.themeStorage.getThemes()) {
            captureThemeColors();
        }
        return capturedAccentColor;
    }

    private int secondAccentColor() {
        if (capturedTheme != Xeron.INSTANCE.themeStorage.getThemes()) {
            captureThemeColors();
        }
        return capturedSecondAccentColor;
    }

    private void captureThemeColors() {
        capturedTheme = Xeron.INSTANCE.themeStorage.getThemes();
        capturedAccentColor = staticThemeColor(capturedTheme, 0);
        capturedSecondAccentColor = staticThemeColor(capturedTheme, 1);
        if (capturedSecondAccentColor == capturedAccentColor) {
            capturedSecondAccentColor = mixColor(capturedAccentColor, 0xffffffff, 0.35f);
        }
    }

    private int staticThemeColor(ThemeStorage.Themes theme, int index) {
        Theme themeData = theme.getTheme();
        if ("Rainbow".equals(themeData.getName())) {
            return themeData.getColor(index) | 0xff000000;
        }
        int[] colors = themeData.getColor();
        if (colors != null && colors.length > index) {
            return colors[index] | 0xff000000;
        }
        if (colors != null && colors.length > 0) {
            return colors[0] | 0xff000000;
        }
        return index == 0 ? 0xffff6f8e : 0xff57d6c4;
    }

    private String formatSlider(FloatSetting setting) {
        float value = setting.get();
        float increment = setting.getIncrement();
        if (increment >= 1.0f) {
            return String.valueOf((int) value);
        }
        if (increment >= 0.1f) {
            return String.format(Locale.US, "%.1f", value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private void drawNavButton(Context ctx, MdsfFont font, float x, float y, float w, String label, int count, boolean active, int accent) {
        if (active) {
            gradientRect(ctx, x, y, w, 34.0f, 7.0f, colorWithAlpha(accent, 0.27f), 0x0dffffff);
            outline(ctx, x, y, w, 34.0f, 7.0f, colorWithAlpha(accent, 0.40f));
        }
        drawSidebarIcon(ctx, font, x + 9.0f, y + 7.0f, label, active, accent);
        text(ctx, font, label, x + 38.0f, y + 11.0f + CATEGORY_Y, CATEGORY_FONT, active ? TEXT : MUTED);
        rect(ctx, x + w - 31.0f, y + 8.0f, 20.0f, 18.0f, 9.0f, 0x0dffffff);
        drawCentered(ctx, font, String.valueOf(count), x + w - 21.0f, y + 13.0f, 10.0f, SOFT);
    }

    private void drawBottomButton(Context ctx, MdsfFont font, float x, float y, String label, boolean active, int accent) {
        rect(ctx, x, y, sidebarWidth() - 24.0f, 34.0f, 7.0f, active ? colorWithAlpha(accent, 0.16f) : 0x00ffffff);
        outline(ctx, x, y, sidebarWidth() - 24.0f, 34.0f, 7.0f, active ? LINE : 0x00ffffff);
        drawSidebarIcon(ctx, font, x + 9.0f, y + 7.0f, label, active, accent);
        text(ctx, font, label, x + 38.0f, y + 11.0f + CATEGORY_Y, CATEGORY_FONT, active ? TEXT : MUTED);
    }

    private void drawSidebarIcon(Context ctx, MdsfFont font, float x, float y, String glyph, boolean active, int accent) {
        rect(ctx, x, y, 20.0f, 20.0f, 6.0f, active ? accent : 0x22ffffff);
        String icon = iconPath(glyph);
        if (icon != null) {
            drawResourceIcon(ctx, icon, x + 4.0f, y + 4.0f, 12.0f, active ? 0xffffffff : MUTED);
        } else {
            drawCentered(ctx, font, glyph.substring(0, 1), x + 10.0f, y + 5.0f, 10.0f, active ? 0xffffffff : MUTED);
        }
    }

    private void drawInput(Context ctx, MdsfFont font, float x, float y, float w, String value, boolean active, int accent) {
        rect(ctx, x, y, w, 32.0f, 7.0f, 0x0dffffff);
        outline(ctx, x, y, w, 32.0f, 7.0f, active ? colorWithAlpha(accent, 0.48f) : LINE);
        int color = search.isEmpty() ? SOFT : TEXT;
        text(ctx, font, trimToWidth(font, value, w - 20.0f, 12.0f), x + 10.0f, y + 10.0f, 12.0f, color);
        if (active && ((System.currentTimeMillis() / 500L) & 1L) == 0L) {
            float cursorX = Math.min(x + w - 10.0f, x + 10.0f + textWidth(font, search, 12.0f) + 1.0f);
            rect(ctx, cursorX, y + 9.0f, 1.0f, 15.0f, 0.0f, accent);
        }
    }

    private void drawTopButton(Context ctx, MdsfFont font, float x, float y, float w, String label, int accent) {
        rect(ctx, x, y, w, 32.0f, 7.0f, 0x0dffffff);
        outline(ctx, x, y, w, 32.0f, 7.0f, LINE);
        drawCentered(ctx, font, label, x + w * 0.5f, y + 10.0f, 12.0f, MUTED);
    }

    private void drawActionButton(Context ctx, MdsfFont font, float x, float y, float w, String label, boolean danger, int accent) {
        int color = danger ? 0x2bff6f8e : colorWithAlpha(accent, 0.14f);
        rect(ctx, x, y, w, 25.0f, 6.0f, color);
        outline(ctx, x, y, w, 25.0f, 6.0f, danger ? 0x60ff6f8e : LINE);
        String icon = switch (label) {
            case "Save" -> "nurdildan/icons/gui/check.png";
            case "Load" -> "nurdildan/icons/gui/load.png";
            case "Delete" -> "nurdildan/icons/gui/bin.png";
            default -> null;
        };
        if (icon != null) {
            drawResourceIcon(ctx, icon, x + 8.0f, y + 7.0f, 11.0f, danger ? 0xffff8fa4 : MUTED);
            text(ctx, font, label, x + 23.0f, y + 7.0f, 10.0f, danger ? 0xffff8fa4 : MUTED);
        } else {
            drawCentered(ctx, font, label, x + w * 0.5f, y + 7.0f, 11.0f, MUTED);
        }
    }

    private void drawSettingCard(Context ctx, float x, float y, float w, float h) {
        rect(ctx, x, y, w, h, 7.0f, 0x0dffffff);
        outline(ctx, x, y, w, h, 7.0f, LINE);
    }

    private void drawToggle(Context ctx, float x, float y, float progress, int accent) {
        rect(ctx, x, y, 28.0f, 18.0f, 9.0f, mixColor(0xff565b66, accent, progress));
        rect(ctx, x + 3.0f + 10.0f * progress, y + 3.0f, 12.0f, 12.0f, 6.0f, 0xffffffff);
    }

    private void drawSwitch(Context ctx, float x, float y, float progress, int accent) {
        rect(ctx, x, y, 34.0f, 20.0f, 10.0f, mixColor(0xff565b66, accent, progress));
        rect(ctx, x + 4.0f + 14.0f * progress, y + 4.0f, 12.0f, 12.0f, 6.0f, 0xffffffff);
    }

    private void drawDot(Context ctx, float x, float y, float progress, int accent) {
        rect(ctx, x, y, 10.0f, 10.0f, 5.0f, mixColor(0xff565b66, accent, progress));
    }

    private void drawScrollbar(Context ctx, float x, float y, float h, float scroll, float maxScroll) {
        if (maxScroll <= 1.0f) {
            return;
        }
        float thumbH = Math.max(38.0f, h * h / (h + maxScroll));
        float thumbY = y + (h - thumbH) * (scroll / maxScroll);
        rect(ctx, x, y, 2.0f, h, 1.0f, 0x22000000);
        rect(ctx, x, thumbY, 2.0f, thumbH, 1.0f, 0x66ffffff);
    }

    private void drawToast(Context ctx, MdsfFont font) {
        if (toastText == null || toastText.isEmpty() || System.currentTimeMillis() > toastUntil) {
            return;
        }
        float progress = Math.min(1.0f, (toastUntil - System.currentTimeMillis()) / 250.0f);
        float w = textWidth(font, toastText, 12.0f) + 24.0f;
        float x = contentBox.x + POPOVER_SPACE + menuWidth() * 0.5f - w * 0.5f;
        float y = contentBox.y + menuHeight() + 14.0f;
        rect(ctx, x, y, w, 30.0f, 7.0f, withAlpha(0xee0c0d12, progress));
        outline(ctx, x, y, w, 30.0f, 7.0f, withAlpha(LINE, progress));
        drawCentered(ctx, font, toastText, x + w * 0.5f, y + 10.0f, 12.0f, withAlpha(toastColor, progress));
    }

    private void shadow(Context ctx, float x, float y, float w, float h, float r) {
        for (int i = 0; i < 5; i++) {
            rect(ctx, x - i * 2.0f, y - i * 2.0f, w + i * 4.0f, h + i * 4.0f, r + i * 2.0f, (16 - i * 3) << 24);
        }
    }

    private void rect(Context ctx, float x, float y, float w, float h, float radius, int color) {
        ctx.drawRect(x, y, w, h, BorderRadius.of(radius), color);
    }

    private void gradientRect(Context ctx, float x, float y, float w, float h, float radius, int topColor, int bottomColor) {
        ctx.drawRect(x, y, w, h, BorderRadius.of(radius), Gradient.topToBottom(topColor, bottomColor));
    }

    private void outline(Context ctx, float x, float y, float w, float h, float radius, int color) {
        ctx.drawRect(DrawRectBuilder.fromXYWH(x, y, w, h).radius(radius).outline(color, 1.0f));
    }

    private void text(Context ctx, MdsfFont font, String text, float x, float y, float size, int color) {
        if (text != null && !text.isEmpty()) {
            font.draw(ctx, text, x, y, size, color);
        }
    }

    private void drawCentered(Context ctx, MdsfFont font, String text, float centerX, float y, float size, int color) {
        font.draw(ctx, text, centerX - textWidth(font, text, size) * 0.5f, y, size, color);
    }

    private void drawRight(Context ctx, MdsfFont font, String text, float rightX, float y, float size, int color) {
        font.draw(ctx, text, rightX - textWidth(font, text, size), y, size, color);
    }

    private String trimToWidth(MdsfFont font, String text, float width, float size) {
        if (text == null) {
            return "";
        }
        if (textWidth(font, text, size) <= width) {
            return text;
        }
        String out = text;
        while (!out.isEmpty() && textWidth(font, out + "...", size) > width) {
            out = out.substring(0, out.length() - 1);
        }
        return out + "...";
    }

    private float textWidth(MdsfFont font, String text, float size) {
        return font.getWidth(text, size);
    }

    private void drawResourceIcon(Context ctx, String path, float x, float y, float size, int color) {
        TextureSimple texture = iconTexture(path);
        if (texture != null) {
            ctx.drawTexture(texture.getSprite(), x, y, size, size, BorderRadius.ZERO, color);
        }
    }

    private TextureSimple iconTexture(String path) {
        if (path == null) {
            return null;
        }
        if (iconTextures.containsKey(path)) {
            return iconTextures.get(path);
        }
        try (InputStream stream = MinecraftClient.getInstance().getResourceManager().open(Identifier.of("xeron", path))) {
            GlintImage image = Platform.get().loadImage(stream);
            try {
                TextureSimple texture = new TextureSimple(image);
                iconTextures.put(path, texture);
                return texture;
            } finally {
                image.close();
            }
        } catch (IOException exception) {
            iconTextures.put(path, null);
            return null;
        }
    }

    private String iconPath(String label) {
        return switch (label) {
            case "Combat" -> "nurdildan/icons/world_render/target.png";
            case "Movement" -> "nurdildan/icons/world_render/arrow.png";
            case "Render" -> "nurdildan/icons/world_render/star.png";
            case "Misc" -> "nurdildan/icons/gui/changes.png";
            case "Player" -> "nurdildan/icons/businessman.png";
            case "Configs" -> "nurdildan/icons/gui/load.png";
            case "Settings" -> "nurdildan/icons/gui/changes.png";
            default -> null;
        };
    }

    private float menuWidth() {
        return WIDTH * guiSize;
    }

    private float menuHeight() {
        return HEIGHT * guiSize;
    }

    private float sidebarWidth() {
        return SIDEBAR_W * guiSize;
    }

    private float moduleWidth() {
        return MODULE_W * guiSize;
    }

    private float detailsWidth() {
        return DETAILS_W * guiSize;
    }

    private float topbarHeight() {
        return TOPBAR_H * guiSize;
    }

    private float detailsHeaderHeight() {
        return DETAILS_HEADER_H * guiSize;
    }

    private float padding() {
        return PAD * guiSize;
    }

    private float rowHeight() {
        return ROW_H * guiSize;
    }

    private float gap() {
        return GAP * guiSize;
    }

    private float menuRadius() {
        return MENU_RADIUS * guiSize;
    }

    private float animate(Object key, float target, float speed) {
        float current = animations.getOrDefault(key, target);
        current += (target - current) * speed;
        if (Math.abs(target - current) < 0.001f) {
            current = target;
        }
        animations.put(key, current);
        return current;
    }

    private int applyOpacity(int color) {
        return withAlpha(color, panelOpacity);
    }

    private int colorWithAlpha(int color, float alpha) {
        return (color & 0x00ffffff) | (MathHelper.clamp(Math.round(alpha * 255.0f), 0, 255) << 24);
    }

    private int withAlpha(int color, float alphaMul) {
        int alpha = MathHelper.clamp(Math.round(((color >>> 24) & 0xFF) * alphaMul), 0, 255);
        return (color & 0x00ffffff) | (alpha << 24);
    }

    private int mixColor(int from, int to, float progress) {
        progress = MathHelper.clamp(progress, 0.0f, 1.0f);
        int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * progress);
        int r = Math.round(((from >>> 16) & 0xFF) + (((to >>> 16) & 0xFF) - ((from >>> 16) & 0xFF)) * progress);
        int g = Math.round(((from >>> 8) & 0xFF) + (((to >>> 8) & 0xFF) - ((from >>> 8) & 0xFF)) * progress);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * progress);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private enum View {
        MODULES,
        CONFIGS
    }

    private record ConfigEntry(String name, String status) {
        String displayName() {
            return name + ".cfg";
        }
    }

    private interface ButtonAction {
        void run(int button);
    }

    private interface FloatConsumer {
        void accept(float value);
    }

    private record Hit(float x, float y, float w, float h, ButtonAction action) {
        boolean contains(float mx, float my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    private record SliderHit(float x, float y, float w, float h, FloatSetting setting, FloatConsumer consumer, float min, float max) {
        SliderHit(float x, float y, float w, float h, FloatSetting setting) {
            this(x, y, w, h, setting, null, 0.0f, 1.0f);
        }

        boolean contains(float mx, float my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }
}

