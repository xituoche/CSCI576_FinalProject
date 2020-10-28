import Util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CreateSynopsisImage {

    ArrayList<BufferedImage[]> keyFrameRow1;
    ArrayList<BufferedImage[]> keyFrameRow2;
    BufferedImage synopsis;

    public CreateSynopsisImage(ArrayList<BufferedImage[]> keyFrameRow1, ArrayList<BufferedImage[]> keyFrameRow2) throws IOException {
        this.keyFrameRow1 = keyFrameRow1;
        this.keyFrameRow2 = keyFrameRow2;
    }

    public BufferedImage mergeImages() throws IOException {
        int len = 0;
        for (int i = 0; i < keyFrameRow1.size(); ++i) {
            len += keyFrameRow1.get(i).length;
        }
        int width = (int)((0.5+len)*352*0.25);
        int height = (int)(2*288*0.25);
        int flag = 0;
        int flag2 = 0;
        int sum = keyFrameRow1.get(0).length * 88;
        int sum2 = keyFrameRow2.get(0).length * 88;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for (int x=0;x<width;x++){
            for (int y=0;y<height;y++){
                if (y < height / 2) {
                    if (x-sum >= 0) {
                        flag++;
                        if(flag < keyFrameRow1.size()) {
                            sum += keyFrameRow1.get(flag).length * 88;
                        } else {
                            flag--;
                        }
                    }
                    int trueX = x - sum + keyFrameRow1.get(flag).length * 88;
                    int index = (int) Math.floor(trueX/(352.00/4));
                    if (index < keyFrameRow1.get(flag).length) {
                        trueX %= (352 / 4);
                        int trueY = y;
                        int orginX = trueX * 4;
                        int orginY = trueY * 4;
                        int rgb = keyFrameRow1.get(flag)[index].getRGB(orginX, orginY);
                        img.setRGB(x, y, rgb);
                    }
                } else  {
                    int trueX = x - 352/8;
                    if (trueX-sum2 >= 0) {
                        flag2++;
                        if(flag2 < keyFrameRow1.size()) {
                            sum2 += keyFrameRow1.get(flag2).length * 88;
                        } else {
                            flag2--;
                        }
                    }
                    trueX = trueX - sum2 + keyFrameRow1.get(flag2).length * 88;
                    if(trueX >= 0) {
                        int trueY = y - 288 / 4;
                        int index = (int) Math.floor(trueX / (352.00 / 4));
                        trueX %= (352 / 4);
//                        System.out.println(trueX + ", " + trueY);
                        int orginX = trueX * 4;
                        int orginY = trueY * 4;
                        int rgb = keyFrameRow2.get(flag2)[index].getRGB(orginX, orginY);
                        img.setRGB(x, y, rgb);
                    }
                }
            }
        }

        ImageUtil.bufferedImage2png(img);
        return img;
    }

    public void writeRGBFile()  {

    }
}
