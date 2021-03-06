package pe.com.productosalpha.android.drawing;

import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Shader;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.TypedValue;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by user on 8/7/16.
 */
public class DrawingView extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private float brushSize, lastBrushSize;

    private boolean erase=false;

    private boolean isFilling = false;  //for flood fill

    public DrawingView( Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        setupDrawing();
    }

    private void setupDrawing(){
        //get drawing area setup for interacction
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);

        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;

    }

    public void setBrushSize(float newSize){
        //update size
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }

    public void setErase(boolean isErase){
        //set erase true or false
        erase=isErase;
        if(erase) this.setColor("#FFFFFFFF"); //drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
    }

    public void startNew(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw view
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();

        if (isFilling) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    FloodFill(new Point((int) touchX, (int) touchY));
                    break;

                default:
                    return true;
            }
        } else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawPath.moveTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_UP:
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    break;
                default:
                    return false;
            }
        }
        invalidate();
        return true;
    }

    public void setColor(String newColor){
        //set color
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
        drawPaint.setShader(null);
    }

    //set pattern
    public void setPattern(String newPattern) {
        invalidate();

        int patternID = getResources().getIdentifier(newPattern, "drawable", "pe.com.productosalpha.android.drawing");

        Bitmap patternBMP = BitmapFactory.decodeResource(getResources(), patternID);

        BitmapShader patternBMPshader = new BitmapShader(patternBMP,
                Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        drawPaint.setColor(0xFFFFFFFF);
        drawPaint.setShader(patternBMPshader);
    }



    //fill effect
    public void fillColor() {
        isFilling = true;
    }

    private synchronized void FloodFill(Point startPoint) {

        Queue<Point> queue = new LinkedList<>();
        queue.add(startPoint);

        int targetColor = canvasBitmap.getPixel(startPoint.x, startPoint.y);

        while (queue.size() > 0) {
            Point nextPoint = queue.poll();
            if (canvasBitmap.getPixel(nextPoint.x, nextPoint.y) != targetColor)
                continue;

            Point point = new Point(nextPoint.x + 1, nextPoint.y);

            while ((nextPoint.x > 0) && (canvasBitmap.getPixel(nextPoint.x, nextPoint.y) == targetColor)) {
                canvasBitmap.setPixel(nextPoint.x, nextPoint.y, paintColor);
                if ((nextPoint.y > 0) && (canvasBitmap.getPixel(nextPoint.x, nextPoint.y - 1) == targetColor))
                    queue.add(new Point(nextPoint.x, nextPoint.y - 1));
                if ((nextPoint.y < canvasBitmap.getHeight() - 1)
                        && (canvasBitmap.getPixel(nextPoint.x, nextPoint.y + 1) == targetColor))
                    queue.add(new Point(nextPoint.x, nextPoint.y + 1));
                nextPoint.x--;
            }

            while ((point.x < canvasBitmap.getWidth() - 1)
                    && (canvasBitmap.getPixel(point.x, point.y) == targetColor)) {
                canvasBitmap.setPixel(point.x, point.y, paintColor);

                if ((point.y > 0) && (canvasBitmap.getPixel(point.x, point.y - 1) == targetColor))
                    queue.add(new Point(point.x, point.y - 1));
                if ((point.y < canvasBitmap.getHeight() - 1)
                        && (canvasBitmap.getPixel(point.x, point.y + 1) == targetColor))
                    queue.add(new Point(point.x, point.y + 1));
                point.x++;
            }
        }

        isFilling = false;
    }
}
