/*
 * Copyright 2024 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.photobox2;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
public enum AnsiColor
{
  //Color end string, color reset
  RESET("\033[0m"),
  
  // Regular Colors
  FG_BLACK("\033[0;30m"), 
  FG_RED("\033[0;31m"),
  FG_GREEN("\033[0;32m"),
  FG_YELLOW("\033[0;33m"),
  FG_BLUE("\033[0;34m"),
  FG_MAGENTA("\033[0;35m"),
  FG_CYAN("\033[0;36m"),
  FG_WHITE("\033[0;37m"),

  // Bold
  FG_BOLD_BLACK("\033[1;30m"), 
  FG_BOLD_RED("\033[1;31m"), 
  FG_BOLD_GREEN("\033[1;32m"), 
  FG_BOLD_YELLOW("\033[1;33m"), 
  FG_BOLD_BLUE("\033[1;34m"), 
  FG_BOLD_MAGENTA("\033[1;35m"), 
  FG_BOLD_CYAN("\033[1;36m"),
  FG_BOLD_WHITE("\033[1;37m"),

  // Underline
  FG_UNDERLINED_BLACK("\033[4;30m"),
  FG_UNDERLINED_RED("\033[4;31m"),
  FG_UNDERLINED_GREEN("\033[4;32m"),
  FG_UNDERLINED_YELLOW("\033[4;33m"),
  FG_UNDERLINED_BLUE("\033[4;34m"),
  FG_UNDERLINED_MAGENTA("\033[4;35m"),
  FG_UNDERLINED_CYAN("\033[4;36m"),
  FG_UNDERLINED_WHITE("\033[4;37m"),

  // Bright
  FG_BRIGHT_BLACK("\033[0;90m"),
  FG_BRIGHT_RED("\033[0;91m"),
  FG_BRIGHT_GREEN("\033[0;92m"),
  FG_BRIGHT_YELLOW("\033[0;93m"),
  FG_BRIGHT_BLUE("\033[0;94m"),
  FG_BRIGHT_MAGENTA("\033[0;95m"),
  FG_BRIGHT_CYAN("\033[0;96m"),
  FG_BRIGHT_WHITE("\033[0;97m"),

  // Bold & Bright
  FG_BOLD_BRIGHT_BLACK("\033[1;90m"),
  FG_BOLD_BRIGHT_RED("\033[1;91m"),
  FG_BOLD_BRIGHT_GREEN("\033[1;92m"),
  FG_BOLD_BRIGHT_YELLOW("\033[1;93m"),
  FG_BOLD_BRIGHT_BLUE("\033[1;94m"),
  FG_BOLD_BRIGHT_MAGENTA("\033[1;95m"),
  FG_BOLD_BRIGHT_CYAN("\033[1;96m"),
  FG_BOLD_BRIGHT_WHITE("\033[1;97m"),

  // Background
  BG_BLACK("\033[40m"),
  BG_RED("\033[41m"),
  BG_GREEN("\033[42m"),
  BG_YELLOW("\033[43m"),
  BG_BLUE("\033[44m"),
  BG_MAGENTA("\033[45m"),
  BG_CYAN("\033[46m"),
  BG_WHITE("\033[47m"),

  // Background Bright
  BG_BRIGHT_BLACK("\033[0;100m"),
  BG_BRIGHT_RED("\033[0;101m"),
  BG_BRIGHT_GREEN("\033[0;102m"),
  BG_BRIGHT_YELLOW("\033[0;103m"),
  BG_BRIGHT_BLUE("\033[0;104m"),
  BG_BRIGHT_MAGENTA("\033[0;105m"),
  BG_BRIGHT_CYAN("\033[0;106m"),
  BG_BRIGHT_WHITE("\033[0;107m");

  public final String code;

  AnsiColor(String code)
  {
    this.code = code;
  }

  @Override
  public String toString()
  {
    return code;
  }
}
