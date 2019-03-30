import org.usb4java.*;


import javax.usb.*;
import java.util.Iterator;
import java.util.List;

public class USBListerAka {

  private Context context;

  public USBListerAka(){
    context = new Context();
  }
  public static void main(String[] args) throws UsbException {
    USBListerAka  i =new USBListerAka();
    i.listDevices();
  }

  // http://www.mets-blog.com/java-usb-communication-usb4java/
  public  void listDevices() {
    // Initialize the libusb context
    int result = LibUsb.init(this.context);
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

        byte manufacturerCode = descriptor.iManufacturer();
        System.out.println("Manufacturer index: " + manufacturerCode);


        byte productCode = descriptor.iProduct();
        System.out.println("Product index: " + productCode);
        byte serialCode = descriptor.iSerialNumber();
        System.out.println("Serial number index: " + serialCode);

        System.out.println("Vendor ID: 0x"
                + Integer.toHexString(descriptor.idVendor()));
        System.out.println("Product ID: 0x"
                + Integer.toHexString(descriptor.idProduct()));
        System.out.println("Class: " + descriptor.bDeviceClass());
        System.out.println("Subclass: " + descriptor.bDeviceSubClass());
        System.out.println("Protocol: " + descriptor.bDeviceProtocol());
        System.out.println("Maximum control packet size: "
                + descriptor.bMaxPacketSize0());
        System.out.println("Number of configurations: "
                + descriptor.bNumConfigurations());

        System.out.println();
        /*
        if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) {

          System.out.println("Device Found");
          getDeviceHandle(device);
          //LibUsb.claimInterface(handle, 0);
        }*/

      }
    } finally {
      // Ensure the allocated device list is freed
      LibUsb.freeDeviceList(list, true);
    }
  }
}