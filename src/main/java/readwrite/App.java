package readwrite;

import javax.usb.UsbDevice;
import javax.usb.UsbException;

public class App {

	public static void main(final String[] args) throws UsbException {

		Usb4JavaHigh usb4java = new Usb4JavaHigh();
		UsbDevice usbDevice = usb4java.findDevice((short) (0x46d), (short) (0xffffc52b));
		
		usb4java.readMessage(usb4java.getDeviceInterface(usbDevice, 0), 0);
	}

}
