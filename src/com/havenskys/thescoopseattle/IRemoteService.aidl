package com.havenskys.thescoopseattle;

import com.havenskys.thescoopseattle.IRemoteServiceCallback;

interface IRemoteService {
	void registerCallback(IRemoteServiceCallback cb);
	void unregisterCallback(IRemoteServiceCallback cb);
}