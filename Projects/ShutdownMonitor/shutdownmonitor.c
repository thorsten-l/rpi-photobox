#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "shutdownmonitor.h"

int main(void)
{
  puts("GPIO ShudownMonitor (cl)2024 by Dr. Thorsten Ludewig");

  int return_code = system(SUDO PIN_SETUP_COMMAND);
  if ( return_code != 0 )
  {
    puts( "pin setup failed!" );
    return return_code;
  }

  FILE *pipe = popen(PIN_POLL_COMMAND, "r");

  if (pipe == NULL) {
    perror("popen");
    exit(1);
  }

  char buffer[1024];
  char *index = buffer;
  puts( "polling started...");
  int character;
  unsigned long duration_us = 0l;

  while ((character = fgetc(pipe)) != EOF) 
  {
    if ( character == '\r') continue;
    if ( character == '\n')
    {
      *index = 0;
      index = buffer;

      if ( buffer[0] == '+' )
      {
        duration_us = strtoul( buffer+1, NULL, 10);
      }
      else
      {
        if ( duration_us > 500000l )
        {
          // printf( "%ldus -> %s\n", duration_us, buffer );
          if ( strstr( buffer, GPIO_PIN_REBOOT ": hi ") != NULL )
          {
            puts( "REBOOT system");
            system(SUDO REBOOT_COMMAND);
          }
          if ( strstr( buffer, GPIO_PIN_POWEROFF ": hi ") != NULL )
          {
            puts( "POWEROFF system");
            system(SUDO POWEROFF_COMMAND);
          }
        }
        duration_us = 0l;
      }

      continue;
    }
    *index++ = character;
  }

  pclose(pipe);
  return -1;
}
