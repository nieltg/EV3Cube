#include <errno.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <linux/limits.h>
#include <string.h>
#include <unistd.h>
#include "coordcube.h"
#include "cubiecube.h"
#include "cache.h"

short parityMove[2][18] = {
    { 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1 },
    { 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0 }
};

void move(coordcube_t* coordcube, int m)
{
    coordcube->twist = cache->twistMove[coordcube->twist][m];
    coordcube->flip = cache->flipMove[coordcube->flip][m];
    coordcube->parity = parityMove[coordcube->parity][m];
    coordcube->FRtoBR = cache->FRtoBR_Move[coordcube->FRtoBR][m];
    coordcube->URFtoDLF = cache->URFtoDLF_Move[coordcube->URFtoDLF][m];
    coordcube->URtoUL = cache->URtoUL_Move[coordcube->URtoUL][m];
    coordcube->UBtoDF = cache->UBtoDF_Move[coordcube->UBtoDF][m];
    if (coordcube->URtoUL < 336 && coordcube->UBtoDF < 336)// updated only if UR,UF,UL,UB,DR,DF
        // are not in UD-slice
        coordcube->URtoDF = cache->MergeURtoULandUBtoDF[coordcube->URtoUL][coordcube->UBtoDF];
}

int join_path(char *buf, int len, const char *dir, const char *filename)
{
    int out = snprintf (buf, len, "%s/%s", dir, filename);
    return (out >= 0) && (out < len);
}

int check_cached_table(const char* name, void* ptr, int len, const char *cache_dir)
{
	char fname[PATH_MAX];
    if (!join_path(fname, PATH_MAX, cache_dir, name)) {
        fprintf(stderr, "Path to cache tables is too long\n");
        return -1;
    }
    int res = 0;
    if (access(fname, F_OK | R_OK) != -1) {
        // fprintf(stderr, "Found cache for %s. Loading...", name);
        read_from_file(ptr, len, fname);
        // fprintf(stderr, "done.\n");
        res = 0;
    } else {
        fprintf(stderr, "Cache table %s was not found. Recalculating.\n", fname);
        res = 1;
    }

    return res;
}

void read_from_file(void* ptr, int len, const char* name)
{
    FILE* f = fopen(name, "r");
    if (!fread(ptr, len, 1, f))
        ((void)0); // suppress -Wunused-result warning
    fclose(f);
}

