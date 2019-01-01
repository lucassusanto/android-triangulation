#include <SPI.h>
#include <nRF24L01.h>
#include <RF24.h>

char buff[32], index = 0; // Internal INPUT
char buffExternal[32]; // External INPUT

// Radio
RF24 radio(7, 8); // CE, CSN
const byte address[6] = "00001";

// Delay between each broadcast. To avoid broadcast collision
int broadcastDelay = 2000, currentDelay = 0;

// My Device Info
char myDevice[5] = "TRI1\0";
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

  randomSeed(analogRead(0));
  broadcastDelay += random(0, 20);
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

  // Set Broadcast Delay
  else if(strncmp(buff, "SBD", 3) == 0) {
    char tmp[3];
    sscanf(buff + 4, "%s", tmp);
    
    int bd = String(tmp).toInt();
    
    // Min 2 seconds
    if(bd < 2) {
      Serial.println("ERR");
    }
    
    broadcastDelay = bd * 1000 + random(0, 20);

    Serial.println("OK");
  }
  
  // Set Position
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

void processExternalInput() {
  // Get position data from other devices
  if(strncmp(buffExternal, "TRI", 3) == 0) {
    char exDevice[4], exLatitude[12], exLongitude[12];
    sscanf(buffExternal, "%s %s %s", exDevice, exLatitude, exLongitude);
    
    Serial.print("nRF24L01< ");
    Serial.print(exDevice);
    Serial.print(" ");
    Serial.print(exLatitude);
    Serial.print(" ");
    Serial.println(exLongitude);
  }
}

void broadcastMyPosition() {
  if(myLatitude == 0 && myLongitude == 0) return;
  
  char myInfo[32];
  sprintf(myInfo, "%s %s %s", myDevice, String(myLatitude, 6).c_str(), String(myLongitude, 6).c_str());

  radio.write(&myInfo, sizeof(myInfo));

  Serial.print("nRF24L01> ");
  Serial.println(myInfo);
}
