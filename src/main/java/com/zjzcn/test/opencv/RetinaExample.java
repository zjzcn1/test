package com.zjzcn.test.opencv;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.tools.Slf4jLogger;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.awt.*;

import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.INTER_AREA;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

/**
 * Bioinspired Retina demonstration
 * This retina model allows spatio-temporal image processing
 * As a summary, these are the retina model properties:
 * It applies a spectral whithening (mid-frequency details enhancement)
 * high frequency spatio-temporal noise reduction
 * low frequency luminance to be reduced (luminance range compression)
 * local logarithmic luminance compression allows details to be enhanced in low light conditions
 *
 * Created by mbetzel on 04.09.2016.
 */
public class RetinaExample {

    static {
        System.setProperty("org.bytedeco.javacpp.logger", "slf4jlogger");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    private static final Slf4jLogger logger = (Slf4jLogger) org.bytedeco.javacpp.tools.Logger.create(RetinaExample.class);

    public static void main(String[] args) {
        try {
            logger.info(String.valueOf(logger.isDebugEnabled()));
            logger.info("Start");
            new RetinaExample().execute(args);
            logger.info("Stop");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void execute(String[] args) throws Exception {
        Size dsize = new Size(32, 32); // 设置新图片的大小
        Mat img = imread("/Users/zjz/workspace/test/src/main/resources/test1.jpg", 0);
        Mat output = new Mat();
        resize(img,output,dsize, 0, 0, INTER_AREA);
        System.out.println(output.size().height() + ", " + output.size().width());
        int bufferSize = output.channels() * output.cols() * output.rows();
        byte[] buffer = new byte[bufferSize];

        for(int i = 0; i < 1025; i++) {
            System.out.println(i + ":" + output.data().get(i));
        }
        System.out.println(output.depth());
        showImage(output);
    }

    private void showImage(Mat matrix) {
        CanvasFrame canvasFrame = new CanvasFrame("Retina demonstration", 1);
        canvasFrame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
//        canvasFrame.setCanvasSize(640, 480);
        Canvas canvas = canvasFrame.getCanvas();
        canvasFrame.getContentPane().removeAll();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.add(canvas);
        canvasFrame.add(scrollPane);
        canvasFrame.showImage(new OpenCVFrameConverter.ToMat().convert(matrix));
    }

}