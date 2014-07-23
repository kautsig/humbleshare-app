package org.kautsig.humbleshare;

import java.io.InputStream;
import java.security.KeyStore;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.content.Context;

/**
 * Tries to load a keystore for https. If the keystore is not found, it falls
 * back to default (probably secure) behavior and will reject self signed
 * certificates.
 */
public class TrustingHttpClient extends DefaultHttpClient {

	/** The application context. */
	final Context context;

	/** The dynamically determined resource id of the truststore. */
	private int resourceId;

	/**
	 * Constructor.
	 *
	 * @param context
	 *            the application context.
	 */
	public TrustingHttpClient(Context context) {
		this.context = context;
		this.resourceId = context.getResources().getIdentifier(
				"org.kautsig.humbleshare:raw/mystore", null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ClientConnectionManager createClientConnectionManager() {
		if (resourceId != 0) {
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", createSslSocketFactory(), 443));
			registry.register(new Scheme("https", createSslSocketFactory(),
					8443));
			return new SingleClientConnManager(getParams(), registry);
		} else {
			return super.createClientConnectionManager();
		}
	}

	/**
	 * Creates a new ssl socket factory. If a custom truststore was found
	 * compiled to the app it's considered. Otherwise it falls back to default
	 * behavior.
	 *
	 * @return the ssl socket factory
	 */
	private SSLSocketFactory createSslSocketFactory() {
		try {
			KeyStore trusted = KeyStore.getInstance("BKS");
			InputStream in = context.getResources().openRawResource(resourceId);
			try {
				trusted.load(in, "ez24get".toCharArray());
			} finally {
				in.close();
			}
			return new SSLSocketFactory(trusted);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
