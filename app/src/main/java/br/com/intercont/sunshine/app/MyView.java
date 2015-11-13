package br.com.intercont.sunshine.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom View personalizada feita do zero
 * Created by intercont on 13/11/15.
 */
public class MyView extends View {

    Paint paint = new Paint();

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //obtendo os valores dos tamanhos
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int myHeight = hSpecSize;

        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int myWidth = wSpecSize;

        //height exemplo, aplica-se para width também
//        if(hSpecMode == MeasureSpec.EXACTLY){
//            myHeight = hSpecSize;
//        } else if(hSpecMode == MeasureSpec.AT_MOST){
//            //acao para Wrap Content, limita a altura/largura da View
//        }

        //Para finalizar, especifico os valores finais calculados que a View deve ter
        setMeasuredDimension(myWidth,myHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        canvas.drawCircle(160, 160, 80, paint);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.RED);
        canvas.drawLine(80, 80, 120, 100, paint);

//        canvas.drawRect(80, 80, 80, 80, paint);
//        paint.setStrokeWidth(0);
//        paint.setColor(Color.CYAN);
//        canvas.drawRect(83, 120, 147, 147, paint);
//        paint.setColor(Color.YELLOW);
//        canvas.drawRect(88, 88, 144, 120, paint);
    }

}
