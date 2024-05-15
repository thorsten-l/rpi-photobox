cmake_minimum_required(VERSION 3.10...3.27)
include(GNUInstallDirs)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra -Werror -pedantic")

#set project name
project(shutdownmonitor)

#add executables
add_executable(shutdownmonitor shutdownmonitor.c)
install(TARGETS shutdownmonitor RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR})

