#!/bin/bash

until echo quit | telnet $1 22 2>/dev/null | grep Connected
do
  sleep 1
done