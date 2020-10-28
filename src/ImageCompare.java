import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import Util.ImageUtil;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

public class ImageCompare {

    private boolean compareResult = false;
    private String mark = "_compareResult";
    /**
     * 比较两张图片，如不同则将不同处标记并输出到新的图片中
     */
    public double CompareAndMarkDiff(File file1, File file2) {
        BufferedImage img1 = ImageUtil.rgbFile2BufferImage(file1);
        BufferedImage img2 = ImageUtil.rgbFile2BufferImage(file2);
        byte[] pixels1 = ((DataBufferByte) img1.getRaster().getDataBuffer())
                .getData();
        byte[] pixels2 = ((DataBufferByte) img2.getRaster().getDataBuffer())
                .getData();
        Mat mat1 = new Mat(img1.getHeight(), img1.getWidth(), CvType.CV_8SC3);
        Mat mat2 = new Mat(img2.getHeight(), img2.getWidth(), CvType.CV_8SC3);
        mat1.put(0, 0, pixels1);
        mat2.put(0, 0, pixels2);

        if(mat1.cols() == 0 || mat2.cols() == 0 || mat1.rows() == 0 || mat2.rows() == 0)
        {
            System.out.println("图片文件路径异常，获取的图片大小为0，无法读取");
            return 0;
        }
        if(mat1.cols() != mat2.cols() || mat1.rows() != mat2.rows())
        {
            System.out.println("两张图片大小不同，无法比较");
            return 0;
        }
        mat1.convertTo(mat1, CvType.CV_8UC1);
        mat2.convertTo(mat2, CvType.CV_8UC1);
        Mat mat1_gray = new Mat();
        Imgproc.cvtColor(mat1, mat1_gray, Imgproc.COLOR_BGR2GRAY);
        Mat mat2_gray = new Mat();
        Imgproc.cvtColor(mat2, mat2_gray, Imgproc.COLOR_BGR2GRAY);
        mat1_gray.convertTo(mat1_gray, CvType.CV_32F);
        mat2_gray.convertTo(mat2_gray, CvType.CV_32F);
        double result = Imgproc.compareHist(mat1_gray, mat2_gray, Imgproc.CV_COMP_CORREL);
//        if(result == 1)
//        {
//            compareResult = true;//此处结果为1则为完全相同
//            System.out.println(result);
//            return;
//        }
//        System.out.println("相似度数值为:"+result);
        return result;

    }

    private void writeImage(Mat mat, String outPutFile)
    {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            ImageIO.write(bufImage, "png", new File(outPutFile));
        } catch (IOException | HeadlessException e)
        {
            e.printStackTrace();
        }
    }

    private String getFileName(String filePath)
    {
        File f = new File(filePath);
        return f.getName();
    }

    private String getParentDir(String filePath)
    {
        File f = new File(filePath);
        return f.getParent();
    }

    private Mat readMat(String filePath)
    {
        try {
            File file = new File(filePath);
            FileInputStream inputStream = new FileInputStream(filePath);
            byte[] byt = new byte[(int) file.length()];
            int read = inputStream.read(byt);
            List<Byte> bs = convert(byt);
            Mat mat1 = Converters.vector_char_to_Mat(bs);
            return mat1;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Mat();
    }

    private List<Byte> convert(byte[] byt)
    {
        List<Byte> bs = new ArrayList<Byte>();
        for (int i = 0; i < byt.length; i++)
        {
            bs.add(i, byt[i]);
        }
        return bs;
    }
}

