package ru.shemplo.megaplan.network;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.digest.HmacAlgorithms.HMAC_SHA_1;
import static ru.shemplo.megaplan.network.RequestAction.AUTH_REQUEST;
import static ru.shemplo.megaplan.updater.MegaplanAPIManager.CLIENT_HOST;
import static ru.shemplo.megaplan.updater.MegaplanAPIManager.SERVER_PROTOCOL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import ru.shemplo.exception.AuthorizationException;
import ru.shemplo.exception.RequestException;
import ru.shemplo.support.Pair;

public class APIConnection {

	private static final DateFormat 
		DATE_FORMAT = new SimpleDateFormat ("EEE, d MMM yyyy HH:mm:ss Z", 
											Locale.ENGLISH);

	@SuppressWarnings ("unused")
	private static String SECRET_KEY, ACCESS_ID, LOGIN;
	private static boolean authorized = false;
	private static HmacUtils HASH_GENERATOR;

	public static boolean isAuthorized () {
		return authorized;
	}

	/* -===| AUTHORIZATION SECTION |===- */

	public static void authorize (String login, String password) throws AuthorizationException {
		if (password == null) { throw new AuthorizationException ("Password is null"); }
		if (login == null) { throw new AuthorizationException ("Login is null"); }

		if (isAuthorized () && login.equals (LOGIN)) { return; } // Already authorized
		password = hashMD5 (password);

		String url = SERVER_PROTOCOL + CLIENT_HOST + AUTH_REQUEST.URI;
		HttpPost postRequestUrl = new HttpPost (url);

		List <NameValuePair> params = new ArrayList <> ();
		params.add (new BasicNameValuePair ("Password", password));
		params.add (new BasicNameValuePair ("Login", login));

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity (params, Consts.UTF_8);
		postRequestUrl.setEntity (entity);

		ResponseHandler <Pair <String, String>> responseHandler = r -> {
			StatusLine status = r.getStatusLine ();
			if (status.getStatusCode () >= 300) {
				String message = ("(" + status.getStatusCode () + ") " + status.getReasonPhrase ());
				return Pair.make ("Error", message);
			}

			HttpEntity content = r.getEntity ();
			if (content == null) { return Pair.make ("Error", "(Resconse) Empty conten"); }

			InputStream is = content.getContent ();
			Reader reader = new InputStreamReader (is, UTF_8);
			BufferedReader br = new BufferedReader (reader);

			StringBuilder sb = new StringBuilder ();
			String s;
			while ((s = br.readLine ()) != null) {
				sb.append (s);
			}

			JSONObject jRoot = new JSONObject (sb.toString ());
			JSONObject jStatus = jRoot.getJSONObject ("status");
			String jCode = jStatus.getString ("code");
			if (!"ok".equals (jCode)) { return Pair.make ("Error", jStatus.getString ("message")); }
			
			JSONObject jData = jRoot.getJSONObject ("data");
			return Pair.make (jData.getString ("AccessId"), jData.getString ("SecretKey"));
		};

		try (CloseableHttpClient client = HttpClients.createDefault ();) {
			Pair <String, String> tokens = client.execute (postRequestUrl, responseHandler);
			if (tokens.f.equals ("Error")) { // Like an exception
				throw new AuthorizationException (tokens.s);
			}

			HASH_GENERATOR = new HmacUtils (HMAC_SHA_1, tokens.s);
			APIConnection.authorized = true;
			ACCESS_ID = tokens.f;
			LOGIN = login;
			
			// Done
		} catch (ClientProtocolException cpe) {
			String message = "(Protocol) Bad protocol used";
			throw new AuthorizationException (message, cpe);
		} catch (IOException ioe) {
			String message = "(I/O) Failed to read from response";
			throw new AuthorizationException (message, ioe);
		}
	}

	public static void authorize () throws AuthorizationException {
		String login = null;
		try {
			login = System.getProperty ("megaplan.api.login");
		} catch (SecurityException se) {
			String message = "(Security) failed get login";
			throw new AuthorizationException (message, se);
		} catch (IllegalArgumentException iae) {
			String message = "(Argument) Property with login isn't declared";
			throw new AuthorizationException (message, iae);
		}

		String password = null;
		try {
			password = System.getProperty ("megaplan.api.password");
		} catch (SecurityException se) {
			String message = "(Security) failed get password";
			throw new AuthorizationException (message, se);
		} catch (IllegalArgumentException iae) {
			String message = "(Argument) Property with password isn't declared";
			throw new AuthorizationException (message, iae);
		}

		authorize (login, password);
	}

