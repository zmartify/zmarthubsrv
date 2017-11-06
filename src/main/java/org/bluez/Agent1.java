package org.bluez;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Path;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.UInt32;

public interface Agent1 extends DBusInterface {

    public void Release();

    public String RequestPinCode(Path path);

    /**
     * This method gets called when the service daemon needs to display a pincode
     * for an authentication. An empty reply should be returned. When the pincode
     * needs no longer to be displayed, the Cancel method of the agent will be
     * called. This is used during the pairing process of keyboards that don't
     * support Bluetooth 2.1 Secure Simple Pairing, in contrast to DisplayPasskey
     * which is used for those that do. This method will only ever be called once
     * since older keyboards do not support typing notification. Note that the PIN
     * will always be a 6-digit number, zero-padded to 6 digits. This is for harmony
     * with the later specification. Possible errors: org.bluez.Error.Rejected
     * org.bluez.Error.Canceled -->
     * 
     */
    public void DisplayPinCode(Path path, String pincode);

    /**
     * This method gets called when the service daemon needs to get the passkey for
     * an authentication. The return value should be a numeric value between
     * 0-999999. Possible errors: org.bluez.Error.Rejected org.bluez.Error.Canceled
     * 
     * @param device
     * @return
     */
    public UInt32 RequestPasskey(Path path);

    /**
     * This method gets called when the service daemon needs to display a passkey
     * for an authentication. The entered parameter indicates the number of already
     * typed keys on the remote side. An empty reply should be returned. When the
     * passkey needs no longer to be displayed, the Cancel method of the agent will
     * be called. During the pairing process this method might be called multiple
     * times to update the entered value. Note that the passkey will always be a
     * 6-digit number, so the display should be zero-padded at the start if the
     * value contains less than 6 digits.
     * 
     * @param device
     * 
     * @param passkey
     * 
     * @param entered
     */
    public void DisplayPassKey(Path path, UInt32 passkey, UInt16 entered);

    /**
     * This method gets called when the service daemon needs to confirm a passkey
     * for an authentication. To confirm the value it should return an empty reply
     * or an error in case the passkey is invalid. Note that the passkey will always
     * be a 6-digit number, so the display should be zero-padded at the start if the
     * value contains less than 6 digits. Possible errors: org.bluez.Error.Rejected
     * org.bluez.Error.Canceled
     * 
     * @param device
     * @param passkey
     */
    public void RequestConfirmation(Path path, UInt32 passkey);

    /**
     * 
     * This method gets called to request the user to authorize an incoming
     * 
     * pairing attempt which would in other circumstances trigger the just-works
     * model. Possible errors: org.bluez.Error.Rejected org.bluez.Error.Canceled
     * 
     * @param device
     */
    public void RequestAuthorization(Path path);

    /**
     * This method gets called when the service daemon needs to authorize a
     * connection/service request. Possible errors: org.bluez.Error.Rejected
     * org.bluez.Error.Canceled
     * 
     * @param device
     * @param uuid
     */
    public void AuthorizeService(Path path, String uuid);

    /**
     * This method gets called to indicate that the agent request failed before a
     * reply was returned.
     * 
     */
    public void Cancel();

}
