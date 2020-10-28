import DIsplayInterface.VideoPlayer;
import Util.ImageUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.opencv.core.Core;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
//    final static private String rootPath = "/Users/vito/Downloads/test";
    final static private String rootPath = "/Users/vito/Downloads/StudentsUse_Dataset_Armenia";

//    final static private String folderPath = "/Users/vito/Downloads/CSCI576ProjectMedia";
    final static private String synopsisPath = "src/image.png";
    static private ArrayList<String> allSoundPath;
    static private Gson gson = new Gson();
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) throws IOException {
        File root = new File(rootPath);
        File[] videoFolders = ImageUtil.sortFiles(root.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().equals(".DS_Store")){
                    return false;
                }else{
                    return true;
                }
            }
        }));

        File flagFile = new File("src/image.png");
        allSoundPath = new ArrayList<>();
        File soundFolder = new File("/Users/vito/Downloads/audio");
        for (File f: ImageUtil.sortFiles(soundFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().equals(".DS_Store")){
                    return false;
                }else{
                    return true;
                }
            }
        }))
             ) {
            allSoundPath.add(f.getPath());
        }
        if(!flagFile.exists()) {
            ArrayList<ArrayList<Integer>> row1 = new ArrayList<>();
            ArrayList<ArrayList<Integer>> row2 = new ArrayList<>();
            for (int k = 0; k < videoFolders.length;k++) {
                String folderPath = videoFolders[k].getPath();
                System.out.println(videoFolders[k].getPath());
                File[] files = ImageUtil.readFiles(folderPath);
                boolean[] isDeleted = new boolean[files.length]; //True deleted, F: not deleted
                ImageCompare imageCompare = new ImageCompare();
                ArrayList<Integer> frameIndex1 = new ArrayList<>();
                ArrayList<Integer> frameIndex2 = new ArrayList<>();

                if (videoFolders[k].getName().equals("rgb")){
                    System.out.println(folderPath);
                    int count =  0;
//                    FileWriter fileWriter = new FileWriter("out.txt");
                    for (int j = 0; j < files.length-1;++j) {
                        for (int h = j + 1; h < files.length;++h){
                            if (!isDeleted[j] && !isDeleted[h]) {
                                double result = imageCompare.CompareAndMarkDiff(files[j], files[h]);
//                                fileWriter.write(result+"\r\n");
                                if (result >= 0.25) {
                                    isDeleted[h] = true;
                                }
                            }
                        }
                    }
//                    fileWriter.flush();
//                    fileWriter.close();
                    for (int i = 0; i < files.length; ++i)  {
                        if (!isDeleted[i]) {
                            if (count % 2 == 0) {
                                frameIndex1.add(i);
                            } else {
                                frameIndex2.add(i);
                            }
                            count++;
                        }
                    }
                    if(frameIndex1.size() != frameIndex2.size()) {
                        frameIndex2.add(frameIndex1.get(frameIndex1.size()-1));
                    }
                    row1.add(frameIndex1);
                    row2.add(frameIndex2);
                }else {
                    if (!videoFolders[k].isDirectory()) {
                        continue;
                    }
                    FileWriter fileWriter = new FileWriter("out_1.txt");

                    frameIndex1.add(0);
                    int count = 0;
                    for (int i = 0; i < files.length - 1; ++i) {
                        double result = imageCompare.CompareAndMarkDiff(files[i], files[i + 1]);
                        fileWriter.write(result+"\r\n");
                        if (result < 0.6) {
                            frameIndex1.add(i + 1);
                            frameIndex2.add((int) ((i + 1 + frameIndex1.get(count)) / 2));
                            count++;
                        }
                    }
                    fileWriter.flush();
                    fileWriter.close();
                    frameIndex2.add((files.length - 1 + frameIndex1.get(frameIndex1.size() - 1)) / 2);

                    row1.add(frameIndex1);
                    row2.add(frameIndex2);
                }
            }
            String indexString1 = gson.toJson(row1);
            String indexString2 = gson.toJson(row2);
            File indexFile1 = new File("src/indexFile1.json");
            File indexFile2 = new File("src/indexFile2.json");
            indexFile1.createNewFile();
            indexFile2.createNewFile();
            FileWriter writer1 = new FileWriter(indexFile1);
            FileWriter writer2 = new FileWriter(indexFile2);
            BufferedWriter bufferedWriter = new BufferedWriter(writer1);
            bufferedWriter.write(indexString1);
            bufferedWriter.flush();
            bufferedWriter.close();
            bufferedWriter = new BufferedWriter(writer2);
            bufferedWriter.write(indexString2);
            bufferedWriter.flush();
            bufferedWriter.close();


            ArrayList<BufferedImage[]> keyFrameRow1 = new ArrayList<>();
            ArrayList<BufferedImage[]> keyFrameRow2 = new ArrayList<>();
            for(int j = 0; j < row1.size(); ++j) {
                int size = row1.get(j).size();
                BufferedImage[] keyRowArray1 = new BufferedImage[size];
                BufferedImage[] keyRowArray2 = new BufferedImage[size];
                for (int i = 0; i < size; i++) {
                    String path = "";
                    if(videoFolders[j].isDirectory())   {
                        path = videoFolders[j].getPath();
                    } else {
                        continue;
                    }
                    keyRowArray1[i] = ImageUtil.rgbFile2BufferImage(ImageUtil.readFiles(path)[row1.get(j).get(i)]);
                    keyRowArray2[i] = ImageUtil.rgbFile2BufferImage(ImageUtil.readFiles(path)[row2.get(j).get(i)]);
                    System.out.println(row1.get(j).get(i) + ", " + row2.get(j).get(i));
                }
                keyFrameRow1.add(keyRowArray1);
                keyFrameRow2.add(keyRowArray2);
            }
            CreateSynopsisImage createSynopsisImage = new CreateSynopsisImage(keyFrameRow1, keyFrameRow2);
            createSynopsisImage.mergeImages();
        }

        File indexFile1 = new File("src/indexFile1.json");
        File indexFile2 = new File("src/indexFile2.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(indexFile1));
        ArrayList<ArrayList<Integer>> arrayList1 = gson.fromJson(bufferedReader.readLine(), new TypeToken<ArrayList<ArrayList<Integer>>>(){}.getType());
        bufferedReader.close();
        bufferedReader = new BufferedReader(new FileReader(indexFile2));
        ArrayList<ArrayList<Integer>> arrayList2 = gson.fromJson(bufferedReader.readLine(), new TypeToken<ArrayList<ArrayList<Integer>>>(){}.getType());
        bufferedReader.close();

        ArrayList<ArrayList<ImageIcon>> images = new ArrayList<>();
        for(int i = 0; i < videoFolders.length;i++) {
            if(videoFolders[i].isDirectory())   {
                images.add(ImageUtil.Folder2ImageIconList(videoFolders[i].getPath()));
            }
        }
        new VideoPlayer(allSoundPath, synopsisPath, arrayList1, arrayList2, images);
    }
}
