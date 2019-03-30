package transferdata;

import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/*
I’m recently developing an application that has to communicate with a POS thermal printer
and after a long research and some trials decided best way would be to send commands through usb port.
 Unfortunately I don’t have a POS printer right now but I managed to claim a USB device (GamePad)
 with the help of Usb4Java

I’m posting their example code because seems to be confusing for some people at first (including me)
The code is briefly explained at their website Low Level Api
If your device cannot be claimed, install libusb’s own driver with Zadig
You will need to find the right end point to communicate, the easiest way is to pop a usb with ubuntu on it,
then in terminal use “lsusb” command
This will give you a list of usb devices with their vendors and produkt numbers
Note the vendor and product number of device you want and use “lsusb -v -d vendor_nr:product_nr” to get your devices endpoint
 */
public class UsbLowPos {

    private DeviceHandle handle;
    private Context context;
    private short vendor;
    private short product;
    private byte endpoint;

    public UsbLowPos() {
        context = new Context();
    }

    public void claimDevice(short vendor, short produkt) {
        findDevice(context, vendor, produkt);
    }

    public void releaseUsb() {

        LibUsb.releaseInterface(handle, 0);
        LibUsb.close(handle);

    }

    public static void main(String[] args) {

        UsbLowPos upos = new UsbLowPos();

        upos.claimDevice((short) (0x1504), (short) (0x001F));

        char[] initEP = new char[]{0x1b, '@'};
        char[] cutP = new char[]{0x1d, 'V', 1};

        String ptxt = new String(initEP) + 'n';



        upos.print(ptxt);

        upos.print("abc n");
        upos.print("abc n");

        upos.print("nnnnnnnnn");

        upos.print(new String(cutP));
        upos.releaseUsb();

    }

    public void setEndpoint(byte endpoint) {
        this.endpoint = endpoint;
    }

    public byte getEndpoint() {
        return endpoint;
    }

    public void findDevice(Context context, short vendorId, short productId) {

        // Initialize the libusb context
        int result = LibUsb.init(context);
        if (result < 0) {
            throw new LibUsbException("Unable to initialize libusb", result);
        }

        // Read the USB device list
        DeviceList list = new DeviceList();
        result = LibUsb.getDeviceList(context, list);
        if (result < 0) {
            throw new LibUsbException("Unable to get device list", result);
        }

        try {
            // Iterate over all devices and list them
            for (Device device : list) {

                int address = LibUsb.getDeviceAddress(device);
                int busNumber = LibUsb.getBusNumber(device);
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);

                if (result < 0) {
                    throw new LibUsbException(
                            "Unable to read device descriptor", result);
                }

                if (result != LibUsb.SUCCESS) {
                    throw new LibUsbException("Unable to read device descriptor", result);
                }
                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) {

                    System.out.println("Device Found");
                    getDeviceHandle(device);
                    //LibUsb.claimInterface(handle, 0);
                }

            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }
    }

    public void getDeviceHandle(Device device) {

        handle = new DeviceHandle();

        int result = LibUsb.open(device, handle);

        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to open USB device", result);
        }

        try {
            // Use device handle here
            claimDevice(handle, 0);
        } finally {
            //LibUsb.close(handle);
        }
    }

    public void claimDevice(DeviceHandle handle, int interfaceNumber) {
        int result = LibUsb.claimInterface(handle, interfaceNumber);

        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to claim interface", result);
        }

    }

    public boolean print(String text) {

        ByteBuffer buffer = ByteBuffer.allocateDirect(text.getBytes().length);

        buffer.put(text.getBytes());

        IntBuffer transfered = IntBuffer.allocate(3);

        int result = LibUsb.bulkTransfer(handle, endpoint, buffer, transfered, 3000);

        if (result != LibUsb.SUCCESS) {
            System.out.println("EXCEPTION THROWN");
            return false;
        }

        System.out.println(transfered.get() + " bytes sent");
        return true;
    }

}