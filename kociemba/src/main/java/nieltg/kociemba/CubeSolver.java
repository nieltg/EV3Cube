package nieltg.kociemba;

public class CubeSolver
{
	static
	{
		System.loadLibrary ("kociemba");
	}

	public static native boolean isReady ();
	public static native void prepare (String cacheDir);
	public static native String solve (String facelets) throws SolveException;
	public static native void destroy ();
}
