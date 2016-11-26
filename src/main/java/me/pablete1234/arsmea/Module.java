package me.pablete1234.arsmea;

import com.google.common.reflect.TypeToken;
import me.pablete1234.arsmea.util.Serializer;

public interface Module {

    default void load() {
    }

    default void unload() {
    }

    default <T> T unserialize(String fileName, TypeToken<?> type, T fallback) {
        return Serializer.unserialize(this.getClass().getSimpleName() + "_" + fileName, type, fallback);
    }

    default void serialize(String fileName, Object obj) {
        Serializer.serialize(this.getClass().getSimpleName() + "_" + fileName, obj);
    }

}
