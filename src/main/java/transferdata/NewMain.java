package transferdata;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;

/**
 *
 * @author admin
 */
public class NewMain {

    public NewMain() {
        UsbHub virtualRootUsbHub = ShowTopology.getVirtualRootUsbHub();

        /* This method recurses through the topology tree, using
         * the getAttachedUsbDevices() method.
         */
        // System.out.println("Using UsbHub.getAttachedUsbDevices() to show toplogy:");
        ShowTopology.processUsingGetAttachedUsbDevices(virtualRootUsbHub, "");

        /* Let's go through the topology again, but using getUsbPorts()
         * this time.
         */
        // System.out.println("Using UsbHub.getUsbPorts() to show toplogy:");
        ShowTopology.processUsingGetUsbPorts(virtualRootUsbHub, "");
        short vendorId = 0x17a8;
        short productId = 0x0101;

        UsbDevice device = findDevice(virtualRootUsbHub, vendorId, productId);
        System.out.println(device);
        communicate(device); // point 1 
//          readConf(device);
    }

    public final UsbDevice findDevice(UsbHub hub, short vendorId, short productId) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) {
                return device;
            }
            if (device.isUsbHub()) {
                device = findDevice((UsbHub) device, vendorId, productId);
                if (device != null) {
                    return device;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        new NewMain(); // start
    }

    private void communicate(UsbDevice device) {

        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        javax.usb.UsbInterface iface = configuration.getUsbInterface((byte) 0);
        try {
            iface.claim();

        } catch (Exception  ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            send(iface); // point 2
           // read(iface);// point 3 
        } finally {
            try {
                iface.release();
            } catch (Exception ex) {
                Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
// point 4

    private void send(UsbInterface iface) {
        byte b = 0x01; //0x81
        UsbEndpoint endpoint = iface.getUsbEndpoint(b);
        UsbPipe pipe = endpoint.getUsbPipe();
        try {
            pipe.open();
        } catch (Exception  ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            int sent = 0;
            try {
                byte bytes2[] = {(byte) 0x4B, (byte) 0x4B, (byte) 0x15, (byte) 0x5D, (byte) 0x4B, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x11, (byte) 0x00, (byte) 0x14, (byte) 0x21, (byte) 0x11, (byte) 0x01, (byte) 0x01,
                (byte) 0x02, (byte) 0x43};
                sent = pipe.syncSubmit(bytes2);

            } catch (Exception  ex) {
                Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(sent + " bytes sent");
            
        } finally {
            try {
                pipe.close();
            } catch (Exception  ex) {
                Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // poiint 4
    private void read(UsbInterface iface) {
        UsbEndpoint endpoint = iface.getUsbEndpoint((byte) 0x81);
        UsbPipe pipe = endpoint.getUsbPipe();
        pipe.addUsbPipeListener(new UsbPipeListener() {
            @Override
            public void errorEventOccurred(UsbPipeErrorEvent event) {
                UsbException error = event.getUsbException();

            }

            @Override
            public void dataEventOccurred(UsbPipeDataEvent event) {
                System.out.println("event");
                byte[] data = event.getData();
                for (int i = 0; i < data.length; i++) {
                    System.out.println(data[i]);

                }
            }

        });
        try {
            pipe.open();
            System.out.println("open");
        } catch (Exception  ex) {
            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
           
            byte bytes2[] = {(byte) 0x4B, (byte) 0x4B, (byte) 0x15, (byte) 0x5D, (byte) 0x4B, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x11, (byte) 0x00, (byte) 0x14, (byte) 0x21, (byte) 0x11, (byte) 0x01, (byte) 0x01,
                (byte) 0x02, (byte) 0x43};
            UsbIrp received = null;
            try {
                System.out.println("sync");
                received = pipe.asyncSubmit(bytes2);
                System.out.println("sync2");
            } catch (Exception  ex) {
                Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(received.getLength() + " bytes received");
            for (int i = 0; i < received.getData().length; i++) {
                System.out.print(received.getData()[i]);System.out.print (" ");
                
            }
            System.out.println();
        } finally {
//            try {
//                
//               pipe.close();
//            } catch (UsbException | UsbNotActiveException | UsbNotOpenException | UsbDisconnectedException ex) {
//                Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
    }

    private void readConf(UsbDevice device) {
        UsbControlIrp irp = device.createUsbControlIrp(
                (byte) (UsbConst.REQUESTTYPE_DIRECTION_IN
                | UsbConst.REQUESTTYPE_TYPE_STANDARD
                | UsbConst.REQUESTTYPE_RECIPIENT_DEVICE),
                UsbConst.REQUEST_GET_CONFIGURATION,
                (short) 0,
                (short) 0
        );

        byte bytes1[] = {(byte) 0x4B, (byte) 0x19, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0x1F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x11,
            (byte) 0x00, (byte) 0x14, (byte) 0x21, (byte) 0x03, (byte) 0x01, (byte) 0x5D, (byte) 0x4B,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x30, (byte) 0xF3};

        byte bytes2[] = {(byte) 0x4B, (byte) 0x4B, (byte) 0x15, (byte) 0x5D, (byte) 0x4B, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x11, (byte) 0x00, (byte) 0x14, (byte) 0x21, (byte) 0x11, (byte) 0x01, (byte) 0x01,
            (byte) 0x02, (byte) 0x43};
        irp.setData(bytes2);
        try {
            device.syncSubmit(irp);
        } catch (UsbException ex) {
            ex.printStackTrace();
        }
        for (int i = 0; i < irp.getData().length; i++) {
            System.out.print(irp.getData()[i]);
            System.out.print(" ");
        }
        System.out.println();

    }
}