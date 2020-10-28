package Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.Buffer;
import java.util.*;

public class ImageUtil {
    final static int width = 352;
    final static int height = 288;

    public static BufferedImage rgbFile2BufferImage(File file)  {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        try
        {
            int frameLength = width*height*3;
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    img.setRGB(x,y,pix);
                    ind++;
                }
            }
            raf.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return img;
    }

    public static File[] readFiles(String folderPath)    {
        File folder = new File(folderPath);
        File[] files = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().equals(".DS_Store")){
                    return false;
                }else{
                    return true;
                }
            }
        });
        return sortFiles(files);
    }

    public static File[] sortFiles(File[] files)    {
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return files;
    }

    public static ArrayList<ImageIcon> Folder2ImageIconList(String folderPath) {
        ArrayList<ImageIcon> icons = new ArrayList<>();
        File[] files = readFiles(folderPath);
        for (File file: files)  {
            icons.add(new ImageIcon(rgbFile2BufferImage(file)));
        }
        return icons;
    }

    public static void bufferedImage2png(BufferedImage img) throws IOException {
        File outputfile = new File("src/image.png");
        System.out.println("image.png");
        ImageIO.write(img, "png", outputfile);
    }
}
