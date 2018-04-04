#!/usr/bin/env bash
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

if [ "$#" -ne 2 ]; then
    echo "${RED}Error: invalid arguments count${NC}"
    echo "Usage: ./compile.sh runtime_path test_path"
    exit 1
fi

gcc -S -m32 -O0 -fno-asynchronous-unwind-tables $1/runtime.c -o ./runtime.s
gcc -m32 -c $1/runtime.s -o $1/runtime.o
gcc -m32 -c $2.s -o $2.o
gcc -m32 $1/runtime.o $2.o -o $2