#!/usr/bin/env bash

gcc -S -m32 -O0 -fno-asynchronous-unwind-tables $1.c