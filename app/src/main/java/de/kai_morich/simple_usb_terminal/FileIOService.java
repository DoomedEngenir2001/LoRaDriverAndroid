package de.kai_morich.simple_usb_terminal;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileIOService extends Service {
    private Binder binder = new Binder();
    @Override
    public IBinder onBind(Intent intent){
        Toast.makeText(this, "file binded", Toast.LENGTH_SHORT).show();
        return binder;
    }

    public byte[] readFile(String filename) throws  IOException{
        Toast.makeText(this, "read proceed", Toast.LENGTH_SHORT).show();
        FileInputStream fin = new FileInputStream(new File(filename));
        byte[] data = new byte[fin.available()];
        fin.read(data);
        //    String text = new String( data);
            return data;
        }


    }

