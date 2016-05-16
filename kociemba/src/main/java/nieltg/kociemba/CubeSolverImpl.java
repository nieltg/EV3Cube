package nieltg.kociemba;

public class CubeSolverImpl
{
	static
	{
		System.loadLibrary ("kociemba");
	}

	private CubeSolverImpl ()
	{
	}

	public static native boolean isReady ();
	public static native synchronized void prepare (String cacheDir);
	public static native synchronized String solve (String facelets) throws SolveException;
	public static native synchronized void destroy ();
}
