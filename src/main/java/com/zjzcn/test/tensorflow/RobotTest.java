package com.zjzcn.test.tensorflow;


import com.alibaba.fastjson.JSON;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.INTER_AREA;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.resize;


public class RobotTest {
    public static void main(String[] args) {
        Size dsize = new Size(32, 32); // 设置新图片的大小
        Mat img = imread("/Users/zjz/workspace/test/src/main/resources/2_1.jpg", 1);
        Mat grayMat = new Mat();
        cvtColor(img, grayMat, COLOR_RGB2GRAY);
        Mat resizedImg = new Mat();
        resize(grayMat,resizedImg,dsize, 0, 0, INTER_AREA);
        System.out.println(resizedImg.size().height + ", " + resizedImg.size().width);
        int bufferSize = resizedImg.channels() * resizedImg.cols() * resizedImg.rows();
        float[] imageBuf = new float[bufferSize];

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