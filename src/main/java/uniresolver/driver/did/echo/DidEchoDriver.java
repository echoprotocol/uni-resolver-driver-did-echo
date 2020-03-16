package uniresolver.driver.did.echo;

import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import did.Authentication;
import did.DIDDocument;
import did.JsonLdObject;
import did.PublicKey;
import did.Service;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

public class DidEchoDriver implements Driver {

    private static Logger log = LoggerFactory.getLogger(DidEchoDriver.class);

    public static final Pattern DID_ECHO_PATTERN_METHOD = Pattern.compile("^did:echo:[0|1].(.*)$");
    public static final Pattern DID_ECHO_PATTERN_METHOD_OBJECT_ID = Pattern.compile("^[1].\\d+.\\d+$");

    private Map<String, Object> properties = new HashMap<String, Object> ();

    public DidEchoDriver() {
        try {
			String envRpcUrl = System.getenv("DID_ECHO_DRIVER_RPC_URL");

			if (envRpcUrl != null) {
                properties.put("rpcUrl", envRpcUrl);
            } else {
                properties.put("rpcUrl", "http://localhost:80");
            }

            log.info("Loading from environment: " + properties.get("rpcUrl"));
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
    }

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {
        Matcher matcher = DID_ECHO_PATTERN_METHOD.matcher(identifier);
		if (!matcher.matches()) {
            log.info("DID method doesn't match pattern!");
            throw new ResolutionException("DID method doesn't match pattern!");
        }

        String objectId = matcher.group(1);
        matcher = DID_ECHO_PATTERN_METHOD_OBJECT_ID.matcher(objectId);
        if (!matcher.matches()) {
            log.info("DID method doesn't match pattern!");
            throw new ResolutionException("DID method doesn't match pattern!");
        }
        String getObjectString = "{\"jsonrpc\": \"2.0\", \"method\": \"get_objects\", \"params\": [[\"" + objectId + "\"]], \"id\": 1}";
        log.info("JSON Request: " + getObjectString);

        try {
            URL url = new URL(String.valueOf(properties.get("rpcUrl")));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.connect();

            OutputStream os = connection.getOutputStream();
            os.write(getObjectString.getBytes());
            os.flush();

            String response = "";
            BufferedReader br = null;
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String strCurrentLine;
            while ((strCurrentLine = br.readLine()) != null) {
                response += strCurrentLine;
            }

            log.info("JSON response: " + response);

            JsonParser parser = new JsonParser();
            JsonObject resp = (JsonObject) parser.parse(new StringReader(response));

            log.info("RPC resp: " + resp.toString());

        } catch (java.net.MalformedURLException e) {
            throw new ResolutionException("Bad url of node! " + e.getMessage());
        } catch (java.net.ProtocolException e) {
            throw new ResolutionException("Protocol exception! " + e.getMessage());
        } catch (java.io.IOException e) {
            throw new ResolutionException("IO exception! " + e.getMessage());
        }
        
        return null;
    }

	@Override
	public Map<String, Object> properties() {
		return properties;
	}
}