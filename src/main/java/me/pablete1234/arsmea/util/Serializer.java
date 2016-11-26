package me.pablete1234.arsmea.util;


import com.google.common.reflect.TypeToken;
import me.pablete1234.arsmea.Arsmea;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class Serializer {

    public static <T> T unserialize(String fileName, TypeToken<?> type, T fallback) {
        File input = new File(Arsmea.instance().getDataFolder().getPath() + File.separator + fileName);
        if (input.exists()) {
            try {
                return Arsmea.getGson().fromJson(new FileReader(input), type.getType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fallback;
    }

    public static void serialize(String fileName, Object obj) {
        File output = new File(Arsmea.instance().getDataFolder().getPath() + File.separator + fileName);
        try {
            output.delete();
            Files.write(output.toPath(), Arsmea.getGson().toJson(obj).getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
