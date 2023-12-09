package de.kai_morich.simple_usb_terminal;

public class Message {
    private byte[] Id;
    private byte n;
    private byte[] payload;
    Message(byte[] data){
        Id = new byte[2];// костыль - связан с особенностью передачи сообщений модемом
        Id[0] = data[0]; // адресс
        Id[1]= data[1];
        n = data[2];
        payload = new byte[data.length - 3]; // полезная нагрузка
        System.arraycopy(data, 3, payload, 0,data.length-3);
    }
    Message(byte[] id, byte n_, byte[] payload_){
        Id = id;
        n = n_;
        payload = payload_;
    }
    // геттеры
    byte[] getId(){
        return Id;
    }

    byte[] getPayload(){
        return  payload;
    }
    byte getN(){
        return  n;
    }

    int getSize(){
        return 4 + payload.length;
    }
    // преобразование сообщения в байты, нужно для передачи
    byte[] toArray(){
        byte[] message = new byte[4+payload.length];
        message[0] = (byte)127;
        message[1] = Id[0];
        message[2] = Id[1];
        message[3] =n;
        System.arraycopy(payload,0,message,4,payload.length);
        return message;

    }
}
