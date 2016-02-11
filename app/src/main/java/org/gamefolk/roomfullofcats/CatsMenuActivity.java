package org.gamefolk.roomfullofcats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arcadeoftheabsurd.j_utils.Delegate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CatsMenuActivity extends Activity implements SensorEventListener {

    private static final String TAG = "RoomFullOfCats";

    private static final int SENSOR_TYPE = Sensor.TYPE_ROTATION_VECTOR;
    private static final int INIT_DELAY = 5;  // reject the first 5 rotation sensor readings

    private GLSurfaceView glSurfaceView;

    private SensorManager sensorManager;
    private Sensor rotationSensor;

    private Delegate sensorDelegate;
    private int delay;
    private boolean hasSensor = false;

    private float[] rotDeviceCurr = new float[9];
    private float[] rotDeviceInit = new float[9];

    private float[] orientation = new float[3];

    private TextView orientationView;

    class MenuRenderer implements GLSurfaceView.Renderer {

        private int[] textures = new int[1];
        private Bitmap bitmap;

        private FloatBuffer vertexBuffer;
        private FloatBuffer textureBuffer;

        private float aspect;

        float vertices[] = {
                -1.0f, -1.0f,  0.0f,        // V1 - bottom left
                -1.0f,  1.0f,  0.0f,        // V2 - top left
                 1.0f, -1.0f,  0.0f,        // V3 - bottom right
                 1.0f,  1.0f,  0.0f         // V4 - top right
        };

        private float texture[] = {
                0.0f, 1.0f,     // top left     (V2)
                0.0f, 0.0f,     // bottom left  (V1)
                1.0f, 1.0f,     // top right    (V4)
                1.0f, 0.0f      // bottom right (V3)
        };

        public MenuRenderer(Bitmap bitmap) {

            this.bitmap = bitmap;

            // allocate and fill vertex buffer

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            vertexBuffer = byteBuffer.asFloatBuffer();
            vertexBuffer.put(vertices);
            vertexBuffer.position(0);

            // allocate and fill texture buffer

            byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            textureBuffer = byteBuffer.asFloatBuffer();
            textureBuffer.put(texture);
            textureBuffer.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            gl.glGenTextures(1, textures, 0);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
            //bitmap.recycle();

            gl.glEnable(GL10.GL_TEXTURE_2D);
            gl.glEnable(GL10.GL_DEPTH_TEST);

            gl.glDepthFunc(GL10.GL_LEQUAL);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
            gl.glShadeModel(GL10.GL_SMOOTH);

            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glClearDepthf(1.0f);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
            aspect = (float)width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-aspect, aspect, -1.0f, 1.0f, 1.0f, 10.0f);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();

            gl.glTranslatef(0.0f, 0.0f, -2.0f);
            gl.glScalef(aspect, 1.0f, 1.0f);

            gl.glRotatef((float)Math.toDegrees(orientation[1]), 1.0f, 0.0f, 0.0f);
            gl.glRotatef((float)Math.toDegrees(orientation[2]), 0.0f, 1.0f, 0.0f);
            gl.glRotatef((float)Math.toDegrees(orientation[0]), 0.0f, 0.0f, 1.0f);

            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cats_menu);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if((rotationSensor = sensorManager.getDefaultSensor(SENSOR_TYPE)) == null) {
            Log.e(TAG, "rotation vector sensor unavailable");
        } else {
            sensorDelegate = processOrientationInit;
            hasSensor = true;
            orientationView = (TextView)findViewById(R.id.textView);
        }

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setRenderer(
                new MenuRenderer(BitmapFactory.decodeResource(getResources(), R.drawable.menu)));

        ((ViewGroup)findViewById(R.id.cats_menu_view)).addView(glSurfaceView, 0);

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CatsMenuActivity.this, CatsGameActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();

        if (hasSensor) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    private Delegate processOrientationInit = new Delegate() {
        @Override
        public void function(Object... args) {
            if (delay >= INIT_DELAY) {
                rotDeviceInit = rotDeviceCurr.clone();
                sensorDelegate = processOrientation;
            } else {
                delay++;
            }
        }
    };

    private Delegate processOrientation = new Delegate() {
        @Override
        public void function(Object... args) {
            SensorManager.getAngleChange(orientation, rotDeviceCurr, rotDeviceInit);

            DecimalFormat df = new DecimalFormat("#.#####");

            orientationView.setText(
                    "Yaw: " + df.format(orientation[0]) +
                    "\nPitch: " + df.format(orientation[1]) +
                    "\nRoll: " + df.format(orientation[2]));
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == SENSOR_TYPE) {

            float[] rotWorld = new float[9];

            SensorManager.getRotationMatrixFromVector(rotWorld, event.values.clone());
            SensorManager.remapCoordinateSystem(
                    rotWorld, SensorManager.AXIS_X, SensorManager.AXIS_Y, rotDeviceCurr);

            sensorDelegate.function();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
