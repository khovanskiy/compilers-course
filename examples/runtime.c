#include "stdio.h"

int read() {
    int a;
    printf("> ");
    scanf("%d", &a);
    return a;
}

void write(int value) {
    printf("%d\n", value);
}