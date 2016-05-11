/* cache.h */

#pragma once

#include "cubiecube.h"
#include "coordcube.h"

typedef struct
{
	/* coordcube.h */
	
	/* === Phase 1 Move Tables === */
	
	// Move table for the twists of the corners
	// twist < 2187 in phase 2
	// twist = 0 in phase 2
	short twistMove[N_TWIST][N_MOVE];
	
	// Move table for the flips of the edges
	// flip < 2048 in phase 1
	// flip = 0 in phase 2
	short flipMove[N_FLIP][N_MOVE];
	
	// parityMove is not generated
	
	/* === Phase 1 and 2 Move Tables === */
	
	// Move table for the four UD-slice edges FR, FL, Bl and BR
	// FRtoBRMove < 11880 in phase 1
	// FRtoBRMove < 24 in phase 2
	// FRtoBRMove = 0 for solved cube
	short FRtoBR_Move[N_FRtoBR][N_MOVE];
	
	// Move table for permutation of six corners.
	// The positions of the DBL and DRB corners are determined by the parity.
	// URFtoDLF < 20160 in phase 1
	// URFtoDLF < 20160 in phase 2
	// URFtoDLF = 0 for solved cube
	short URFtoDLF_Move[N_URFtoDLF][N_MOVE];
	
	// Move table for the permutation of six U-face and D-face edges in phase-2.
	// The positions of the DL and DB edges are determined by the parity.
	// URtoDF < 665280 in phase 1
	// URtoDF < 20160 in phase 2
	// URtoDF = 0 for solved cube
	short URtoDF_Move[N_URtoDF][N_MOVE];
	
	/* === Helper Move Tables to Compute URtoDF (Phase 2) == */
	
	// Move table for the three edges UR,UF and UL in phase-1.
	short URtoUL_Move[N_URtoUL][N_MOVE];
	
	// Move table for the three edges UB,DR and DF in phase-1.
	short UBtoDF_Move[N_UBtoDF][N_MOVE];
	
	// Table to merge the coordinates of the UR,UF,UL and UB,DR,DF edges
	// at the beginning of phase-2
	short MergeURtoULandUBtoDF[336][336];
	
	/* === Pruning tables === */
	
	// Pruning table for the permutation of the corners and the UD-slice edges
	// in phase-2
	// Pruning table entries give a lower estimation for the number of moves to
	// reach the solved cube
	signed char Slice_URFtoDLF_Parity_Prun[N_SLICE2 * N_URFtoDLF * N_PARITY / 2];
	
	// Pruning table for the permutation of the edges in phase-2
	// Pruning table entries give a lower estimation for the number of moves to
	// reach the solved cube
	signed char Slice_URtoDF_Parity_Prun[N_SLICE2 * N_URtoDF * N_PARITY / 2];
	
	// Pruning table for the twist of the corners and the position
	// (not permutation) of the UD-slice edges in phase-1
	// Pruning table entries give a lower estimation for the number of moves to
	// reach the H-subgroup
	signed char Slice_Twist_Prun[N_SLICE1 * N_TWIST / 2 + 1];
	
	// Pruning table for the flip of the edges and the position
	// (not permutation) of the UD-slice edges in phase-1
	// Pruning table entries give a lower estimation for the number of moves to
	// reach the H-subgroup.
	signed char Slice_Flip_Prun[N_SLICE1 * N_FLIP / 2];
	
	/* cubiecube.h */
	
	cubiecube_t moveCube[6];
}
CacheTable;

extern CacheTable* cache;

int cache_is_prepared (void);
void cache_prepare (const char *cache_dir);
void cache_destroy (void);

