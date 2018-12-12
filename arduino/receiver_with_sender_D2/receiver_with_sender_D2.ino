// Receiver (bisa switch jd sender)
#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

RF24 radio(7, 8); // CE, CSN
const byte address[6] = "00001";
const byte devName[2] = "D2";

// Delay
int count = 0;
// 0: Receiver, 1: Sender
byte role = '0'; 

void setup() {
  Serial.begin(9600);
  
  radio.begin();
  radio.setDataRate(RF24_250KBPS);
  radio.setPALevel(RF24_PA_LOW);
  radio.setChannel(108);

  radio.openWritingPipe(address);
  radio.openReadingPipe(1, address);
  
  radio.powerUp();  
  radio.startListening();
}

void loop() {  
  if(count >= 2000) {
    switchRole();
    kirim_small();
    switchRole();
    
    count = 0;
  }
  
  terima();

  delay(1);
  count++;
}

void switchRole() {
  if(role == '0') {
    // Jadi Sender
    radio.stopListening(); 
    role = '1';
  }
  else if(role == '1') {
    // Jadi Receiver
    radio.startListening(); 
    role = '0';
  }
}

void terima() {
  if (radio.available()) {
    char text[32] = "";
    
    while (radio.available())
      radio.read(&text, sizeof(text));

    Serial.print("Recv> ");
    Serial.println(text);
  }
}

void kirim_small() {
  char data1[16] = "Hello! Dari D2";
  radio.write(&data1, sizeof(data1));

  Serial.print("Sent> ");
  Serial.println(data1);
}

void kirim() {
  if(Serial.available()) {
    char ch = Serial.read();
    Serial.write(ch);

    byte data[32]; // DEBUG
    int index; // DEBUG

    if(index < 30 && (ch != ';')) {
      data[index++] = ch;
    } else {
      if(ch != ';')
        data[index++] = ch;
      
      data[index] = 0;

      radio.write(&data, sizeof(data));

//      Serial.println();
//      Serial.print("Sent> ");
//      Serial.println(data);

      index = 0;
    }
  }
}


