#include "stdio.h"
#include "stdlib.h"

#define DEBUG 0
#define logger_debug(...) \
            do { if (DEBUG) printf(__VA_ARGS__); } while (0)

#define ARRAY_OFFSET 1
#define reference struct _reference
#define meta_info struct _meta_info

#define UNBOXED_TYPE 0
#define BOXED_TYPE 1
#define DEFAULT_TYPE UNBOXED_TYPE

struct _meta_info {
    int count;
    int life;
    int type;
};

struct _reference {
    meta_info* meta;
    int* data;
};

int read() {
    int a;
    printf("> ");
    scanf("%d", &a);
    return a;
}

void write(int value) {
    printf("%d\n", value);
}

reference* init_reference() {
    logger_debug("Init reference\n");
    reference* ref = malloc(sizeof(reference));
    ref->meta = 0;
    ref->data = 0;
    return ref;
}

reference* new_reference(int size) {
    logger_debug("New reference allocates %d bytes\n", size);
    reference* ref = malloc(sizeof(reference));
    ref->meta = malloc(sizeof(meta_info));
    ref->meta->count = 0;
    ref->meta->type = DEFAULT_TYPE;
    ref->data = malloc(size);
    ref->data[0] = size / sizeof(int) - 1;
    return ref;
}

reference* gc_assign(reference* source, reference* destination) {
    logger_debug("gc_assign\n");
    if (source == destination) {
        return destination;
    }
    if (destination->meta != 0) {
        destination->meta->count--;
        if (destination->meta->count <= 0) {
            free(destination->meta);
            free(destination->data);
        }
    }
    destination->meta = source->meta;
    destination->data = source->data;
//    logger_debug("before gc_assign [count = %d]\n", destination->meta->count);
    destination->meta->count++;
//    logger_debug("after gc_assign [count = %d]\n", destination->meta->count);
    return destination;
}

reference* gc_acquire(reference* ref) {
    logger_debug("before gc_acquire [count = %d]\n", ref->meta->count);
    ref->meta->count++;
    logger_debug("after gc_acquire [count = %d]\n", ref->meta->count);
    return ref;
}

reference* gc_return(reference* ref) {
    logger_debug("before gc_return [count = %d, life = %d]\n", ref->meta->count, ref->meta->life);
    ref->meta->life++;
    logger_debug("after gc_return [count = %d, life = %d]\n", ref->meta->count, ref->meta->life);
    return ref;
}

reference* gc_release(reference* ref);

void gc_release_data(int type, int* data) {
    if (type == BOXED_TYPE) {
        int size = data[0];
        logger_debug("recursive gc_release for %d elements\n", size);
        for (int i = 0; i < size; ++i) {
            gc_release((reference*) data[ARRAY_OFFSET + i]);
        }
        return;
    }
    free(data);
}

reference* gc_release(reference* ref) {
    if (ref->meta == 0) {
        return ref;
    }
    logger_debug("before gc_release [count = %d, life = %d]\n", ref->meta->count, ref->meta->life);
    ref->meta->count--;
    if (ref->meta->count == 0) {
        if (ref->meta->life == 0) {
            logger_debug("Free reference\n");
            free(ref->meta);
            gc_release_data(ref->meta->type, ref->data);
            free(ref);
            return 0;
        }
        ref->meta->life--;
    }
    logger_debug("after gc_release [count = %d, life = %d]\n", ref->meta->count, ref->meta->life);
    return ref;
}

int strlen(reference* string) {
    return string->data[0];
}

int strget(reference* string, int index) {
    return string->data[ARRAY_OFFSET + index];
}

int strset(reference* string, int index, int character) {
    string->data[ARRAY_OFFSET + index] = character;
    return 0;
}

reference* strsub(reference* string, int offset, int count) {
    reference* subString = new_reference((count + 1) * sizeof(int));
    subString->data[0] = count;
    for (int i = 0; i < count; ++i) {
        subString->data[ARRAY_OFFSET + i] = string->data[ARRAY_OFFSET + i + offset];
    }
    return subString;
}

reference* strdup(reference* string) {
    int size = string->data[0];
    reference* copy = new_reference((size + 1) * sizeof(int));
    copy->data[0] = size;
    for (int i = 0; i< size; ++i) {
        copy->data[ARRAY_OFFSET + i] = string->data[ARRAY_OFFSET + i];
    }
    return copy;
}

reference* strcat(reference* lhs, reference* rhs) {
    int lhsSize = lhs->data[0];
    int rhsSize = rhs->data[0];
    int size = lhsSize + rhsSize;
    reference* string = new_reference((size + 1) * sizeof(int));
    string->data[0] = size;
    for (int i = 0; i < lhsSize; ++i) {
        string->data[ARRAY_OFFSET + i] = lhs->data[ARRAY_OFFSET + i];
    }
    for (int i = 0; i < rhsSize; ++i) {
        string->data[ARRAY_OFFSET + i + lhsSize] = rhs->data[ARRAY_OFFSET + i];
    }
    return string;
}

int cmp(int a, int b) {
    return a > b ? 1 : (a < b ? -1 : 0);
}

int min(int a, int b) {
    return a > b ? b : a;
}

int strcmp(reference* lhs, reference* rhs) {
    int lhsSize = lhs->data[0];
    int rhsSize = rhs->data[0];
    int lim = ARRAY_OFFSET + min(lhsSize, rhsSize);
    int k = ARRAY_OFFSET;
    while (k < lim) {
        if (lhs->data[k] != rhs->data[k]) {
            return cmp(lhs->data[k], rhs->data[k]);
        }
        k++;
    }
    return cmp(lhsSize, rhsSize);
}

reference* strmake(int size, int defaultValue) {
    reference* string = new_reference((size + 1) * sizeof(int));
    string->data[0] = size;
    for (int i = 0; i < size; ++i) {
        string->data[ARRAY_OFFSET + i] = defaultValue;
    }
    return string;
}

reference* arrmake(int size, int defaultValue) {
    reference* array = new_reference((size + 1) * sizeof(int));
    array->data[0] = size;
    for (int i = 0; i < size; ++i) {
        array->data[ARRAY_OFFSET + i] = defaultValue;
    }
    return array;
}

int arrlen(reference* array) {
    return array->data[0];
}

reference* Arrmake(int size, reference* defaultValue) {

    reference* array = new_reference((size + 1) * sizeof(reference*));
    array->meta->type = BOXED_TYPE;
    array->data[0] = size;
    for (int i = 0; i < size; ++i) {
        if (defaultValue == 0) {
            array->data[ARRAY_OFFSET + i] = (int) init_reference();
        } else {
            array->data[ARRAY_OFFSET + i] = (int) defaultValue;
        }
    }
    return array;
}