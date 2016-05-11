package nieltg.robomsg;

import android.bluetooth.BluetoothDevice;

public class RoboMessaging
{
	// See http://standards.ieee.org/regauth/oui/index.shtml
	public static final String OUI_LEGO = "00:16:53";

	public static boolean isRoboDevice (BluetoothDevice device)
	{
		return device.getAddress ().startsWith (OUI_LEGO);
	}
}

