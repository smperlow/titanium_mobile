/**
 * 
 */
package org.appcelerator.titanium.proxy;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollInvocation;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.kroll.KrollCallback;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.titanium.util.TiActivitySupportHelper;
import org.appcelerator.titanium.util.TiConfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

@Kroll.proxy
public class ActivityProxy extends KrollProxy
	implements TiActivityResultHandler
{
	private static final String TAG = "ActivityProxy";
	private static boolean DBG = TiConfig.LOGD;
		
	protected Activity activity;
	protected IntentProxy intentProxy;
	protected KrollCallback resultCallback;
	
	public ActivityProxy(TiContext tiContext) {
		super(tiContext);
	}
	
	public ActivityProxy(TiContext tiContext, Activity activity) {
		this(tiContext);
		this.activity = activity;
		
		Intent intent = activity.getIntent();
		if (intent != null) {
			intentProxy = new IntentProxy(tiContext, activity.getIntent());
		}
	}
	
	protected Activity getActivity(KrollInvocation invocation) {
		Activity activity = this.activity;
		if (activity != null) return activity;
		
		if (invocation != null) {
			activity = invocation.getTiContext().getActivity();
			if (activity != null) return activity;
		}
		
		activity = getTiContext().getActivity();
		if (activity != null) return activity;
		
		activity = getTiContext().getRootActivity();
		if (activity != null) return activity;
		
		return null;
	}
	
	@Kroll.method
	public void startActivity(KrollInvocation invocation, IntentProxy intent) {
		Activity activity = getActivity(invocation);
		if (activity != null) {
			activity.startActivity(intent.getIntent());
		}
	}
	
	@Kroll.method
	public void startActivityForResult(KrollInvocation invocation, IntentProxy intent, KrollCallback callback) {
		Activity activity = getActivity(invocation);
		if (activity != null) {
			TiActivitySupport support = null;
			if (activity instanceof TiActivitySupport) {
				support = (TiActivitySupport)activity;
			} else {
				support = new TiActivitySupportHelper(activity);
			}
			
			this.resultCallback = callback;
			int requestCode = support.getUniqueResultCode();
			support.launchActivityForResult(intent.getIntent(), requestCode, this);
		}
	}
	
	@Kroll.method
	public void startActivityFromChild(KrollInvocation invocation, ActivityProxy child, IntentProxy intent, int requestCode) {
		Activity activity = getActivity(invocation);
		if (activity != null) {
			activity.startActivityFromChild(child.getActivity(), intent.getIntent(), requestCode);
		}
	}
	
	@Kroll.method
	public boolean startActivityIfNeeded(KrollInvocation invocation, IntentProxy intent, int requestCode) {
		Activity activity = getActivity(invocation);
		if (activity != null) {
			return activity.startActivityIfNeeded(intent.getIntent(), requestCode);
		}
		return false;
	}
	
	@Kroll.method
	public boolean startNextMatchingActivity(KrollInvocation invocation, IntentProxy intent) {
		Activity activity = getActivity(invocation);
		if (activity != null) {
			return activity.startNextMatchingActivity(intent.getIntent());
		}
		return false;
	}
	
	@Kroll.method
	public String getString(KrollInvocation invocation, int resId, Object[] formatArgs) {
		Activity activity = getActivity(invocation);
		if (activity != null) {
			if (formatArgs == null || formatArgs.length == 0) {
				return activity.getString(resId);
			} else {
				return activity.getString(resId, formatArgs);
			}
		}
		return null;
	}
	
	@Kroll.method @Kroll.getProperty
	public IntentProxy getIntent() {
		return intentProxy;
	}
	
	@Kroll.method @Kroll.setProperty
	public void setRequestedOrientation(KrollInvocation invocation, int orientation) {
		Activity activity = getActivity(invocation);
		if (activity != null) {
			activity.setRequestedOrientation(orientation);
		}
	}
	
	@Override
	public void onResult(Activity activity, int requestCode, int resultCode, Intent data) {
		if (resultCallback == null) return;
		
		KrollDict event = new KrollDict();
		event.put("requestCode", requestCode);
		event.put("resultCode", resultCode);
		event.put("intent", new IntentProxy(getTiContext(), data));
		event.put("source", this);
		resultCallback.call(event);
	}
	
	@Override
	public void onError(Activity activity, int requestCode, Exception e) {
		if (resultCallback == null) return;
		
		KrollDict event = new KrollDict();
		event.put("requestCode", requestCode);
		event.put("error", e.getMessage());
		event.put("source", this);
		resultCallback.call(event);
	}
	
	public Context getContext() {
		if (activity == null) {
			return getTiContext().getActivity().getApplication();
		}
		return activity;
	}
	
	public Activity getActivity() {
		return activity;
	}
	
	public void release() {
		activity = null;
	}
}
