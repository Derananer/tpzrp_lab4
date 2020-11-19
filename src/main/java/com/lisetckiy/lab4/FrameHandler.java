package com.lisetckiy.lab4;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

@Component
@Slf4j
public class FrameHandler {

    private CascadeClassifier faceCascade;

    static{
        String opencvpath = "C:\\Users\\10ila\\Desktop\\tzrp\\tpzrp_lab4\\lib\\";
        System.out.println(opencvpath);
        System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll");
        //nu.pattern.OpenCV.loadShared();
        //System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    @PostConstruct
    private void init() {
        this.faceCascade = new CascadeClassifier("C:\\Users\\10ila\\Desktop\\tzrp\\tpzrp_lab4\\src\\main\\resources\\haarcascade_frontalface_alt2.xml");
        log.info("faceCascade: " + faceCascade.empty());
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public byte[] convertAndConsume(ByteBuffer frame){
        Mat matFrame = Imgcodecs.imdecode(new MatOfByte(frame.array()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        log.info("Channels: " + matFrame.channels() + " height: " + matFrame.rows() + " width: " + matFrame.cols());
        detectAndDisplay(matFrame);
        MatOfByte result = new MatOfByte();
        Imgcodecs.imencode(".jpg", matFrame, result);
        return result.toArray();
    }

    private BufferedImage matToBufferedImage(Mat original)
    {
        // init
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1)
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        }
        else
        {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }


    private void detectAndDisplay(Mat frame)
    {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();
        int absoluteFaceSize = 0;

        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

//         compute minimum face size (20% of the frame height, in our case)
        if (absoluteFaceSize == 0)
        {
            int height = frame.rows();
            if (Math.round(height * 0.2f) > 0)
            {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        log.info("Channels: " + grayFrame.channels() + " height: " + grayFrame.rows() + " width: " + grayFrame.cols());

        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                                          new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);

    }
}
