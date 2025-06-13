package com.nb2506.booktracker;

import javafx.scene.image.Image;
import java.io.*;

/**
 * Вспомогательный класс для работы с изображениями:
 * чтение из файлов и преобразование массива байт в объект {@link Image}.
 */
public class ImageHelper {

    /**
     * Считывает содержимое файла изображения и возвращает его в виде массива байт.
     *
     * @param file объект файла изображения для чтения
     * @return массив байт, представляющий содержимое файла
     * @throws IOException при ошибках чтения файла
     */
    public static byte[] readImageFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return baos.toByteArray();
        }
    }

    /**
     * Преобразует массив байт изображения в объект {@link Image} JavaFX.
     *
     * @param bytes массив байт изображения, может быть null
     * @return объект {@link Image} или null, если входные данные равны null
     */
    public static Image toImage(byte[] bytes) {
        if (bytes == null) return null;
        return new Image(new ByteArrayInputStream(bytes));
    }
}
