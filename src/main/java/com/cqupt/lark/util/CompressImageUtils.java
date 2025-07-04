package com.cqupt.lark.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class CompressImageUtils {


    public static byte[] compressImage(byte[] originalImageBytes, int targetWidth, int targetHeight) throws Exception {

        // 1. 将二进制数据转换为BufferedImage
        ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImageBytes);
        BufferedImage originalImage = ImageIO.read(inputStream);
        inputStream.close();

        // 2. 创建目标尺寸的缩放图像
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();

        // 设置高质量缩放（双三次插值）
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        // 3. 将缩放后的图像编码为JPEG（避免二次质量压缩）
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(ios);

            // 配置编码参数（禁用质量压缩）
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(1.0f); // 1.0 = 最高质量（无额外压缩）

            writer.write(null, new IIOImage(scaledImage, null, null), params);
            writer.dispose();
        }
        return outputStream.toByteArray();
    }


}
