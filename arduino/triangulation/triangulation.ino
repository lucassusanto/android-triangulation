#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

char buff[32], index = 0; // Internal INPUT
char buffExternal[32]; // External INPUT

// Radio
RF24 radio(7, 8); // CE, CSN
const byte address[6] = "00001";
int broadcastDelay = 3000, currentDelay = 0;

// My Device Info
char myDevice[4] = "D1\0";
float myLatitude = 0, myLongitude = 0;

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
    broadcastMyPosition();
    radio.startListening(); 
    
    currentDelay = 0;
  }
  
  getInputs();

  delay(1);
  currentDelay++;
}

void getInputs() {
  // Get Internal Inputs
  if(Serial.available()) {
    while(Serial.available()) {
      char ch = Serial.read();
  
      if(index < 30 && (ch != ';')) {
        buff[index++] = ch;
      } else {
        if(ch != ';') buff[index++] = ch;
        buff[index] = 0;
        
        processInternalInput();
        
        index = 0;
      }
    }
  }
  
  // Get External Inputs
  else if (radio.available()) {    
    while (radio.available()) radio.read(&buffExternal, sizeof(buffExternal));
    processExternalInput();
  }
}

void processInternalInput() {
  // Set Device name
  if(strncmp(buff, "SD", 2) == 0) {
    sscanf(buff + 3, "%s", myDevice);    
    Serial.println("OK");
  }
  
  // Get Device Name
  else if(strncmp(buff, "GD", 2) == 0) {
    Serial.println(myDevice);
  }
  
  // Set Latitude and Longitude
  else if(strncmp(buff, "SP", 2) == 0) {
    String lat, lon;
    char blat[13], blon[13];
    
    sscanf(buff + 3, "%s %s", blat, blon);
    
    myLatitude = String(blat).toFloat();
    myLongitude = String(blon).toFloat();
    
    Serial.println("OK");
  }
  
  // Get Latitude and Longitude
  else if(strncmp(buff, "GP", 2) == 0) {
    Serial.print(myLatitude, 6);
    Serial.print(" ");
    Serial.println(myLongitude, 6);
  }

  // Unknown Command
  else {
    Serial.println("ERR");
  }
}

// Get position data from other devices
void processExternalInput() {  
  if(strncmp(buffExternal, "TRI", 3) == 0) {
    char fDevice[4], fLatitude[12], fLongitude[12];
    
    sscanf(buffExternal + 4, "%s %s %s", fDevice, fLatitude, fLongitude);
    
    Serial.print("nRF24L01< ");
    Serial.print(fDevice);
    Serial.print(" ");
    Serial.print(fLatitude);
    Serial.print(" ");
    Serial.println(fLongitude);
  }
}

// Send position data to other devices
void broadcastMyPosition() {
  if(myLatitude == 0 && myLongitude == 0) return;
  
  char myInfo[32];
  sprintf(myInfo, "TRI %s %s %s\0", myDevice, String(myLatitude, 6).c_str(), String(myLongitude, 6).c_str());

  radio.write(&myInfo, sizeof(myInfo));

  Serial.print("nRF24L01> ");
  Serial.println(myInfo + 4);
}
