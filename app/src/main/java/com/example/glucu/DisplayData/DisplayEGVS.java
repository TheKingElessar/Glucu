package com.example.glucu.DisplayData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.glucu.Database.EGVS;
import com.example.glucu.Database.InsertEGVS;
import com.example.glucu.Database.RetrieveEGVS;
import com.example.glucu.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.common.collect.Lists;
import com.scichart.charting.ClipMode;
import com.scichart.charting.Direction2D;
import com.scichart.charting.model.dataSeries.IXyDataSeries;
import com.scichart.charting.modifiers.RolloverModifier;
import com.scichart.charting.modifiers.SourceMode;
import com.scichart.charting.modifiers.ZoomPanModifier;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.annotations.HorizontalLineAnnotation;
import com.scichart.charting.visuals.annotations.IAnnotation;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.pointmarkers.EllipsePointMarker;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.charting.visuals.renderableSeries.XyScatterRenderableSeries;
import com.scichart.charting.visuals.renderableSeries.data.XyRenderPassData;
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IPointMarkerPaletteProvider;
import com.scichart.charting.visuals.renderableSeries.paletteProviders.PaletteProviderBase;
import com.scichart.core.model.DoubleValues;
import com.scichart.core.model.IntegerValues;
import com.scichart.data.model.DateRange;
import com.scichart.data.model.DoubleRange;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DisplayEGVS extends AppCompatActivity {
    static SciChartSurface surface;
    static SciChartBuilder sciChartBuilder;
    static RolloverModifier rolloverModifier;
    static ZoomPanModifier zoomPanModifier;

    Button talkbutton;
    TextView textview;
    protected Handler myHandler;
    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_egvs);

        try {
            createChart(R.id.chart, "Time", "Glucose Value (Mg/dL)");
        } catch (InterruptedException e) {
            // Show an error onscreen
            e.printStackTrace();
        }

        talkbutton = findViewById(R.id.talkButton);
        textview = findViewById(R.id.textView);

        myHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                messageText(stuff.getString("messageText"));
                return true;
            }
        });


        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);

    }

    public void messageText(String newinfo) {
        if (newinfo.compareTo("") != 0) {
            textview.append("\n" + newinfo);
        }
    }


    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String messageFromWatch = intent.getStringExtra("message");
            String message = "I just received a message from the wearable: " + messageFromWatch;
            String inBetween = ".........";
            String alsoMessage = "Sending message.... ";
            String fullDisplay = message + inBetween + alsoMessage;
            textview.setText(fullDisplay);
            System.out.println("Displayed the text...");
            try {
                RetrieveEGVS retrieveEGVS = new RetrieveEGVS(getApplicationContext(), InsertEGVS.db);
                retrieveEGVS.start();

                retrieveEGVS.join();
                List<EGVS> egvsList = retrieveEGVS.egvsList;
                List<EGVS> reversedList = Lists.reverse(egvsList);
                Collections.sort(reversedList);
                Date latestEGVSDate = (reversedList.get(reversedList.size() - 1).systemTimeEGVS);
                double latestEGVSValue = (reversedList.get(reversedList.size() - 1).valueEGVS);
                String stringLatestEGVSTrend = (reversedList.get(reversedList.size() - 1).trendEGVS);

                NumberFormat nf = DecimalFormat.getInstance();
                nf.setMaximumFractionDigits(0);
                String stringLatestEGVSValue = nf.format(latestEGVSValue);
                String stringLatestEGVSDate = Long.toString(latestEGVSDate.getTime());

                message = stringLatestEGVSDate + "/" + stringLatestEGVSValue + "/" + stringLatestEGVSTrend;
            } catch(InterruptedException e) {
                e.printStackTrace();
                System.out.println("Failed to retreive EGVS values");
            }

            System.out.println("Message phone is about to send: " + message);
            new DisplayEGVS.NewThread("/my_path", message).start();

        }
    }


    public void talkClick(View v) {
        String message = "Sending message.... ";

        try {
            RetrieveEGVS retrieveEGVS = new RetrieveEGVS(getApplicationContext(), InsertEGVS.db);
            retrieveEGVS.start();

            retrieveEGVS.join();
            List<EGVS> egvsList = retrieveEGVS.egvsList;
            List<EGVS> reversedList = Lists.reverse(egvsList);
            Collections.sort(reversedList);
            Date latestEGVSDate = (reversedList.get(reversedList.size() - 1).systemTimeEGVS);
            double latestEGVSValue = (reversedList.get(reversedList.size() - 1).valueEGVS);
            String stringLatestEGVSTrend = (reversedList.get(reversedList.size() - 1).trendEGVS);

            NumberFormat nf = DecimalFormat.getInstance();
            nf.setMaximumFractionDigits(0);
            String stringLatestEGVSValue = nf.format(latestEGVSValue);
            String stringLatestEGVSDate = Long.toString(latestEGVSDate.getTime());

            message = stringLatestEGVSDate + "/" + stringLatestEGVSValue + "/" + stringLatestEGVSTrend;
        } catch(InterruptedException e) {
            e.printStackTrace();
            System.out.println("Failed to retreive EGVS values");
        }

        textview.setText(message);
        new DisplayEGVS.NewThread("/my_path", message).start();

    }


    public void sendmessage(String messageText) {
        Bundle bundle = new Bundle();
        bundle.putString("messageText", messageText);
        Message msg = myHandler.obtainMessage();
        msg.setData(bundle);
        myHandler.sendMessage(msg);

    }


    class NewThread extends Thread {
        String path;
        String message;

        NewThread(String p, String m) {
            path = p;
            message = m;
        }


        public void run() {

            Task<List<Node>> wearableList =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {

                List<Node> nodes = Tasks.await(wearableList);
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(DisplayEGVS.this).sendMessage(node.getId(), path, message.getBytes());

                    try {

                        Integer result = Tasks.await(sendMessageTask);
                        sendmessage("I just sent the wearable a message " + sentMessageNumber++);

                    } catch (ExecutionException exception) {

                        //TO DO: Handle the exception//


                    } catch (InterruptedException exception) {

                    }

                }

            } catch (ExecutionException exception) {

                //TO DO: Handle the exception//

            } catch (InterruptedException exception) {

                //TO DO: Handle the exception//
            }

        }
    }


    public void createChart(int idOfChart, String xAxisTitle, String yAxisTitle) throws InterruptedException {
        // Create a SciChartSurface
        surface = findViewById(idOfChart);
        SciChartSurface surface = DisplayEGVS.surface;

        // Initialize the SciChartBuilder
        SciChartBuilder.init(this);

        // Obtain the SciChartBuilder instance
        sciChartBuilder = SciChartBuilder.instance();


        // Test create new X axis
        // Initialises an XyDataSeries with type TX=Date and TY=Double
        final IXyDataSeries<Date, Double> dataSeries = sciChartBuilder.newXyDataSeries(Date.class, Double.class).build();

        final IAxis xBottomAxis = sciChartBuilder.newDateAxis()
                .build();
        xBottomAxis.setAutoRange(AutoRange.Never);
        xBottomAxis.setTextFormatting("MM.dd h:mm a");
        xBottomAxis.setCursorTextFormatting("MM.dd.yyyy h:mm a");

        // Declares the DateAxis
        final IAxis yRightAxis = sciChartBuilder.newNumericAxis()
                .withGrowBy(new DoubleRange(0.1d, 0.1d))
                .withVisibleRangeLimit(new DoubleRange((double) 40, (double) 400))
                .withVisibleRange(new DoubleRange((double) 40, (double) 400))
                .build();

        yRightAxis.setVisibleRange(new DoubleRange((double) 40, (double) 400));
        yRightAxis.setVisibleRangeLimit(new DoubleRange((double) 40, (double) 400));
        yRightAxis.setAxisTitle(yAxisTitle);
        yRightAxis.setAutoRange(AutoRange.Never);

        // Create an Ellipse PointMarker
        final EllipsePointMarker pointMarker = sciChartBuilder.newPointMarker(new EllipsePointMarker())
                .withSize(8, 8)
                .withStroke(ColorUtil.Grey, 2)
                .withFill(ColorUtil.White)
                .build();

        HorizontalLineAnnotation upperLimit = sciChartBuilder.newHorizontalLineAnnotation().withIsEditable(false).withStroke(1, ColorUtil.Yellow).withY1((double) 180).build();
        HorizontalLineAnnotation lowerLimit = sciChartBuilder.newHorizontalLineAnnotation().withIsEditable(false).withStroke(1, ColorUtil.Red).withY1((double) 75).build();

        // Create and configure a Scatter Series
        final IRenderableSeries rs1 = sciChartBuilder.newScatterSeries()
                .withDataSeries(dataSeries)
                .withPointMarker(pointMarker)
                .withPaletteProvider(new HorizontalThresholdPaletteProvider(upperLimit, lowerLimit))
                .build();

        RetrieveEGVS retrieveEGVS = new RetrieveEGVS(this, InsertEGVS.db);
        retrieveEGVS.start();
        retrieveEGVS.join();
        List<EGVS> egvsList = retrieveEGVS.egvsList;
        List<EGVS> reversedList = Lists.reverse(egvsList);
        Collections.sort(reversedList);
        Date latestEGVSDate = (reversedList.get(reversedList.size() - 1).systemTimeEGVS);
        double latestEGVSValue = (reversedList.get(reversedList.size() - 1).valueEGVS);
        String latestEGVSTrend = (reversedList.get(reversedList.size() - 1).trendEGVS);

        TextView latestEGVSTextView = (TextView)findViewById(R.id.LatestEGVS);
        ImageView imageView = (ImageView)findViewById(R.id.imageView);

        NumberFormat nf = DecimalFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        String stringLatestEGVSValue = nf.format(latestEGVSValue);
        latestEGVSTextView.setText(stringLatestEGVSValue);

        if(Integer.parseInt(stringLatestEGVSValue) >= 180) {
            latestEGVSTextView.setTextColor(Color.RED);
            imageView.setImageResource(R.drawable.yellow_trans);
            System.out.println("TextView set to yellow");
        } else if(Integer.parseInt(stringLatestEGVSValue) <= 75) {
            latestEGVSTextView.setTextColor(Color.YELLOW);
            imageView.setImageResource(R.drawable.red_trans);
            System.out.println("TextView set to red");
        } else {
            latestEGVSTextView.setTextColor(Color.WHITE); //0xffffffff
            imageView.setImageResource(R.drawable.white_trans);
            System.out.println("TextView set to white");
        }

        switch (latestEGVSTrend) {
            case("flat"):
                System.out.println("Rotated none (flat)");
                break;
            case("fortyFiveUp"):
                imageView.setRotation(-45);
                System.out.println("Rotated 45 up");
                break;
            case("singleUp"):
                imageView.setRotation(-90);
                System.out.println("Rotated 90 up");
                break;
            case("doubleUp"):
                imageView.setRotation(-90);
                System.out.println("Rotated 90 up (doubleUp");
                break;
            case("fortyFiveDown"):
                imageView.setRotation(45);
                System.out.println("Rotated 45 down");
                break;
            case("singleDown"):
                imageView.setRotation(90);
                System.out.println("Rotated 90 down");
                break;
            case("doubleDown"):
                imageView.setRotation(90);
                System.out.println("Rotated 90 down (doubleDown");
            default:
                imageView.setVisibility(View.INVISIBLE);
        }

        System.out.println("Latest EGVS: " + latestEGVSDate.toString() + " and " + latestEGVSValue + " and " + latestEGVSTrend);

        for (EGVS currentEGVS : reversedList) {
            dataSeries.append(currentEGVS.systemTimeEGVS, currentEGVS.valueEGVS);
        }

        final long ONE_MINUTE_IN_MILLIS = 60000;
        final long ONE_HOUR_IN_MILLIS = ONE_MINUTE_IN_MILLIS * 60;

        long t = latestEGVSDate.getTime();
        Date rightNowPlusFiveMin = new Date(t + ((5 * ONE_MINUTE_IN_MILLIS)));
        Date rightNowMinusThreeHr = new Date(t - (3 * ONE_HOUR_IN_MILLIS));

        xBottomAxis.setVisibleRange(new DateRange(rightNowMinusThreeHr, rightNowPlusFiveMin));

        System.out.println("Current visible data range: " + xBottomAxis.getVisibleRange());
        xBottomAxis.setAxisTitle(xAxisTitle);


        Collections.addAll(surface.getXAxes(), xBottomAxis);
        Collections.addAll(surface.getYAxes(), yRightAxis);
        Collections.addAll(surface.getRenderableSeries(), rs1);
        Collections.addAll(surface.getAnnotations(), upperLimit, lowerLimit);
        Collections.addAll(surface.getChartModifiers(), sciChartBuilder.newModifierGroupWithDefaultModifiers().build());

        // Create a ZoomPanModifier
        zoomPanModifier = new ZoomPanModifier();
        zoomPanModifier.setClipModeX(ClipMode.ClipAtExtents);
        zoomPanModifier.setDirection(Direction2D.XDirection);
        zoomPanModifier.setZoomExtentsY(false);


        // Add the modifier to the surface
        surface.getChartModifiers().add(zoomPanModifier);

        rolloverModifier = new RolloverModifier();
        rolloverModifier.setSourceMode(SourceMode.AllVisibleSeries);
        rolloverModifier.setDrawVerticalLine(true);
        rolloverModifier.setShowTooltip(true);
        rolloverModifier.getVerticalLinePaint().setStrokeWidth(8);


   //     XyzSeriesInfo testInfo = new XyzSeriesInfo();
   //     XyzSeriesTooltip testTooltip = new XyzSeriesTooltip();

        surface.getChartModifiers().add(rolloverModifier);
        rolloverModifier.setIsEnabled(false);

        surface.setTheme(R.style.SciChart_Glucu);
    }

    private static final class HorizontalThresholdPaletteProvider extends PaletteProviderBase<XyScatterRenderableSeries> implements IPointMarkerPaletteProvider {
        IAnnotation upperLimit, lowerLimit;
        int upperColor = ColorUtil.Yellow, mediumColor = ColorUtil.White, lowerColor = ColorUtil.Red;
        IntegerValues colors = new IntegerValues();

        HorizontalThresholdPaletteProvider(IAnnotation upperLimit, IAnnotation lowerLimit) {
            super(XyScatterRenderableSeries.class);
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
        }

        @Override
        public void update() {
            final XyScatterRenderableSeries scatterSeries = this.renderableSeries;
            final XyRenderPassData currentRenderPassData = (XyRenderPassData) scatterSeries.getCurrentRenderPassData();
            final DoubleValues yValues = currentRenderPassData.yValues;
            final int size = currentRenderPassData.pointsCount();
            colors.setSize(size);
            final double y1 = (Double)upperLimit.getY1();
            final double y2 = (Double)lowerLimit.getY1();
            final double min = Math.min(y1, y2);
            final double max = Math.max(y1, y2);
            final int[] colorsArray = colors.getItemsArray();
            final double[] valuesArray = yValues.getItemsArray();
            for (int i = 0; i < size; i++) {
                final double value = valuesArray[i];
                if (value >= max)
                    colorsArray[i] = upperColor;
                else if(value > min && value < max)
                    colorsArray[i] = mediumColor;
                else
                    colorsArray[i] = lowerColor;
            }
        }

        @Override
        public IntegerValues getPointMarkerColors() {
            return colors;
        }
    }

    public void showDetails(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if(checked) {
            zoomPanModifier.setIsEnabled(false);
            rolloverModifier.setIsEnabled(true);
        } else {
            rolloverModifier.setIsEnabled(false);
            zoomPanModifier.setIsEnabled(true);
        }
    }

}
