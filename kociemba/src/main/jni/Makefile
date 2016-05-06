CKOCIEMBA_SRC = coordcube.c cubiecube.c facecube.c search.c cache.c
CFLAGS = -std=c99 -O3
BINDIR = bin
BIN = kociemba

solve: solve.c $(CKOCIEMBA_SRC)
	mkdir -p $(BINDIR)
	gcc $(CFLAGS) $(CKOCIEMBA_SRC) solve.c -o $(BINDIR)/$(BIN)

