#!/usr/bin/env bash

current_location=./runtime
#`dirname $0`

chmod +x "${current_location}/asm.sh"
chmod +x "${current_location}/compile.sh"
chmod +x "${current_location}/run.sh"

"${current_location}/asm.sh" "${current_location}/runtime"