package nieltg.kociemba;

public class SolveException extends Exception
{
	public SolveException (int code)
	{
		super (getMessageFromCode (code));
	}

	public static String getMessageFromCode (int code)
	{
		String msg = "Unknown solve error: " + code;

		switch (code)
		{
		case -1:
			msg = "There is not exactly one facelet of each colour";
			break;
		case -2:
			msg = "Not all 12 edges exist exactly once";
			break;
		case -3:
			msg = "Flip error: One edge has to be flipped";
			break;
		case -4:
			msg = "Not all corners exist exactly once";
			break;
		case -5:
			msg = "Twist error: One corner has to be twisted";
			break;
		case -6:
			msg = "Parity error: Two corners or two edges have to be exchanged";
			break;
		case -7:
			msg = "No solution exists for the given maxDepth";
			break;
		case -8:
			msg = "Timeout, no solution within given time";
			break;
		case -9:
			msg = "Cache was not initialized yet";
			break;
		}

		return msg;
	}
}
