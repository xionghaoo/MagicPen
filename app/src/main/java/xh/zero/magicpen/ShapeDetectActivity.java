package xh.zero.magicpen;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.android.Utils;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShapeDetectActivity extends AppCompatActivity {

    private String TAG = "MainActivity";

    private Button blueBtn;
    private Button greenBtn;
    private Button yellowBtn;
    private Button cyanBtn;
    private Button allBtn;

    private ImageView iv_dst;

    private Bitmap resultBitmap;

    private Mat srcMat, hsvMat;

    private List<MatOfPoint> contours;
    private MatOfPoint2f contours2f, approxCurve;
    private int contoursSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape_detect);
        OpenCVInit();

        Button redBtn = findViewById(R.id.btn_red);
        blueBtn = findViewById(R.id.btn_blue);
        greenBtn = findViewById(R.id.btn_green);
        yellowBtn = findViewById(R.id.btn_yellow);
        cyanBtn = findViewById(R.id.btn_cyan);
        allBtn = findViewById(R.id.btn_all);
        iv_dst = findViewById(R.id.iv_dst);

        try {
            srcMat = Utils.loadResource(this, R.drawable.shape);
        } catch (IOException e) {
            e.printStackTrace();
        }

        redBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tri = 0;
                int rect = 0;
                int circle = 0;
                int star = 0;
                int pentagon = 0;
                contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Mat binaryMat = new Mat();
                Mat resultMat = srcMat.clone();
                hsvMat = new Mat();
                Imgproc.cvtColor(srcMat, hsvMat, Imgproc.COLOR_BGR2HSV);
                Core.inRange(hsvMat, new Scalar(156, 43, 46), new Scalar(180, 255, 255), binaryMat);
                resultBitmap = Bitmap.createBitmap(hsvMat.width(), hsvMat.height(), Bitmap.Config.ARGB_8888);
                Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.drawContours(resultMat, contours, -1, new Scalar(0, 0, 0), 10);
                double epsilon;
                contours2f = new MatOfPoint2f(contours.get(0).toArray());
                epsilon = 0.04 * Imgproc.arcLength(contours2f, true);
                approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(contours2f, approxCurve, epsilon, true);
                if (approxCurve.rows() == 3) {
                    tri++;
                }
                if (approxCurve.rows() == 4) {
                    rect++;
                }
                if (approxCurve.rows() == 5) {
                    pentagon++;
                }
                if (approxCurve.rows() == 10) {
                    star++;
                }
                if (approxCurve.rows() > 5 && approxCurve.rows() != 10) {
                    circle++;
                }

                Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGB2BGR);
                Utils.matToBitmap(resultMat, resultBitmap);
                iv_dst.setImageBitmap(resultBitmap);
                Log.d(TAG, "三角形:" + tri + "\t" + "矩形:" + rect + "\t" + "五边形:" + pentagon + "\t" + "星形:" + star + "\t" + "圆形:" + circle + "\t");
                Core.inRange(hsvMat, new Scalar(100, 43, 46), new Scalar(124, 255, 255), hsvMat);
            }
        });

        greenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tri = 0;
                int rect = 0;
                int circle = 0;
                int star = 0;
                int pentagon = 0;
                contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Mat binaryMat = new Mat();
                Mat resultMat = srcMat.clone();
                hsvMat = new Mat();
                Imgproc.cvtColor(srcMat, hsvMat, Imgproc.COLOR_BGR2HSV);
                Core.inRange(hsvMat, new Scalar(35, 43, 46), new Scalar(77, 255, 255), binaryMat);
                resultBitmap = Bitmap.createBitmap(hsvMat.width(), hsvMat.height(), Bitmap.Config.ARGB_8888);
                Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.drawContours(resultMat, contours, -1, new Scalar(0, 0, 0), 10);
                double epsilon;
                contours2f = new MatOfPoint2f(contours.get(0).toArray());
                epsilon = 0.04 * Imgproc.arcLength(contours2f, true);
                approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(contours2f, approxCurve, epsilon, true);
                if (approxCurve.rows() == 3) {
                    tri++;
                }
                if (approxCurve.rows() == 4) {
                    rect++;
                }
                if (approxCurve.rows() == 5) {
                    pentagon++;
                }
                if (approxCurve.rows() == 10) {
                    star++;
                }
                if (approxCurve.rows() > 5 && approxCurve.rows() != 10) {
                    circle++;
                }

                Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGB2BGR);
                Utils.matToBitmap(resultMat, resultBitmap);
                iv_dst.setImageBitmap(resultBitmap);
                Log.d(TAG, "三角形:" + tri + "\t" + "矩形:" + rect + "\t" + "五边形:" + pentagon + "\t" + "星形:" + star + "\t" + "圆形:" + circle + "\t");
            }
        });

        blueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tri = 0;
                int rect = 0;
                int circle = 0;
                int star = 0;
                int pentagon = 0;
                contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Mat binaryMat = new Mat();
                Mat resultMat = srcMat.clone();
                hsvMat = new Mat();
                Imgproc.cvtColor(srcMat, hsvMat, Imgproc.COLOR_BGR2HSV);
                Core.inRange(hsvMat, new Scalar(100, 43, 46), new Scalar(124, 255, 255), binaryMat);
                resultBitmap = Bitmap.createBitmap(hsvMat.width(), hsvMat.height(), Bitmap.Config.ARGB_8888);
                Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.drawContours(resultMat, contours, -1, new Scalar(0, 0, 0), 10);
                double epsilon;
                contours2f = new MatOfPoint2f(contours.get(0).toArray());
                epsilon = 0.04 * Imgproc.arcLength(contours2f, true);
                approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(contours2f, approxCurve, epsilon, true);
                if (approxCurve.rows() == 3) {
                    tri++;
                }
                if (approxCurve.rows() == 4) {
                    rect++;
                }
                if (approxCurve.rows() == 5) {
                    pentagon++;
                }
                if (approxCurve.rows() == 10) {
                    star++;
                }
                if (approxCurve.rows() > 5 && approxCurve.rows() != 10) {
                    circle++;
                }

                Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGB2BGR);
                Utils.matToBitmap(resultMat, resultBitmap);
                iv_dst.setImageBitmap(resultBitmap);
                Log.d(TAG, "三角形:" + tri + "\t" + "矩形:" + rect + "\t" + "五边形:" + pentagon + "\t" + "星形:" + star + "\t" + "圆形:" + circle + "\t");
            }
        });


        yellowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tri = 0;
                int rect = 0;
                int circle = 0;
                int star = 0;
                int pentagon = 0;
                contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Mat binaryMat = new Mat();
                Mat resultMat = srcMat.clone();
                hsvMat = new Mat();
                Imgproc.cvtColor(srcMat, hsvMat, Imgproc.COLOR_BGR2HSV);
                Core.inRange(hsvMat, new Scalar(26, 43, 46), new Scalar(34, 255, 255), binaryMat);
                resultBitmap = Bitmap.createBitmap(hsvMat.width(), hsvMat.height(), Bitmap.Config.ARGB_8888);
                Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.drawContours(resultMat, contours, -1, new Scalar(0, 0, 0), 10);
                double epsilon;
                contours2f = new MatOfPoint2f(contours.get(0).toArray());
                epsilon = 0.04 * Imgproc.arcLength(contours2f, true);
                approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(contours2f, approxCurve, epsilon, true);
                if (approxCurve.rows() == 3) {
                    tri++;
                }
                if (approxCurve.rows() == 4) {
                    rect++;
                }
                if (approxCurve.rows() == 5) {
                    pentagon++;
                }
                if (approxCurve.rows() == 10) {
                    star++;
                }
                if (approxCurve.rows() > 5 && approxCurve.rows() != 10) {
                    circle++;
                }

                Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGB2BGR);
                Utils.matToBitmap(resultMat, resultBitmap);
                iv_dst.setImageBitmap(resultBitmap);
                Log.d(TAG, "三角形:" + tri + "\t" + "矩形:" + rect + "\t" + "五边形:" + pentagon + "\t" + "星形:" + star + "\t" + "圆形:" + circle + "\t");
            }
        });

        cyanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tri = 0;
                int rect = 0;
                int circle = 0;
                int star = 0;
                int pentagon = 0;
                contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Mat binaryMat = new Mat();
                Mat resultMat = srcMat.clone();
                hsvMat = new Mat();
                Imgproc.cvtColor(srcMat, hsvMat, Imgproc.COLOR_BGR2HSV);
                Core.inRange(hsvMat, new Scalar(78, 43, 46), new Scalar(99, 255, 255), binaryMat);
                resultBitmap = Bitmap.createBitmap(hsvMat.width(), hsvMat.height(), Bitmap.Config.ARGB_8888);
                Imgproc.findContours(binaryMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.drawContours(resultMat, contours, -1, new Scalar(0, 0, 0), 10);
                double epsilon;
                contours2f = new MatOfPoint2f(contours.get(0).toArray());
                epsilon = 0.04 * Imgproc.arcLength(contours2f, true);
                approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(contours2f, approxCurve, epsilon, true);
                if (approxCurve.rows() == 3) {
                    tri++;
                }
                if (approxCurve.rows() == 4) {
                    rect++;
                }
                if (approxCurve.rows() == 5) {
                    pentagon++;
                }
                if (approxCurve.rows() == 10) {
                    star++;
                }
                if (approxCurve.rows() > 5 && approxCurve.rows() != 10) {
                    circle++;
                }

                Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGB2BGR);
                Utils.matToBitmap(resultMat, resultBitmap);
                iv_dst.setImageBitmap(resultBitmap);
                Log.d(TAG, "三角形:" + tri + "\t" + "矩形:" + rect + "\t" + "五边形:" + pentagon + "\t" + "星形:" + star + "\t" + "圆形:" + circle + "\t");
            }
        });

        allBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tri = 0;
                int rect = 0;
                int circle = 0;
                int star = 0;
                int pentagon = 0;
                contours = new ArrayList<>();
                hsvMat = new Mat();
                Mat resultMat = srcMat.clone();
                Mat binaryMat = new Mat();
                Imgproc.cvtColor(srcMat, hsvMat, Imgproc.COLOR_BGR2HSV);
                Core.inRange(hsvMat, new Scalar(0, 0, 221), new Scalar(180, 30, 255), binaryMat);
                Core.bitwise_not(binaryMat, binaryMat);
                resultBitmap = Bitmap.createBitmap(binaryMat.width(), binaryMat.height(), Bitmap.Config.ARGB_8888);

                Mat outMat = new Mat();
                Imgproc.findContours(binaryMat, contours, outMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                contoursSize = contours.size();
                double epsilon;
                Imgproc.drawContours(resultMat, contours, -1, new Scalar(0, 0, 0), 10);
                for (int i = 0; i < contoursSize; i++) {
                    contours2f = new MatOfPoint2f(contours.get(i).toArray());
                    epsilon = 0.04 * Imgproc.arcLength(contours2f, true);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(contours2f, approxCurve, epsilon, true);
                    if (approxCurve.rows() == 3) {
                        tri++;
                    }
                    if (approxCurve.rows() == 4) {
                        rect++;
                    }
                    if (approxCurve.rows() == 5) {
                        pentagon++;
                    }
                    if (approxCurve.rows() == 10) {
                        star++;
                    }
                    if (approxCurve.rows() > 5 && approxCurve.rows() != 10) {
                        circle++;
                    }
                }
                Log.d(TAG, "三角形:" + tri + "\t" + "矩形:" + rect + "\t" + "五边形:" + pentagon + "\t" + "星形:" + star + "\t" + "圆形:" + circle + "\t");
                Imgproc.cvtColor(resultMat, resultMat, Imgproc.COLOR_RGB2BGR);
                Utils.matToBitmap(resultMat, resultBitmap);
                iv_dst.setImageBitmap(resultBitmap);
            }
        });


    }

    private void OpenCVInit() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            Log.d(TAG, "OpenCV Lode success");
        } else {
            Log.d(TAG, "OpenCV Lode failed ");
        }
    }
}