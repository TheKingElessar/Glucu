package com.example.glucu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivityWear extends WearableActivity {


    private TextView textView;
    Button talkButton;
    int receivedMessageNumber = 1;
    int sentMessageNumber = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);
        textView = findViewById(R.id.text);
        talkButton = findViewById(R.id.talkClick);

        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, newFilter); // TODO: Changed this to earlier. Maybe it helps? :)

//Create an OnClickListener//
        String onClickMessage = "I just sent the handheld a message " + sentMessageNumber++;
        textView.setText(onClickMessage);

//Make sure you’re using the same path value//

        String datapath = "/my_path";
        System.out.println("About to send message to phone");
        new SendMessage(datapath, onClickMessage).start();

        talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "MESSAGE SENT FROM WATCH!";
                textView.setText(message);
                new MainActivityWear.SendMessage("/my_path", message).start();

            }
        });

//Register the local broadcast receiver//

    }


    public class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Watch received message... again! This time in main activity");
            Intent startingIntent = getIntent();
            String messageFromPhone = startingIntent.getStringExtra("message");
            textView.setText(messageFromPhone);

        }
    }

    class SendMessage extends Thread {
        String path;
        String message;

//Constructor///

        SendMessage(String p, String m) {
            path = p;
            message = m;
        }

//Send the message via the thread. This will send the message to all the currently-connected devices//

        public void run() {

//Get all the nodes//

            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            try {

//Block on a task and get the result synchronously//

                List<Node> nodes = Tasks.await(nodeListTask);

//Send the message to each device//

                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(MainActivityWear.this).sendMessage(node.getId(), path, message.getBytes());

                    try {


                        Integer result = Tasks.await(sendMessageTask);


//Handle the errors//

                    } catch (ExecutionException exception) {

//TO DO//

                    } catch (InterruptedException exception) {

//TO DO//

                    }

                }

            } catch (ExecutionException exception) {

//TO DO//

            } catch (InterruptedException exception) {

//TO DO//

            }
        }
    }
}