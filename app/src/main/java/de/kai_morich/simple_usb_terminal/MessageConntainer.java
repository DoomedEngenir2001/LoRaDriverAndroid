package de.kai_morich.simple_usb_terminal;

import android.widget.Toast;

import java.util.*;
// Это надо менять (а может и не надо)
// если надо добавлять какое- нибудь дополнительное действие - лучше делать это здесь
public  class MessageConntainer{
    private  final int bufferSize = 57;
    // словарь в котором хранится соответствие адрес (котороый определяется в SerialSocket ) с сообщениями который он отправил
    // все сообщения представляют собой пакеты
    private static Map<byte[], List<Message>> messageContainer= new HashMap<byte[], List<Message> >();
    // private  Zipper zip = new Zipper();
    public  void addMessage(Message message){
        byte[] id_ = message.getId();
        byte n = message.getN();
        List<Message> currentList;
        if (containAddress(messageContainer, id_)){ // если с адреа приходили сообщения
            byte[] key = getExistKey(messageContainer, id_);
            currentList = messageContainer.get(key); // if key exist
            currentList.add(message);
           // messageContainer.remove(key);
            messageContainer.put(key, currentList);
        }
        else { // если сообщение приходит в первый раз
            currentList = new Vector<Message>(240);
            currentList.add(message); // in any case append message and update in hashmap
            messageContainer.put(id_, currentList);

        }


        if (n==(byte)255){ // когда получаем пакет, котороый является последним
            int listLen =  currentList.size();
            int payloadSize = bufferSize -4;
                int size = (currentList.size() - 1)*(payloadSize)
                        + currentList.get(listLen - 1).getPayload().length;
                byte[] recievedData = new byte[size]; // собираем всю полезную нагрузку воедино
                for(int i=0;i< listLen;i++){
                    byte[] currentPayload = currentList.get(i).getPayload();
                    System.arraycopy(currentPayload, 0, // copy to array
                            recievedData, i*payloadSize, currentPayload.length);
                }
                byte[] decompressData = Zipper.deflate(recievedData); // разжимаем
                // call serial service on read
                SerialSocket.listener.onSerialRead(decompressData); // использую обработчик событий SerialSocket отправляем разжатые данные в обработчик SerialService
                // TO-DO write function to write to json file
                messageContainer.remove(getExistKey(messageContainer, id_)); // erase key

            }

    }

    public  boolean containAddress(Map<byte[], List<Message>> map, byte[] Address){ // костыль - поиск адресов, с которых отправлялись сообщения
        for(byte[] key : map.keySet()){
            if(key[0] == Address[0] && key[1] == Address[1])return true;
        }
        return false;
    }

    public  byte[] getExistKey(Map<byte[], List<Message>> map, byte[] value){// костыль получение интересующего адреса
        for(byte[] key : map.keySet()){
            if(key[0] == value[0] && key[1] == value[1])return key;
        }
        return null;
    }

}
