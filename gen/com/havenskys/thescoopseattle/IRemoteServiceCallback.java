/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\jon.dull\\workspace\\com.havenskys.thescoopseattle.Start\\src\\com\\havenskys\\thescoopseattle\\IRemoteServiceCallback.aidl
 */
package com.havenskys.thescoopseattle;
public interface IRemoteServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.havenskys.thescoopseattle.IRemoteServiceCallback
{
private static final java.lang.String DESCRIPTOR = "com.havenskys.thescoopseattle.IRemoteServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.havenskys.thescoopseattle.IRemoteServiceCallback interface,
 * generating a proxy if needed.
 */
public static com.havenskys.thescoopseattle.IRemoteServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.havenskys.thescoopseattle.IRemoteServiceCallback))) {
return ((com.havenskys.thescoopseattle.IRemoteServiceCallback)iin);
}
return new com.havenskys.thescoopseattle.IRemoteServiceCallback.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_valueChanged:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.valueChanged(_arg0);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.havenskys.thescoopseattle.IRemoteServiceCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void valueChanged(int value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(value);
mRemote.transact(Stub.TRANSACTION_valueChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_valueChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void valueChanged(int value) throws android.os.RemoteException;
}
