#!/bin/sh

kill $(ps aux | grep java | grep 'dif-merge' | awk '{print $2}')