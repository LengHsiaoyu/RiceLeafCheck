package com.riceleaf.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil {

    public static void saveImage(BufferedImage image, String path) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        String ext = path.substring(path.lastIndexOf('.') + 1);
        ImageIO.write(image, ext, file);
    }

    public static void saveThumbnail(BufferedImage original, String dir,
                                      String filename, int maxW, int maxH) throws IOException {
        int w = original.getWidth();
        int h = original.getHeight();
        double ratio = Math.min((double) maxW / w, (double) maxH / h);
        int newW = (int) (w * ratio);
        int newH = (int) (h * ratio);

        BufferedImage thumb = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumb.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newW, newH, null);
        g.dispose();

        File dirFile = new File(dir);
        dirFile.mkdirs();
        ImageIO.write(thumb, "jpg", new File(dir, filename));
    }
}