void dump_to_file(void* ptr, int len, const char* name, const char *cache_dir)
{
    int status;
    status = mkdir(cache_dir, S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
    if (status == 0 || errno == EEXIST) {
        char fname[PATH_MAX];
        if (!join_path (fname, PATH_MAX, cache_dir, name)) {
            fprintf(stderr, "Path to cache tables is too long\n");
        } else {
            FILE* f = fopen(fname, "w");
            fwrite(ptr, len, 1, f);
            fclose(f);
        }
    } else {
        fprintf(stderr, "cannot create cache tables directory\n");
    }
}

void init_coordcube (coordcube_t* result, cubiecube_t* cubiecube)
{
    result->twist       = getTwist(cubiecube);
    result->flip        = getFlip(cubiecube);
    result->parity      = cornerParity(cubiecube);
    result->FRtoBR      = getFRtoBR(cubiecube);
    result->URFtoDLF    = getURFtoDLF(cubiecube);
    result->URtoUL      = getURtoUL(cubiecube);
    result->UBtoDF      = getUBtoDF(cubiecube);
    result->URtoDF      = getURtoDF(cubiecube);// only needed in phase2
}

void initPruning(const char *cache_dir)
{
    cubiecube_t a;
    cubiecube_t* moveCube = cache->moveCube;

    if(check_cached_table("twistMove", (void*) cache->twistMove, sizeof(cache->twistMove), cache_dir) != 0) {
        init_cubiecube (&a);
        for (short i = 0; i < N_TWIST; i++) {
            setTwist(&a, i);
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 3; k++) {
                    cornerMultiply(&a, &moveCube[j]);
                    cache->twistMove[i][3 * j + k] = getTwist(&a);
                }
                cornerMultiply(&a, &moveCube[j]);// 4. faceturn restores
            }
        }
        dump_to_file((void*) cache->twistMove, sizeof(cache->twistMove), "twistMove", cache_dir);
    }

    if(check_cached_table("flipMove", (void*) cache->flipMove, sizeof(cache->flipMove), cache_dir) != 0) {
        init_cubiecube (&a);
        for (short i = 0; i < N_FLIP; i++) {
            setFlip(&a, i);
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 3; k++) {
                    edgeMultiply(&a, &moveCube[j]);
                    cache->flipMove[i][3 * j + k] = getFlip(&a);
                }
                edgeMultiply(&a, &moveCube[j]);
            }
        }
        dump_to_file((void*) cache->flipMove, sizeof(cache->flipMove), "flipMove", cache_dir);
    }

    if(check_cached_table("FRtoBR_Move", (void*) cache->FRtoBR_Move, sizeof(cache->FRtoBR_Move), cache_dir) != 0) {
        init_cubiecube (&a);
        for (short i = 0; i < N_FRtoBR; i++) {
            setFRtoBR(&a, i);
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 3; k++) {
                    edgeMultiply(&a, &moveCube[j]);
                    cache->FRtoBR_Move[i][3 * j + k] = getFRtoBR(&a);
                }
                edgeMultiply(&a, &moveCube[j]);
            }
        }
        dump_to_file((void*) cache->FRtoBR_Move, sizeof(cache->FRtoBR_Move), "FRtoBR_Move", cache_dir);
    }

    if(check_cached_table("URFtoDLF_Move", (void*) cache->URFtoDLF_Move, sizeof(cache->URFtoDLF_Move), cache_dir) != 0) {
        init_cubiecube (&a);
        for (short i = 0; i < N_URFtoDLF; i++) {
            setURFtoDLF(&a, i);
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 3; k++) {
                    cornerMultiply(&a, &moveCube[j]);
                    cache->URFtoDLF_Move[i][3 * j + k] = getURFtoDLF(&a);
                }
                cornerMultiply(&a, &moveCube[j]);
            }
        }
        dump_to_file((void*) cache->URFtoDLF_Move, sizeof(cache->URFtoDLF_Move), "URFtoDLF_Move", cache_dir);
    }

    if(check_cached_table("URtoDF_Move", (void*) cache->URtoDF_Move, sizeof(cache->URtoDF_Move), cache_dir) != 0) {
        init_cubiecube (&a);
        for (short i = 0; i < N_URtoDF; i++) {
            setURtoDF(&a, i);
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 3; k++) {
                    edgeMultiply(&a, &moveCube[j]);
                    cache->URtoDF_Move[i][3 * j + k] = (short) getURtoDF(&a);
                    // Table values are only valid for phase 2 moves!
                    // For phase 1 moves, casting to short is not possible.
                }
                edgeMultiply(&a, &moveCube[j]);
            }
        }
        dump_to_file((void*) cache->URtoDF_Move, sizeof(cache->URtoDF_Move), "URtoDF_Move", cache_dir);
    }

    if(check_cached_table("URtoUL_Move", (void*) cache->URtoUL_Move, sizeof(cache->URtoUL_Move), cache_dir) != 0) {
        init_cubiecube (&a);
        for (short i = 0; i < N_URtoUL; i++) {
            setURtoUL(&a, i);
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 3; k++) {
                    edgeMultiply(&a, &moveCube[j]);
                    cache->URtoUL_Move[i][3 * j + k] = getURtoUL(&a);
                }
                edgeMultiply(&a, &moveCube[j]);
            }
        }
        dump_to_file((void*) cache->URtoUL_Move, sizeof(cache->URtoUL_Move), "URtoUL_Move", cache_dir);
    }

    if(check_cached_table("UBtoDF_Move", (void*) cache->UBtoDF_Move, sizeof(cache->UBtoDF_Move), cache_dir) != 0) {
        init_cubiecube (&a);
        for (short i = 0; i < N_UBtoDF; i++) {
            setUBtoDF(&a, i);
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 3; k++) {
                    edgeMultiply(&a, &moveCube[j]);
                    cache->UBtoDF_Move[i][3 * j + k] = getUBtoDF(&a);
                }
                edgeMultiply(&a, &moveCube[j]);
            }
        }
        dump_to_file((void*) cache->UBtoDF_Move, sizeof(cache->UBtoDF_Move), "UBtoDF_Move", cache_dir);
    }

    if(check_cached_table("MergeURtoULandUBtoDF", (void*) cache->MergeURtoULandUBtoDF, sizeof(cache->MergeURtoULandUBtoDF), cache_dir) != 0) {
        // for i, j <336 the six edges UR,UF,UL,UB,DR,DF are not in the
        // UD-slice and the index is <20160
        for (short uRtoUL = 0; uRtoUL < 336; uRtoUL++) {
            for (short uBtoDF = 0; uBtoDF < 336; uBtoDF++) {
                cache->MergeURtoULandUBtoDF[uRtoUL][uBtoDF] = (short) getURtoDF_standalone(uRtoUL, uBtoDF);
            }
        }
        dump_to_file((void*) cache->MergeURtoULandUBtoDF, sizeof(cache->MergeURtoULandUBtoDF), "MergeURtoULandUBtoDF", cache_dir);
    }

    int depth, done;

    if(check_cached_table("Slice_URFtoDLF_Parity_Prun", (void*) cache->Slice_URFtoDLF_Parity_Prun, sizeof(cache->Slice_URFtoDLF_Parity_Prun), cache_dir) != 0) {
        for (int i = 0; i < N_SLICE2 * N_URFtoDLF * N_PARITY / 2; i++)
            cache->Slice_URFtoDLF_Parity_Prun[i] = -1;
        depth = 0;
        setPruning(cache->Slice_URFtoDLF_Parity_Prun, 0, 0);
        done = 1;
        while (done != N_SLICE2 * N_URFtoDLF * N_PARITY) {
            for (int i = 0; i < N_SLICE2 * N_URFtoDLF * N_PARITY; i++) {
                int parity = i % 2;
                int URFtoDLF = (i / 2) / N_SLICE2;
                int slice = (i / 2) % N_SLICE2;
                if (getPruning(cache->Slice_URFtoDLF_Parity_Prun, i) == depth) {
                    for (int j = 0; j < 18; j++) {
                        int newSlice;
                        int newURFtoDLF;
                        int newParity;
                        switch (j) {
                        case 3:
                        case 5:
                        case 6:
                        case 8:
                        case 12:
                        case 14:
                        case 15:
                        case 17:
                            continue;
                        default:
                            newSlice = cache->FRtoBR_Move[slice][j];
                            newURFtoDLF = cache->URFtoDLF_Move[URFtoDLF][j];
                            newParity = parityMove[parity][j];
                            if (getPruning(cache->Slice_URFtoDLF_Parity_Prun, (N_SLICE2 * newURFtoDLF + newSlice) * 2 + newParity) == 0x0f) {
                                setPruning(cache->Slice_URFtoDLF_Parity_Prun, (N_SLICE2 * newURFtoDLF + newSlice) * 2 + newParity,
                                        (signed char) (depth + 1));
                                done++;
                            }
                        }
                    }
                }
            }
            depth++;
        }
        dump_to_file((void*) cache->Slice_URFtoDLF_Parity_Prun, sizeof(cache->Slice_URFtoDLF_Parity_Prun), "Slice_URFtoDLF_Parity_Prun", cache_dir);
    }

    if(check_cached_table("Slice_URtoDF_Parity_Prun", (void*) cache->Slice_URtoDF_Parity_Prun, sizeof(cache->Slice_URtoDF_Parity_Prun), cache_dir) != 0) {
        for (int i = 0; i < N_SLICE2 * N_URtoDF * N_PARITY / 2; i++)
            cache->Slice_URtoDF_Parity_Prun[i] = -1;
        depth = 0;
        setPruning(cache->Slice_URtoDF_Parity_Prun, 0, 0);
        done = 1;
        while (done != N_SLICE2 * N_URtoDF * N_PARITY) {
            for (int i = 0; i < N_SLICE2 * N_URtoDF * N_PARITY; i++) {
                int parity = i % 2;
                int URtoDF = (i / 2) / N_SLICE2;
                int slice = (i / 2) % N_SLICE2;
                if (getPruning(cache->Slice_URtoDF_Parity_Prun, i) == depth) {
                    for (int j = 0; j < 18; j++) {
                        int newSlice;
                        int newURtoDF;
                        int newParity;
                        switch (j) {
                        case 3:
                        case 5:
                        case 6:
                        case 8:
                        case 12:
                        case 14:
                        case 15:
                        case 17:
                            continue;
                        default:
                            newSlice = cache->FRtoBR_Move[slice][j];
                            newURtoDF = cache->URtoDF_Move[URtoDF][j];
                            newParity = parityMove[parity][j];
                            if (getPruning(cache->Slice_URtoDF_Parity_Prun, (N_SLICE2 * newURtoDF + newSlice) * 2 + newParity) == 0x0f) {
                                setPruning(cache->Slice_URtoDF_Parity_Prun, (N_SLICE2 * newURtoDF + newSlice) * 2 + newParity,
                                        (signed char) (depth + 1));
                                done++;
                            }
                        }
                    }
                }
            }
            depth++;
        }
        dump_to_file((void*) cache->Slice_URtoDF_Parity_Prun, sizeof(cache->Slice_URtoDF_Parity_Prun), "Slice_URtoDF_Parity_Prun", cache_dir);
    }
    
    if(check_cached_table("Slice_Twist_Prun", (void*) cache->Slice_Twist_Prun, sizeof(cache->Slice_Twist_Prun), cache_dir) != 0) {
        for (int i = 0; i < N_SLICE1 * N_TWIST / 2 + 1; i++)
            cache->Slice_Twist_Prun[i] = -1;
        depth = 0;
        setPruning(cache->Slice_Twist_Prun, 0, 0);
        done = 1;
        while (done != N_SLICE1 * N_TWIST) {
            for (int i = 0; i < N_SLICE1 * N_TWIST; i++) {
                int twist = i / N_SLICE1, slice = i % N_SLICE1;
                if (getPruning(cache->Slice_Twist_Prun, i) == depth) {
                    for (int j = 0; j < 18; j++) {
                        int newSlice = cache->FRtoBR_Move[slice * 24][j] / 24;
                        int newTwist = cache->twistMove[twist][j];
                        if (getPruning(cache->Slice_Twist_Prun, N_SLICE1 * newTwist + newSlice) == 0x0f) {
                            setPruning(cache->Slice_Twist_Prun, N_SLICE1 * newTwist + newSlice, (signed char) (depth + 1));
                            done++;
                        }
                    }
                }
            }
            depth++;
        }
        dump_to_file((void*) cache->Slice_Twist_Prun, sizeof(cache->Slice_Twist_Prun), "Slice_Twist_Prun", cache_dir);
    }

    if(check_cached_table("Slice_Flip_Prun", (void*) cache->Slice_Flip_Prun, sizeof(cache->Slice_Flip_Prun), cache_dir) != 0) {
        for (int i = 0; i < N_SLICE1 * N_FLIP / 2; i++)
            cache->Slice_Flip_Prun[i] = -1;
        depth = 0;
        setPruning(cache->Slice_Flip_Prun, 0, 0);
        done = 1;
        while (done != N_SLICE1 * N_FLIP) {
            for (int i = 0; i < N_SLICE1 * N_FLIP; i++) {
                int flip = i / N_SLICE1, slice = i % N_SLICE1;
                if (getPruning(cache->Slice_Flip_Prun, i) == depth) {
                    for (int j = 0; j < 18; j++) {
                        int newSlice = cache->FRtoBR_Move[slice * 24][j] / 24;
                        int newFlip = cache->flipMove[flip][j];
                        if (getPruning(cache->Slice_Flip_Prun, N_SLICE1 * newFlip + newSlice) == 0x0f) {
                            setPruning(cache->Slice_Flip_Prun, N_SLICE1 * newFlip + newSlice, (signed char) (depth + 1));
                            done++;
                        }
                    }
                }
            }
            depth++;
        }
        dump_to_file((void*) cache->Slice_Flip_Prun, sizeof(cache->Slice_Flip_Prun), "Slice_Flip_Prun", cache_dir);
    }
}

void setPruning(signed char *table, int index, signed char value) {
    if ((index & 1) == 0)
        table[index / 2] &= 0xf0 | value;
    else
        table[index / 2] &= 0x0f | (value << 4);
}

// Extract pruning value
signed char getPruning(signed char *table, int index) {
    signed char res;

    if ((index & 1) == 0)
        res = (table[index / 2] & 0x0f);
    else
        res = ((table[index / 2] >> 4) & 0x0f);

    return res;
}

