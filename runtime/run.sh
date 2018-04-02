#!/usr/bin/env bash
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

if [ "$#" -ne 2 ]; then
    echo "${RED}Error: invalid arguments count${NC}"
    echo "Usage: ./run.sh test_path input_path"
    exit 1
fi

cat $2 | $1

