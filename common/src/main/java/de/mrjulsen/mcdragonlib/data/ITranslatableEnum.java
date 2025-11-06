package de.mrjulsen.mcdragonlib.data;

import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;

/**
 * A small extension for enums that allows for easy translation without developing a lot of code.
 * Especially useful for cases where the user has contact to the values of this enum to represent
 * them ​​in human-readable text.
 */
public interface ITranslatableEnum extends StringRepresentable {

    /**
     * A collection of required data for the enum translation keys.
     * @param modid The id of the mod that translates this enum.
     * @param enumName The name key of this enum.
     * @param valueName The name key of this enum value.
     */
    public static record Data(String modid, String enumName, String valueName) {}

    /**
     * General information about the enum to create the translation keys.
     */
    Data getTranslationData();

    @Override
    default String getSerializedName() {
        return getTranslationData().valueName();
    }

    /**
     * @return The translation for the name of the enum.
     * <p>Format: {@code enum.<modid>.<enumname>}</p>
     */
    default MutableComponent getEnumTranslation() {
        Data data = getTranslationData();
        return TextUtils.translate(String.format("enum.%s.%s", data.modid(), data.enumName()));
    }
    
    /**
     * @return The translation for the value name of the enum.
     * <p>Format: {@code enum.<modid>.<enumname>.<valuename>}</p>
     */
    default MutableComponent getValueTranslation() {
        Data data = getTranslationData();
        return TextUtils.translate(String.format("enum.%s.%s.%s", data.modid(), data.enumName(), data.valueName()));
    }
    
    /**
     * @return The translation for the description of the enum.
     * <p>Format: {@code enum.<modid>.<enumname>.description}</p>
     */
    default MutableComponent getEnumDescriptionTranslation() {
        Data data = getTranslationData();
        return TextUtils.translate(String.format("enum.%s.%s.description", data.modid(), data.enumName()));
    }
    
    /**
     * @return The translation for the description of the value of the enum.
     * <p>Format: {@code enum.<modid>.<enumname>.description.<valuename>}</p>
     */
    default MutableComponent getValueDescriptionTranslation() {
        Data data = getTranslationData();
        return TextUtils.translate(String.format("enum.%s.%s.description.%s", data.modid(), data.enumName(), data.valueName()));
    }
}
