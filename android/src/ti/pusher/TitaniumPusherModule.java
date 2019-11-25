/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2018 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package ti.pusher;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;
import org.appcelerator.kroll.KrollDict;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateChannel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

import java.util.HashMap;

@Kroll.module(name="TitaniumPusher", id="ti.pusher")
public class TitaniumPusherModule extends KrollModule
{
	// Standard Debugging variables

	private static final String LCAT = "TitaniumPusherModule";
	private static final boolean DBG = TiConfig.LOGD;

	private Pusher pusher;

	public TitaniumPusherModule()
	{
		super();
	}

	// Methods

	@Kroll.method
	public void initialize(KrollDict args)
	{
		String key = args.getString("key");
		KrollDict proxyOptions = args.getKrollDict("options");

        PusherOptions options = new PusherOptions();

		if (proxyOptions != null) {
            String authEndpoint = proxyOptions.getString("authEndpoint");
            String accessToken = proxyOptions.getString("accessToken");
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization",  accessToken);

            HttpAuthorizer authorizer = new HttpAuthorizer(authEndpoint);
            authorizer.setHeaders(headers);
            options.setAuthorizer(authorizer);
        }

		options.setCluster("eu");
		pusher = new Pusher(key, options);
	}

	@Kroll.method
	public TiPusherChannelProxy subscribe(String channelName)
	{
        PrivateChannel channel = pusher.subscribePrivate(channelName);

        TiPusherChannelProxy channelProxy = new TiPusherChannelProxy();
		channelProxy.setChannel(channel);

		return channelProxy;
	}

	@Kroll.method
	public void connect() {
		pusher.connect(new ConnectionEventListener() {
			@Override
			public void onConnectionStateChange(ConnectionStateChange change) {
				KrollDict dict = new KrollDict();
				dict.put("old", change.getPreviousState().ordinal());
				dict.put("new", change.getCurrentState().ordinal());

				fireEvent("connectionchange", dict);

				Log.d(LCAT, "State changed to " + change.getCurrentState() +
						" from " + change.getPreviousState());
			}

			@Override
			public void onError(String message, String code, Exception e) {
				KrollDict dict = new KrollDict();
				dict.put("error", message);
				dict.put("code", code);

				fireEvent("error", dict);
				Log.e(LCAT, "There was a problem connecting:" + message + "(" + code + ")");
			}
		});
	}

	@Kroll.method
	public void disconnect() {
		if (pusher != null) {
			pusher.disconnect();
		}
	}
}

