package de.kai_morich.simple_usb_terminal;

import java.util.ArrayDeque;

interface SerialListener {
    void onSerialConnect      ();
    void onSerialConnectError (Exception e);
    void onSerialRead         (byte[] data);                // socket -> (MessageContainer) -> service
    void onSerialRead         (ArrayDeque<byte[]> datas);   // service -> UI thread
    void onSerialIoError      (Exception e);
}
/*
* на самом деле при передаче от MessageContainer в service используется тот же обработчик, что и в socket
* в MessageContainer он никак не перегружается и не имплементируется
* просто берется его текужий экземпляр
* */