package xinQing.common.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 二维码工具类
 *
 * 参考：`https://my.oschina.net/itblog/blog/501577`
 * Created by null on 2017/1/9.
 */
public class QRCode {

    // 二维码的宽度和高度
    public static int QR_CODE_WIDTH = 200;
    public static int QR_CODE_HEIGHT = 200;

    // 二维码中间的icon宽度和高度
    private static int ICON_WIDTH = QR_CODE_WIDTH / 6;
    private static int ICON_HEIGHT = QR_CODE_HEIGHT / 6;
    private static int HALF_ICON_WIDTH = ICON_WIDTH / 2;
    private static int HALF_ICON_HEIGHT = ICON_WIDTH / 2;
    // Icon四周的边框宽度
    private static int ICON_BORDER = 2;

    // 生成的图片格式
    public static String FORMAT = "png";

    public static int BLACK = 0x000000;// 编码的颜色
    public static int WHITE = 0xFFFFFF;// 空白的颜色


    // 二维码读码器和写码器
    private static final MultiFormatWriter WRITER = new MultiFormatWriter();
    private static final MultiFormatReader READER = new MultiFormatReader();

    public static void createQRCodeToPath(String content, Path desPath) throws IOException, WriterException {
        byte[] bytes = createQRCodeToBytes(content);
        Files.write(desPath, bytes);
    }

    public static void createQRCodeToPath(String content, Path desPath, Path iconPath) throws IOException, WriterException {
        byte[] bytes = createQRCodeToBytes(content, iconPath);
        Files.write(desPath, bytes);
    }

    public static byte[] createQRCodeToBytes(String content) throws WriterException, IOException {
        return createQRCodeToByteStream(content).toByteArray();
    }

    public static byte[] createQRCodeToBytes(String content, Path iconPath) throws WriterException, IOException {
        return createQRCodeToByteStream(content, iconPath).toByteArray();
    }

