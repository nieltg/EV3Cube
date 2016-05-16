package nieltg.kociemba;

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
				CubeSolverImpl.prepare (cacheDir);

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
					String sol = CubeSolverImpl.solve (facelets);

					if (mListener != null)
						mListener.onSolve (sol);
				} catch (SolveException e)
				{
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
				CubeSolverImpl.destroy ();

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
