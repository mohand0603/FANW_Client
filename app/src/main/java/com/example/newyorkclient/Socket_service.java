package com.example.newyorkclient;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Socket_service extends Service {

    String ip = "218.146.163.216";
    Socket socket;
    IBinder mBinder = new MyBinder();
    BufferedWriter writer;
    BufferedReader reader;
    socket_queue que = new socket_queue();

    class MyBinder extends Binder {
        Socket_service getService() {
            return Socket_service.this;
        }
    }

    public Socket_service() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, 8989);
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "EUC-KR"));
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "EUC_KR"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        String line = reader.readLine();
                        if(line == null)
                            break;
                        que.add(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    void Send(String _msg) {
        new send_thread(writer, _msg).start();
    }

    socket_queue getQue() { return this.que; }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
