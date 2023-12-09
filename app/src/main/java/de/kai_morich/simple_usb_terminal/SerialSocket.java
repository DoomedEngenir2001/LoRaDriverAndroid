package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.SerialTimeoutException;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.security.InvalidParameterException;

//public class SerialSocket extends CommonUsbSerialPort  implements SerialInputOutputManager.Listener   {


// реализует собственно интерфейс последовательного порта работает с байтами
public class SerialSocket  implements SerialInputOutputManager.Listener {
    private static final int WRITE_WAIT_MILLIS = 1100; // 0 blocked infinitely on unprogrammed arduino
    private final static String TAG = SerialSocket.class.getSimpleName();

    private final BroadcastReceiver disconnectBroadcastReceiver;
    public static final int  bufferSize= 57;

    public static final int payloadSize = bufferSize -4;
    private final Context context;
    public static SerialListener listener;
    private UsbDeviceConnection connection;
    private UsbSerialPort serialPort;
    private SerialInputOutputManager ioManager;
    public static final byte[] Id = {(byte) 0, (byte) 2};
    private MessageConntainer messContainer;
    SerialSocket(Context context, UsbDeviceConnection connection, UsbSerialPort serialPort) {
    //    super(connection, serialPort);
        if(context instanceof Activity)
            throw new InvalidParameterException("expected non UI context");
        this.context = context;
        this.connection = connection;
        this.serialPort = serialPort;
        messContainer = new MessageConntainer();
        disconnectBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (listener != null)
                    listener.onSerialIoError(new IOException("background disconnect"));
                disconnect(); // disconnect now, else would be queued until UI re-attached
            }


        };
    }

    String getName() { return serialPort.getDriver().getClass().getSimpleName().replace("SerialDriver",""); }
    void connect(SerialListener listener) throws IOException {
        this.listener = listener;
        context.registerReceiver(disconnectBroadcastReceiver, new IntentFilter(Constants.INTENT_ACTION_DISCONNECT));
	try {
	    serialPort.setDTR(true); // for arduino, ...
	    serialPort.setRTS(true);
	} catch (UnsupportedOperationException e) {
	    Log.d(TAG, "Failed to set initial DTR/RTS", e);
	}
        ioManager = new SerialInputOutputManager(serialPort, this);
        ioManager.setReadBufferSize(bufferSize);
        ioManager.setWriteBufferSize(bufferSize);
        ioManager.start();
    }

    void disconnect() {
        listener = null; // ignore remaining data and errors
        if (ioManager != null) {
            ioManager.setListener(null);
            ioManager.stop();
            ioManager = null;
        }
        if (serialPort != null) {
            try {
                serialPort.setDTR(false);
                serialPort.setRTS(false);
            } catch (Exception ignored) {
            }
            try {
                serialPort.close();
            } catch (Exception ignored) {
            }
            serialPort = null;
        }
        if(connection != null) {
            connection.close();
            connection = null;
        }
        try {
            context.unregisterReceiver(disconnectBroadcastReceiver);
        } catch (Exception ignored) {
        }
    }
/*
    void write(byte[] data) throws IOException {
        if(serialPort == null)
            throw new IOException("not connected");
        try{
            serialPort.write(data, WRITE_WAIT_MILLIS);
            Thread.sleep(WRITE_WAIT_MILLIS);
        }catch (SerialTimeoutException e){
            Toast.makeText(context, "ne uspel error", Toast.LENGTH_SHORT).show();
        }
        catch (InterruptedException e ){
            Toast.makeText(context, "ne uspel error", Toast.LENGTH_SHORT).show();
        }
    }
*/
// передача - принимает сообщение
    void write(Message[] packets) throws IOException{
        if(serialPort == null)
            throw new IOException("not connected");
        try{
            for(int i = 0; i<packets.length; i++){
                byte[] data = packets[i].toArray();
                serialPort.write(data, WRITE_WAIT_MILLIS);
                Thread.sleep(WRITE_WAIT_MILLIS);
            }

        }catch (SerialTimeoutException e){
            Toast.makeText(context, "ne uspel error", Toast.LENGTH_SHORT).show();
        }
        catch (InterruptedException e ){
            Toast.makeText(context, "ne uspel error", Toast.LENGTH_SHORT).show();
        }
    }
    // прием - записывает пакет(в байтах) в словарь пакетов
    @Override
    public void onNewData(byte[] data) {
        if(listener != null){
            if(data.length > 3){ // socket -> message container
                //listener.onSerialRead(data);

                Message mess = new Message(data);// create message
                messContainer.addMessage(mess); // add to message container

            }

    }
    }

    @Override
    public void onRunError(Exception e) {
        if (listener != null)
            listener.onSerialIoError(e);
    }
}
