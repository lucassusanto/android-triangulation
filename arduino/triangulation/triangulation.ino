#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

// Internal & External Input
char internalBuff[32], index = 0, internalInputFlag = 0;
char externalBuff[32];

// Radio
RF24 radio(7, 8); // CE, CSN
const byte address[6] = "00001";
int broadcastDelay = 3000, currentDelay = 0;

// My Device Info
char deviceName[4] = "D1\0";
float deviceLatitude = 0, deviceLongitude = 0;

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
  if(currentDelay >= broadcastDelay) {
    radio.stopListening(); 
    broadcast();
    radio.startListening(); 
    
    currentDelay = 0;
  }
  
  getInternalInput();
  getExternalInput();

  delay(1);
  currentDelay++;
}

void getInternalInput() {
  // Read User Input
  if(Serial.available()) {
    while(Serial.available()) {
      char ch = Serial.read();
      
      if(index < 30 && (ch != ';')) {
        internalBuff[index++] = ch;
      }
      else {
        if(ch != ';') internalBuff[index++] = ch;
        internalBuff[index] = 0;        
        index = 0;
        internalInputFlag = 1;
        break;
      }
    }
  }

  // Process User Input
  if(internalInputFlag == 1) {
    if(strncmp(internalBuff, "GD", 2) == 0) {
      getDeviceName();
    }
    else if(strncmp(internalBuff, "SD", 2) == 0) {
      setDeviceName();
    }
    else if(strncmp(internalBuff, "GP", 2) == 0) {
      getDevicePosition();
    }
    else if(strncmp(internalBuff, "SP", 2) == 0) {
      setDevicePosition();
    }
    // Unknown Command
    else {
      Serial.println("ERR");
    }
  
    internalInputFlag = 0;
  }
}

void getDeviceName() {
  Serial.println(deviceName);
}

void setDeviceName() {
  sscanf(internalBuff + 3, "%s", deviceName);    
  Serial.println("OK");
}

void getDevicePosition() {
  Serial.print(deviceLatitude, 6);
  Serial.print(" ");
  Serial.println(deviceLongitude, 6);
}

void setDevicePosition() {
  String lat, lon;
  char blat[13], blon[13];
  
  sscanf(internalBuff + 3, "%s %s", blat, blon);
  
  deviceLatitude = String(blat).toFloat();
  deviceLongitude = String(blon).toFloat();
  
  Serial.println("OK");
}

void getExternalInput() {  
  // Read External Input
  if (radio.available()) {
    while (radio.available()) radio.read(&externalBuff, sizeof(externalBuff));
  
    // Process External Input
    if(strncmp(externalBuff, "TRI", 3) == 0) {
      char fDevice[4], fLatitude[12], fLongitude[12];

      sscanf(externalBuff + 4, "%s %s %s", fDevice, fLatitude, fLongitude);
      
      Serial.print("nRF24L01< ");
      Serial.print(fDevice);
      Serial.print(" ");
      Serial.print(fLatitude);
      Serial.print(" ");
      Serial.println(fLongitude);
    }
  }
}

// Broadcast Our Information
void broadcast() {
  if(deviceLatitude != 0 && deviceLongitude != 0) {
    char myInfo[32];
    sprintf(myInfo, "TRI %s %s %s\0", deviceName, String(deviceLatitude, 6).c_str(), String(deviceLongitude, 6).c_str());
  
    // Broadcast
    radio.write(&myInfo, sizeof(myInfo));
  
    Serial.print("nRF24L01> ");
    Serial.println(myInfo + 4);
  }
}