	/* -===| REQUESTS SECTION |===- */

	private static enum RequestMethod {
		GET, POST
	}

	public static JSONObject sendRequest (RequestAction action, List <Pair <String, ?>> params)
			throws RequestException, NullPointerException {
		Objects.requireNonNull (action, "Given request action is null");

		if (!isAuthorized ()) {
			String message = "Not authorized";
			throw new RequestException (message);
		}

		Date date = new Date (); // Date fixation for header params
		String signature = generateSignature (RequestMethod.POST, date, action);
		String signatureHash = hashSignature (signature);

		String url = SERVER_PROTOCOL + CLIENT_HOST + action.URI;
		HttpPost postRequestUrl = new HttpPost (url);

		postRequestUrl.addHeader ("Content-Type", "application/x-www-form-urlencoded");
		postRequestUrl.addHeader ("X-Authorization", ACCESS_ID + ":" + signatureHash);
		postRequestUrl.addHeader ("Date", DATE_FORMAT.format (date));
		postRequestUrl.addHeader ("Accept", "application/json");

		if (params != null) {
			List <NameValuePair> namedParams = generateParamsList (params);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity (namedParams, Consts.UTF_8);
			postRequestUrl.setEntity (entity);
		}

		ResponseHandler <JSONObject> responseHandler = r -> {
			StatusLine status = r.getStatusLine ();
			if (status.getStatusCode () >= 300) {
				String message = ("(" + status.getStatusCode () + ") " + status.getReasonPhrase ());
				System.err.println (message);
				return null;
			}

			HttpEntity content = r.getEntity ();
			if (content == null) { return null; }

			InputStream is = content.getContent ();
			Reader reader = new InputStreamReader (is, UTF_8);
			BufferedReader br = new BufferedReader (reader);

			StringBuilder sb = new StringBuilder ();
			String s;
			while ((s = br.readLine ()) != null) {
				sb.append (s);
			}

			return new JSONObject (sb.toString ());
		};

		try (CloseableHttpClient client = HttpClients.createDefault ();) {
			JSONObject answer = client.execute (postRequestUrl, responseHandler);
			if (answer == null) {
				String message = "(Response) unhandled exception";
				throw new RequestException (message);
			}

			return answer;
		} catch (ClientProtocolException cpe) {
			String message = "(Protocol) Bad protocol used";
			throw new RequestException (message, cpe);
		} catch (IOException ioe) {
			String message = "(I/O) Failed to read from response";
			throw new RequestException (message, ioe);
		}
	}

	// Trusted arguments (each argument != null)
	private static String generateSignature (RequestMethod method, Date date, RequestAction action) {
		StringBuilder sb = new StringBuilder ();
		sb.append (method);
		sb.append ("\n\n");
		sb.append ("application/x-www-form-urlencoded\n");
		sb.append (DATE_FORMAT.format (date));
		sb.append ("\n");
		sb.append (CLIENT_HOST);
		sb.append (action.URI);
		return sb.toString ();
	}

	// Trusted arguments (each argument != null)
	private static List <NameValuePair> generateParamsList (List <Pair <String, ?>> params) {
		List <NameValuePair> list = new ArrayList <> ();
		for (Pair <String, ?> param : params) {
			list.add (new BasicNameValuePair (param.f, param.s.toString ()));
		}

		return list;
	}

	/* -===| CRYPTO |===- */

	// Trusted arguments (each argument != null)
	private static String hashSignature (String signature) {
		String hashHex = HASH_GENERATOR.hmacHex (signature.toString ());
		return Base64.encodeBase64String (hashHex.getBytes (UTF_8));
	}

	private static MessageDigest digestForMD5;

	// Trusted arguments (each argument != null)
	public static String hashMD5 (String string) {
		if (digestForMD5 == null) {
			try {
				digestForMD5 = MessageDigest.getInstance ("MD5");
			} catch (NoSuchAlgorithmException nsme) {}
		}

		byte [] rawString = string.getBytes (UTF_8);
		digestForMD5.update (rawString);

		byte [] rawHash = digestForMD5.digest ();
		StringBuffer sb = new StringBuffer ();

		for (int i = 0; i < rawHash.length; ++i) {
			sb.append (Integer.toHexString ((rawHash [i] & 0xFF) | 0x100).substring (1, 3));
		}

		return sb.toString ();
	}

}
