package de.mrjulsen.mcdragonlib;

public final class DLTranslate {
    private DLTranslate() {}

    public static String key(Type type, String path) {
        return type.getName() + "." + DragonLib.MODID + "." + path;
    }

    public static enum Type {
        ITEM("item"),
        BLOCK("block"),
        GUI("gui"),
        COMMON("common");

        private final String name;

        private Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
