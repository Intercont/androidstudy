package br.com.intercont.sunshine.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

/**
 * Testes com Custom View personalizada feita do zero
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
        canvas.drawCircle(160, 160, 120, paint);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.RED);

        //definindo para onde vai apontar a agulha da bussola
        if(DetailActivity.windDirection != null) {
            if (DetailActivity.windDirection.equals("N")) {
                canvas.drawLine(160, 160, 160, 60, paint);
            } else if (DetailActivity.windDirection.equals("NE")) {
                canvas.drawLine(160, 160, 245, 95, paint);
            } else if (DetailActivity.windDirection.equals("E")) {
                canvas.drawLine(160, 160, 260, 160, paint);
            } else if (DetailActivity.windDirection.equals("SE")) {
                canvas.drawLine(160, 160, 240, 230, paint);
            } else if (DetailActivity.windDirection.equals("S")) {
                canvas.drawLine(160, 160, 160, 260, paint);
            } else if (DetailActivity.windDirection.equals("SW")) {
                canvas.drawLine(160, 160, 105, 240, paint);
            } else if (DetailActivity.windDirection.equals("W")) {
                canvas.drawLine(160, 160, 60, 160, paint);
            } else if (DetailActivity.windDirection.equals("NW")) {
                canvas.drawLine(160, 160, 70, 100, paint);
            }
            Log.d("MY_VIEW", DetailActivity.windDirection);
        }

        //Indicador das coordenadas na bussola
        paint.setTypeface(Typeface.SANS_SERIF);
        paint.setTextSize(30);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawText("N", 0, 1, 150, 72, paint);
        canvas.drawText("S", 0, 1, 150, 270, paint);
        canvas.drawText("E", 0, 1, 250, 170, paint);
        canvas.drawText("W", 0, 1, 48, 170, paint);

        paint.setTextSize(18);
        canvas.drawText("NW", 0, 2, 70, 102, paint);
        canvas.drawText("SW", 0, 2, 70, 232, paint);
        canvas.drawText("NE", 0, 2, 225, 102, paint);
        canvas.drawText("SE",0,2,225,232,paint);

        //Verifica se o usuario esta utilizando algum recurso de acessibilidade
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().
                getSystemService(getContext().ACCESSIBILITY_SERVICE);

        if(accessibilityManager.isEnabled()){
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        }

    }

    /**
     * dispatch Populate Accessibility Event, sempre que o conteúdo da View for alterado
     * Utilizado para notificar o recurso de acessibilidade de uma alteração na View
     * quando o mesmo se encontra habilitado
     */
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.getText().add(DetailActivity.windDirection);
        return true;
    }

}
