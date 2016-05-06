#ifndef COORDCUBE_H
#define COORDCUBE_H

#include "cubiecube.h"

// Representation of the cube on the coordinate level

#define N_TWIST     2187
#define N_FLIP      2048
#define N_SLICE1    495
#define N_SLICE2    24
#define N_PARITY    2
#define N_URFtoDLF  20160
#define N_FRtoBR    11880
#define N_URtoUL    1320
#define N_UBtoDF    1320
#define N_URtoDF    20160
#define N_URFtoDLB  40320
#define N_URtoBR    479001600
#define N_MOVE      18

typedef struct {

    // All coordinates are 0 for a solved cube except for UBtoDF, which is 114
    short twist;
    short flip;
    short parity;
    short FRtoBR;
    short URFtoDLF;
    short URtoUL;
    short UBtoDF;
    int URtoDF;
} coordcube_t;

// Parity of the corner permutation.
// This is the same as the parity for the edge permutation of a valid cube
// parity has values 0 and 1
extern short parityMove[2][18];

void initPruning(const char *cache_dir);

// Set pruning value in table. Two values are stored in one char.
void setPruning(signed char *table, int index, signed char value);

// Extract pruning value
signed char getPruning(signed char *table, int index);

void init_coordcube (coordcube_t* result, cubiecube_t* cubiecube);
void move(coordcube_t* coordcube, int m);
int check_cached_table(const char* name, void* ptr, int len, const char *cache_dir);
void dump_to_file(void* ptr, int len, const char* name, const char *cache_dir);
void read_from_file(void* ptr, int len, const char* name);

#endif
