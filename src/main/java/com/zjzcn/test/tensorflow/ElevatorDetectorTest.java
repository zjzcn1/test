package com.zjzcn.test.tensorflow;


import com.alibaba.fastjson.JSON;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ElevatorDetectorTest {

    public static void main(String[] args) {
        OpenCV.loadShared();

        HOGDescriptor hog = new HOGDescriptor(new Size(16,16),
                new Size(16,16), new Size(8, 8), new Size(8, 8), 9);

        Size dsize = new Size(32, 32); // 设置新图片的大小
        Mat img = Imgcodecs.imread("/Users/zjz/workspace/test/src/main/resources/2_1.jpg", 1);
        Mat grayMat = new Mat();
        Imgproc.cvtColor(img, grayMat, Imgproc.COLOR_RGB2GRAY);
        Mat resizedImg = new Mat();
        Imgproc.resize(grayMat,resizedImg,dsize, 0, 0, Imgproc.INTER_AREA);
        int bufferSize = resizedImg.channels() * resizedImg.cols() * resizedImg.rows();
        float[] imageBuf = new float[bufferSize];

        for(int i = 0; i < resizedImg.rows(); i++) {
            for(int j = 0; j < resizedImg.cols(); j++) {
                double[] imgByte = resizedImg.get(i, j);
                imageBuf[i*resizedImg.rows()+j] = (float)(imgByte[0]/255);
            }
        }

        String modelDir = "/Users/zjz/workspace/test/src/main/resources";
        byte[] graphDef = null;
        try {
            Path path = Paths.get(modelDir, "elevatorDigitsRecognize32TF-new.pb");
            graphDef = Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Failed to read model file: " + e.getMessage());
            System.exit(1);
        }

        long[] inputShape = new long[]{1L, 32L, 32L, 1L};
        Graph graph = new Graph();
        graph.importGraphDef(graphDef);
        try (Session session = new Session(graph)) {
            Tensor<Float> input = Tensor.create(inputShape, FloatBuffer.wrap(imageBuf));
            Long time = System.currentTimeMillis();
            try(Tensor<Float> output = session
                    .runner()
                    .feed("conv2d_1_input", input)
                    .fetch("output_node0:0")
                    .run()
                    .get(0).expect(Float.class)){

                long[] outputShape = output.shape();
                System.out.println(outputShape.length);

                float[] out = new float[14];
                output.copyTo(out);
                input.close();

                System.out.println(JSON.toJSONString(out));
                int bestLabelIdx = maxIndex(out);
                System.out.println(String.format("Best match: %s (%.2f%%)", bestLabelIdx,
                        out[bestLabelIdx] * 100f));
            }
            System.out.println("Elapsed Time: " + (System.currentTimeMillis() - time));
        }

    }

    private static int maxIndex(float[] probabilities) {
        int best = 0;
        for (int i = 1; i < probabilities.length; ++i) {
            if (probabilities[i] > probabilities[best]) {
                best = i;
            }
        }
        return best;
    }
}