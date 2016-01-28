package org.gamefolk.roomfullofcats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CatsMenuActivity extends Activity {

    private GLSurfaceView glSurfaceView;

    private static final String TAG = "RoomFullOfCats";

    class MenuRenderer implements GLSurfaceView.Renderer {

        private int[] textures = new int[1];
        private Bitmap bitmap;

        private FloatBuffer vertexBuffer;
        private FloatBuffer textureBuffer;

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
            float aspect = (float)width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-aspect, aspect, -1.0f, 1.0f, 1.0f, 10.0f);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslatef(0.0f, 0.0f, -3.0f);

            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

            long time = SystemClock.uptimeMillis() % 10000L;
            float angle = (360.0f / 10000.0f) * ((int) time);
            gl.glRotatef(angle, 0, 1, 0);

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
