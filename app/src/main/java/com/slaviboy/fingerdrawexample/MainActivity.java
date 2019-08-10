package com.slaviboy.fingerdrawexample;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.slaviboy.fingerdraw.Canvas;
import com.slaviboy.fingerdraw.Circle;
import com.slaviboy.fingerdraw.Path;

import static com.slaviboy.fingerdrawexample.Base.hideSystemUI;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        canvas = findViewById(R.id.canvas);

        // set custom paths
        canvas.setPaths(new Path[]{
                new Path(Color.RED, 2, 4, 70,
                        100, 25),               // using constructor
                new Path.Builder().withColor(Color.BLUE).build()               // using builder
        });

        // set custom circle
        canvas.setCircles(new Circle[]{
                new Circle.Builder().withStrokeColor(Color.RED).build(),      // using builder
                new Circle(Color.BLUE, Color.WHITE, 1,            // using constructor
                        25, 255, 150)
        });
        canvas.setOnClickListener(this);


        /*
        // how to set OnTouchListener
        canvas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return canvas.onTouch(v, event);
            }
        });*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        //canvas.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        canvas.stop();
    }

    @Override
    public void onClick(View v) {
        v.requestFocus();
        hideSystemUI((Activity) v.getContext());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI(this);
        }
    }
}
