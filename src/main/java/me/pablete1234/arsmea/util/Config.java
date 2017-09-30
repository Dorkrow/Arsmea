package me.pablete1234.arsmea.util;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    public static boolean headDrop_enabled = true;
    public static boolean headDrop_placeHead = true;

    public static boolean bank_enabled = true;
    public static int bank_initialAmount = 0;
    public static String bank_moneyName = "`bMeya";
    public static String bank_balanceDisplay = "`bBank Balance: `a%money%";
    public static String bank_broadcastChannel = "";

    public static boolean discord_enabled = false;
    public static String discord_token = "";

    public static void reload(FileConfiguration config) {
        try {
            for (Field field : Config.class.getFields()) {
                Class<?> t = field.getType();
                for (Parser parser : Parser.values()) {
                    if (t == parser.clazz) {
                        field.set(null, parser.function.apply(config, toKey(field.getName()), field.get(null)));
                        break;
                    }
                }
                config.set(toKey(field.getName()), field.get(null));
            }
        } catch (IllegalAccessException e) {
        }
    }

    private static String toKey(String string) {
        return string.replace("_", ".");
    }

    private enum Parser {
        STRING(String.class, (conf, key, o) -> conf.getString(key, (String) o)),
        INT(int.class, (conf, key, o) -> conf.getInt(key, (int) o)),
        BOOLEAN(boolean.class, (conf, key, o) -> conf.getBoolean(key, (boolean) o)),
        DOUBLE(double.class, (conf, key, o) -> conf.getDouble(key, (double) o)),
        LONG(long.class, (conf, key, o) -> conf.getLong(key, (long) o)),
        LIST(List.class, (conf, key, o) -> conf.getList(key, (List) o));

        private Class<?> clazz;
        private ConfigFunction<Object> function;

        Parser(Class<?> clazz, ConfigFunction<Object> function) {
            this.clazz = clazz;
            this.function = function;
        }

    }

    interface ConfigFunction<T>  {
        T apply(FileConfiguration conf, String key, T t);
    }

}
