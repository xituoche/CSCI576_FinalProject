package DIsplayInterface;

import Util.ImageUtil;
import Util.PlaySound;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.runtime.Thread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public class VideoPlayer implements MouseListener {
    private JFrame jFrame;
    private JPanel playerPanel;
    private JLabel playerLabel;
    private JButton playButton;
    private int videoIndex = 0;
    private JButton pauseButton;
    private JButton stopButton;
    private JLabel synopsisLabel;

    private String synopsisPath;
    private String soundPath;
//    private Map<int[], Integer> pointer;
    private PlaySound playSound;

    private int playStatus = 0;//1 for play, 2 for pause, 0 for stop
    private Thread playingThread;
    private Thread audioThread;
    private ArrayList<ArrayList<ImageIcon>> allImages;
    private ArrayList<ImageIcon> images;
    private int currentFrameNum = 0;
    private ArrayList<String> allSoundPath;
    private ArrayList<ArrayList<Integer>> frameIndex1;
    private ArrayList<ArrayList<Integer>> frameIndex2;

    public VideoPlayer(ArrayList<String> allSoundPath, String synopsisPath, ArrayList<ArrayList<Integer>> frameIndex1, ArrayList<ArrayList<Integer>> frameIndex2, ArrayList<ArrayList<ImageIcon>> allImages) {
        setSynopsisPath(synopsisPath);
//        setPointer(pointer);
        setAllSoundPath(allSoundPath);
        setAllImages(allImages);
        setFrameIndex1(frameIndex1);
        setFrameIndex2(frameIndex2);
        setImages(0);
        setSoundPath(allSoundPath.get(0));
        init();
//        videoPlay();
    }

    private void init() {
        playSound = new PlaySound(soundPath);

        jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        playerPanel = new JPanel();
        playerLabel = new JLabel();
        setInitBackground(images.get(0));
        //playerLabel.setIcon(new ImageIcon("src/dreamstime_xxl_65780868_small.jpg"));
        playerPanel.add(playerLabel);

        Box buttonBox = Box.createHorizontalBox();
        playButton = new JButton("play");
        pauseButton = new JButton("pause");
        stopButton = new JButton("stop");
        playButton.addMouseListener(this);
        pauseButton.addMouseListener(this);
        stopButton.addMouseListener(this);
        buttonBox.add(playButton);
        buttonBox.add(pauseButton);
        buttonBox.add(stopButton);

        JPanel synopsisPanel = new JPanel();
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setPreferredSize(new Dimension(400, 200));
        synopsisLabel = new JLabel();
        synopsisLabel.addMouseListener(this);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane.setViewportView(synopsisPanel);
        synopsisLabel.setIcon(new ImageIcon(synopsisPath));
        synopsisPanel.add(synopsisLabel);


        Box mainBox = Box.createVerticalBox();
        mainBox.add(playerPanel);
        mainBox.add(buttonBox);
        mainBox.add(jScrollPane);

        jFrame.setContentPane(mainBox);

        jFrame.pack();
        jFrame.setVisible(true);

    }

    public void setSynopsisPath(String synopsisPath) {
        System.out.println(synopsisPath);
        this.synopsisPath = synopsisPath;
    }

//    public void setPointer(Map<int[], Integer> pointer) {
//        this.pointer = pointer;
//    }


    public void setVideoIndex(int videoIndex) {
        this.videoIndex = videoIndex;
        setImages(videoIndex);
        setSoundPath(allSoundPath.get(videoIndex));
        playSound = new PlaySound(soundPath);
    }

    public void setAllSoundPath(ArrayList<String> allSoundPath) {
        this.allSoundPath = allSoundPath;
    }

    public void setAllImages(ArrayList<ArrayList<ImageIcon>> allImages) {
        this.allImages = allImages;
    }

    public void setFrameIndex1(ArrayList<ArrayList<Integer>> frameIndex1) {
        this.frameIndex1 = frameIndex1;
    }

    public void setFrameIndex2(ArrayList<ArrayList<Integer>> frameIndex2) {
        this.frameIndex2 = frameIndex2;
    }

    public void setSoundPath(String soundPath) {
        this.soundPath = soundPath;
    }

    public void setImages(int videoIndex) {
        this.images = allImages.get(videoIndex);
    }

    private void videoPlay()  {
        //TODO 控制视频播放 帧率30, set playerLabel icon
        int totalFrameNum = images.size();
        playingThread = new Thread() {
            public void run() {
//                System.out.println("Start playing video: " + fileName);
                long period = (long)(Double.valueOf(1000)/Double.valueOf(29.97)) * 1000000L;
                long startTime, dt;
                int i = currentFrameNum;
                while (i < totalFrameNum) {
                    startTime = System.nanoTime();
                    playerLabel.setIcon(images.get(i));
                    i++;
                    try {
                        dt = startTime-System.nanoTime();
                        if (dt > period)    {
                            continue;
                        }
                        if ((period - System.nanoTime() + startTime)/1000000-2 >= 0) {
                            sleep((period - System.nanoTime() + startTime) / 1000000 - 2);
                        } else {
                            sleep(0);
                        }
                    } catch (InterruptedException e) {
                        if(playStatus == 0) {
                            currentFrameNum = 0;
                        } else {
                            currentFrameNum = i;
                        }
                        playerLabel.setIcon(images.get(currentFrameNum));
                        currentThread().interrupt();
                        break;
                    }
                    while ((System.nanoTime() - startTime) < period)  {
                    }
                }
                if(playStatus == 1) {
                    playStatus = 0;
                    currentFrameNum = 0;
                    playSound.stop();
                }
//                System.out.println("End playing video: " + fileName);
            }
        };

        audioThread = new Thread() {
            public void run() {
                try {
                    playSound.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        audioThread.start();
        playingThread.start();
    }

    private void pauseVideo() throws InterruptedException {
        if(playingThread != null) {
            playingThread.interrupt();
            audioThread.interrupt();
            playSound.pause();
            playingThread = null;
            audioThread = null;
        }
    }

    private void stopVideo() {
        playSound.stop();
        if(playingThread != null) {
            playingThread.interrupt();
            audioThread.interrupt();
            playingThread = null;
            audioThread = null;
        } else {
            currentFrameNum = 0;
        }
    }

    private void setInitBackground(ImageIcon imageIcon) {
        playerLabel.setIcon(imageIcon);
    }

    private void displayImage(int currentFrameNum)  {
        playerLabel.setIcon(allImages.get(allImages.size()-1).get(currentFrameNum));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == playButton)   {
            if(playStatus != 1) {
                playStatus = 1;
                videoPlay();
            }
        } else if (e.getSource() == pauseButton)  {
            playStatus = 2;
            try {
                pauseVideo();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }

        } else if (e.getSource() == stopButton){
            playStatus = 0;
            stopVideo();
            setInitBackground(images.get(0));
        } else if (e.getSource() == synopsisLabel)  {
            if(playStatus == 1) {
                playStatus = 2;
                try {
                    pauseVideo();
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            int x = e.getX();
            int y = e.getY();
            if (x >= 617 && x <667 & y >= 288/4) {
                x -= 44;
            }
            System.out.println(x + ", " + y);
            int flag = 0;
            int sum = frameIndex1.get(0).size() * 88;
            System.out.println("flag: " + flag);
            while (x - sum >= 0){
                flag++;
                if (flag >= frameIndex1.size()) {
                    flag--;
                    break;
                }
                sum += frameIndex1.get(flag).size() * 88;
            }
            if (flag == frameIndex1.size()-1)   {
                playButton.setEnabled(false);
                stopButton.setEnabled(false);
                pauseButton.setEnabled(false);
                x = x - sum + frameIndex1.get(flag).size() * 88;
                if (y < 288 / 4) {
                    int index = (int) Math.floor(x / (352.00 / 4));
                    currentFrameNum = frameIndex1.get(flag).get(index);
                } else {
                    x = x - 352 / 8;
                    int index = (int) Math.floor(x / (352.00 / 4));
                    currentFrameNum = frameIndex2.get(flag).get(index);
                }

                displayImage(currentFrameNum);
            } else {
                playButton.setEnabled(true);
                stopButton.setEnabled(true);
                pauseButton.setEnabled(true);
                setVideoIndex(flag);
                x = x - sum + frameIndex1.get(videoIndex).size() * 88;
                if (y < 288 / 4) {
                    int index = (int) Math.floor(x / (352.00 / 4));
                    currentFrameNum = frameIndex1.get(videoIndex).get(index);
                } else {
                    x = x - 352 / 8;
                    int index = (int) Math.floor(x / (352.00 / 4));
                    if(index < 0)   {
                        index = 0;
                        setVideoIndex(0);
                    }
                    currentFrameNum = frameIndex2.get(videoIndex).get(index);
                }
                playStatus = 1;
                playSound.setPause(currentFrameNum * 44100 / 30);
                videoPlay();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
