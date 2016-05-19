package nieltg.kociemba;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class CubeSolver
{
	private static final ExecutorService mExecutor = Executors.newSingleThreadExecutor ();

	private Listener mListener;

	public CubeSolver ()
	{
	}

	public void setListener (Listener listener)
	{
		mListener = listener;
	}

	public boolean isReady ()
	{
		return CubeSolverImpl.isReady ();
	}

	public void prepare (final String cacheDir)
	{
		mExecutor.submit (new Runnable ()
		{
			@Override
			public void run ()
			{

				Log.i ("Kociemba", "prepare, cacheDir=" + cacheDir);
				CubeSolverImpl.prepare (cacheDir);
				Log.i ("Kociemba", "prepare done");

				if (mListener != null)
					mListener.onPrepare ();
			}
		});
	}

	public void solve (final String facelets)
	{
		mExecutor.submit (new Runnable ()
		{
			@Override
			public void run ()
			{
				try
				{
					Log.i ("Kociemba", "solve, problem=" + facelets);
					String sol = CubeSolverImpl.solve (facelets);
					Log.i ("Kociemba", "solve done, solution=" + sol);

					if (mListener != null)
						mListener.onSolve (sol);
				} catch (SolveException e)
				{
					Log.w ("Kociemba", "solve error: " + e.getMessage ());

					if (mListener != null)
						mListener.onSolveError (e);
				}
			}
		});
	}

	public void destroy ()
	{
		mExecutor.submit (new Runnable ()
		{
			@Override
			public void run ()
			{

				Log.i ("Kociemba", "destroy");
				CubeSolverImpl.destroy ();
				Log.i ("Kociemba", "destroy done");

				if (mListener != null)
					mListener.onDestroy ();
			}
		});
	}

	public interface Listener
	{
		void onPrepare ();
		void onDestroy ();
		void onSolve (String solution);
		void onSolveError (SolveException e);
	}
}
