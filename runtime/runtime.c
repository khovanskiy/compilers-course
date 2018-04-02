#include "stdio.h"
#include "stdlib.h"

#define ARRAY_OFFSET 1

int read() {
    int a;
    printf("> ");
    scanf("%d", &a);
    return a;
}

void write(int value) {
    printf("%d\n", value);
}

int strlen(int* string) {
    return string[0];
}

int strget(int* string, int index) {
    return string[ARRAY_OFFSET + index];
}

int strset(int* string, int index, int character) {
    string[ARRAY_OFFSET + index] = character;
    return 0;
}

int* strsub(int* string, int offset, int count) {
    int* subString = malloc((count + 1) * sizeof(int));
    subString[0] = count;
    for (int i = 0; i < count; ++i) {
        subString[ARRAY_OFFSET + i] = string[ARRAY_OFFSET + i + offset];
    }
    return subString;
}

int* strdup(int* string) {
    int size = string[0];
    int* copy = malloc((size + 1) * sizeof(int));
    copy[0] = size;
    for (int i = 0; i< size; ++i) {
        copy[ARRAY_OFFSET + i] = string[ARRAY_OFFSET + i];
    }
    return copy;
}

int* strcat(int* lhs, int* rhs) {
    int lhsSize = lhs[0];
    int rhsSize = rhs[0];
    int size = lhsSize + rhsSize;
    int* string = malloc((size + 1) * sizeof(int));
    string[0] = size;
    for (int i = 0; i < lhsSize; ++i) {
        string[ARRAY_OFFSET + i] = lhs[ARRAY_OFFSET + i];
    }
    for (int i = 0; i < rhsSize; ++i) {
        string[ARRAY_OFFSET + i + lhsSize] = rhs[ARRAY_OFFSET + i];
    }
    return string;
}

int cmp(int a, int b) {
    return a > b ? 1 : (a < b ? -1 : 0);
}

int min(int a, int b) {
    return a > b ? b : a;
}

int strcmp(int* lhs, int* rhs) {
    int lhsSize = lhs[0];
    int rhsSize = rhs[0];
    int lim = ARRAY_OFFSET + min(lhsSize, rhsSize);
    int k = ARRAY_OFFSET;
    while (k < lim) {
        if (lhs[k] != rhs[k]) {
            return cmp(lhs[k], rhs[k]);
        }
        k++;
    }
    return cmp(lhsSize, rhsSize);
}

int* strmake(int size, int defaultValue) {
    int* string = malloc((size + 1) * sizeof(int));
    string[0] = size;
    for (int i = 0; i < size; ++i) {
        string[ARRAY_OFFSET + i] = defaultValue;
    }
    return string;
}

int* arrmake(int size, int defaultValue) {
    int* array = malloc((size + 1) * sizeof(int));
    array[0] = size;
    for (int i = 0; i < size; ++i) {
        array[ARRAY_OFFSET + i] = defaultValue;
    }
    return array;
}

int arrlen(int* array) {
    return array[0];
}

int** Arrmake(int size, int* defaultValue) {
    int** array = malloc((size + 1) * sizeof(int*));
    array[0] = (int*) size;  // workaround
    for (int i = 0; i < size; ++i) {
        array[ARRAY_OFFSET + i] = defaultValue;
    }
    return array;
}