    public static ByteArrayOutputStream createQRCodeToByteStream(String content) throws WriterException, IOException {
        BufferedImage image = createQRCodeToBufferedImage(content);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, FORMAT, bos);
        return bos;
    }

    public static ByteArrayOutputStream createQRCodeToByteStream(String content, Path iconPath) throws WriterException, IOException {
        BufferedImage image = createQRCodeToBufferedImage(content, iconPath);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, FORMAT, bos);
        return bos;
    }

    public static BufferedImage createQRCodeToBufferedImage(String content) throws WriterException {
        BitMatrix matrix = WRITER.encode(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

    /*
     * 二维码中间带有小图标
     */
    public static BufferedImage createQRCodeToBufferedImage(String content, Path iconPath) throws WriterException {
        if (iconPath == null) {
            return createQRCodeToBufferedImage(content);
        }
        BitMatrix matrix = WRITER.encode(
                content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
        // 读取Icon图像
        BufferedImage scaleImage = null;
        try {
            scaleImage = scaleImage(iconPath, ICON_WIDTH, ICON_HEIGHT, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int[][] iconPixels = new int[ICON_WIDTH][ICON_WIDTH];
        for (int i = 0; i < scaleImage.getWidth(); i++) {
            for (int j = 0; j < scaleImage.getHeight(); j++) {
                iconPixels[i][j] = scaleImage.getRGB(i, j);
            }
        }

        // 二维码的宽和高
        int halfW = matrix.getWidth() / 2;
        int halfH = matrix.getHeight() / 2;

        // 计算图标的边界：
        int minX = halfW - HALF_ICON_WIDTH;//左
        int maxX = halfW + HALF_ICON_WIDTH;//右
        int minY = halfH - HALF_ICON_HEIGHT;//上
        int maxY = halfH + HALF_ICON_HEIGHT;//下

        int[] pixels = new int[QR_CODE_WIDTH * QR_CODE_HEIGHT];

        // 修改二维码的字节信息，替换掉一部分为图标的内容。
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                // 如果点在图标的位置，用图标的内容替换掉二维码的内容
                if (x > minX && x < maxX && y > minY && y < maxY) {
                    int indexX = x - halfW + HALF_ICON_WIDTH;
                    int indexY = y - halfH + HALF_ICON_HEIGHT;
                    pixels[y * QR_CODE_WIDTH + x] = iconPixels[indexX][indexY];
                }
                // 在图片四周形成边框
                else if ((x > minX - ICON_BORDER && x < minX + ICON_BORDER
                        && y > minY - ICON_BORDER && y < maxY + ICON_BORDER)
                        || (x > maxX - ICON_BORDER && x < maxX + ICON_BORDER
                        && y > minY - ICON_BORDER && y < maxY + ICON_BORDER)
                        || (x > minX - ICON_BORDER && x < maxX + ICON_BORDER
                        && y > minY - ICON_BORDER && y < minY + ICON_BORDER)
                        || (x > minX - ICON_BORDER && x < maxX + ICON_BORDER
                        && y > maxY - ICON_BORDER && y < maxY + ICON_BORDER)) {
                    pixels[y * QR_CODE_WIDTH + x] = WHITE;
                } else {
                    // 这里是其他不属于图标的内容。即为二维码没有被图标遮盖的内容，用矩阵的值来显示颜色。
                    pixels[y * QR_CODE_WIDTH + x] = matrix.get(x, y) ? BLACK : WHITE;
                }
            }
        }

        // 用修改后的字节数组创建新的BufferedImage.
        BufferedImage image = new BufferedImage(
                QR_CODE_WIDTH, QR_CODE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        image.getRaster().setDataElements(0, 0, QR_CODE_WIDTH, QR_CODE_HEIGHT, pixels);

        return image;
    }

    /**
     * 把传入的原始图像按高度和宽度进行缩放，生成符合要求的图标
     *
     * @param srcPath   源文件Path
     * @param height    目标高度
     * @param width     目标宽度
     * @param hasFiller 比例不对时是否需要补白：true为补白; false为不补白;
     * @return BufferedImage
     * @throws IOException
     */
    private static BufferedImage scaleImage(Path srcPath, int height, int width,
                                            boolean hasFiller) throws IOException {
        double ratio = 0.0; // 缩放比例
        BufferedImage srcImage = ImageIO.read(srcPath.toFile());
        Image destImage = srcImage.getScaledInstance(
                width, height, BufferedImage.SCALE_SMOOTH);
        // 计算比例
        if ((srcImage.getHeight() > height) || (srcImage.getWidth() > width)) {
            if (srcImage.getHeight() > srcImage.getWidth()) {
                ratio = (new Integer(height)).doubleValue() / srcImage.getHeight();
            } else {
                ratio = (new Integer(width)).doubleValue() / srcImage.getWidth();
            }
            AffineTransformOp op = new AffineTransformOp(
                    AffineTransform.getScaleInstance(ratio, ratio), null);
            destImage = op.filter(srcImage, null);
        }
        // 补白
        if (hasFiller) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphic = image.createGraphics();
            graphic.setColor(Color.white);
            graphic.fillRect(0, 0, width, height);
            if (width == destImage.getWidth(null)) {
                graphic.drawImage(destImage, 0, (height - destImage.getHeight(null)) / 2,
                        destImage.getWidth(null), destImage.getHeight(null), Color.white, null);
            } else {
                graphic.drawImage(destImage, (width - destImage.getWidth(null)) / 2, 0,
                        destImage.getWidth(null), destImage.getHeight(null), Color.white, null);
            }
            graphic.dispose();
            destImage = image;
        }
        return (BufferedImage) destImage;
    }

    public static String parseQRCodeFile(Path qrCodePath) throws NotFoundException, IOException {
        InputStream is = Files.newInputStream(qrCodePath);
        return parseQRCodeInputStream(is);
    }

    public static String parseQRCodeBytes(byte[] qrCodeBytes) throws NotFoundException, IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(qrCodeBytes);
        return parseQRCodeInputStream(bis);
    }

    public static String parseQRCodeInputStream(InputStream qrCodeInputStream) throws NotFoundException, IOException {
        BufferedImage image = ImageIO.read(qrCodeInputStream);
        return parseBufferedImage(image);
    }

    public static String parseBufferedImage(BufferedImage qrCodeImage) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(qrCodeImage);
        Binarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap bitmap = new BinaryBitmap(binarizer);
        return READER.decode(bitmap).getText();
    }

}
