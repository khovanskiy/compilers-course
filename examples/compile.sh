#!/usr/bin/env bash

gcc -m32 -c runtime.s -o runtime.o
gcc -m32 -c $1.s -o $1.o
gcc -m32 runtime.o $1.o -o $1