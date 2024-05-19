#include <Arduino.h>

#define FASTLED_ESP8266_RAW_PIN_ORDER
#include <FastLED.h>

#define DATA_PIN D7
#define NUMPIXELS 35

CRGB leds[NUMPIXELS];
static uint8_t hue = 0;
static int state = 3;
static time_t timestamp = millis();

void showColor( CRGB color )
{
  for (int i = 0; i < NUMPIXELS; i++)
  {
    leds[i] = color;
    FastLED.show();
  }
}

void setup()
{
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, NUMPIXELS);
  Serial.begin(115200);
  // Serial.begin(74880);
  // Serial.begin(9600);
  delay(2000);
  Serial.println("\n\nNeoPixelRing35 v0.9.5, (cl)2024 by Dr. Thorsten Ludewig\n");
  FastLED.setBrightness(128);
  showColor(CRGB::Red);
  delay(500);
  showColor(CRGB::Green);
  delay(500);
  showColor(CRGB::Blue);
  delay(500);
  showColor(CRGB::White);
  delay(500);
  //////////////////////////////////////
  hue = 0;
  state = 3;
  Serial.println( "setup done." );
}

void setState( int _state )
{
  Serial.printf("state=%d\n", _state );
  state = _state;
  timestamp = millis();
}

void checkIntput()
{
  static char buffer[128];
  static int bindex = 0;

  if ( Serial.available() )
  {
    int c = Serial.read();
    //Serial.printf( "c=%d, %x, %c\n", c, c, c);

    if (( c == '\r' || c == '\n' ) && bindex > 0 )
    {
      buffer[bindex] = 0;
      bindex = 0;
      /*
      Serial.print("received=");
      for( int i=0; buffer[i] != 0; i++)
      {
        Serial.printf("%x (%c) ", buffer[i], buffer[i]);
      }
      Serial.println();
      */
      // Serial.printf( "command=%s\n", buffer );
      if ( strcmp("/state", buffer) == 0 )
      {
        Serial.printf("state=%d\n", state );
      }
      if ( strcmp("/run", buffer) == 0 )
      {
        setState(0);
      }
      if ( strcmp("/flash", buffer) == 0 )
      {
        setState(1);
      }
      if ( strcmp("/off", buffer) == 0 )
      {
        setState(3);
      }
      if ( strcmp("/red", buffer) == 0 )
      {
        setState(5);
      }
      if ( strcmp("/green", buffer) == 0 )
      {
        setState(6);
      }
      if ( strcmp("/blue", buffer) == 0 )
      {
        setState(7);
      }
    }

    if ( bindex < 127 && c >= ' ' )
    {
      buffer[bindex++] = c;
    }
  }
}


void loop()
{
  checkIntput();
  switch (state)
  {
  case 0:
    if ((millis() - timestamp) <= 10000)
    {
      for (int i = 0; i < NUMPIXELS; i++)
      {
        leds[i] = CHSV(hue, 255, 255);
        hue += 3;
        FastLED.show();
        checkIntput();
        delay(20);
      }
    }
    else
    {
      setState(3);
    }
    break;

  case 1:
    showColor(CRGB::White);
    setState(2);
    break;

  case 2:
    if ((millis() - timestamp) >= 10000)
    {
      setState(3);
    }
    break;

  case 3:
    showColor(CRGB::Black);
    setState(4);
    break;

  case 5:
    showColor(CRGB::Red);
    setState(2);
    break;

  case 6:
    showColor(CRGB::Green);
    setState(2);
    break;

  case 7:
    showColor(CRGB::Blue);
    setState(2);
    break;

  default:
    state = 4;
  }
}
