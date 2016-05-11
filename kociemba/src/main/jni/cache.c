/* cache.c */

#include <malloc.h>
#include "cache.h"

CacheTable* cache = NULL;

int cache_is_prepared (void)
{
	return cache != NULL;
}

void cache_prepare (const char *cache_dir)
{
	if (cache != NULL) return;
	cache = (CacheTable*) calloc (1, sizeof (CacheTable));
	
	init_moveCube ();
	initPruning (cache_dir);
}

void cache_destroy (void)
{
	if (cache != NULL)
	{
		free (cache);
		cache = NULL;
	}
}

