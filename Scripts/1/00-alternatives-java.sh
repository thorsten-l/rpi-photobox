#!/bin/bash

update-alternatives --install /usr/bin/java java /opt/java/latest/bin/java 1
update-alternatives --install /usr/bin/javac javac /opt/java/latest/bin/javac 1

update-alternatives --config java
update-alternatives --config javac
