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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import did.DIDDocument;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

public class DidEchoDriver implements Driver {

    public String mainnetKey = "rpcUrlMainnet";

    public String testnetKey = "rpcUrlTestnet";

    public String defaultMainnet = "http://localhost:80";

    public String defaultTestnet = "http://localhost:80";

    public static final Pattern DID_ECHO_PATTERN_METHOD = Pattern.compile("^did:echo:[0|1].(.*)$");

    public static final Pattern DID_ECHO_PATTERN_METHOD_OBJECT_ID = Pattern.compile("^[1].\\d+.\\d+$");

    private static Logger log = LoggerFactory.getLogger(DidEchoDriver.class);

    private Map<String, Object> properties = new HashMap<String, Object> ();

    public DidEchoDriver() {
        try {
			String envRpcUrlMainnet = System.getenv("DID_ECHO_DRIVER_MAINNET_RPC_URL");
			String envRpcUrlTestnet = System.getenv("DID_ECHO_DRIVER_TESTNET_RPC_URL");

			if (envRpcUrlMainnet != null) {
                properties.put(mainnetKey, envRpcUrlMainnet);
            } else {
                properties.put(mainnetKey, defaultMainnet);
            }

			if (envRpcUrlTestnet != null) {
                properties.put(testnetKey, envRpcUrlTestnet);
            } else {
                properties.put(testnetKey, defaultTestnet);
            }

            log.info("Loading from environment:\tmainnet = " + properties.get(mainnetKey) + "\ttestnet = " + properties.get(testnetKey));
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
    }

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {
        Matcher matcher = DID_ECHO_PATTERN_METHOD.matcher(identifier);
		if (!matcher.matches()) {
            throw new ResolutionException("DID method doesn't match pattern!");
        }

        String typeOfNetwork = matcher.group(1);
        String objectId = matcher.group(2);
        matcher = DID_ECHO_PATTERN_METHOD_OBJECT_ID.matcher(objectId);
        if (!matcher.matches()) {
            throw new ResolutionException("DID method doesn't match pattern!");
        }
        String getObjectString = "{\"jsonrpc\": \"2.0\", \"method\": \"get_objects\", \"params\": [[\"" + objectId + "\"]], \"id\": 1}";
        log.info("JSON Request: " + getObjectString);

        ResolveResult resolveResult = null;

        try {
            URL url;
            if (typeOfNetwork == "0") {
                url = new URL(String.valueOf(properties.get(mainnetKey)));
            } else if (typeOfNetwork == "1") {
                url = new URL(String.valueOf(properties.get(testnetKey)));
            } else {
                throw new ResolutionException("Bad network type!");
            }
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

            resolveResult = ResolveResult.build(DIDDocument.fromJson(response));

        } catch (java.net.MalformedURLException e) {
            throw new ResolutionException("Bad url of node! " + e.getMessage());
        } catch (java.net.ProtocolException e) {
            throw new ResolutionException("Protocol exception! " + e.getMessage());
        } catch (java.io.IOException e) {
            throw new ResolutionException("IO exception! Can be invalid json response! " + e.getMessage());
        }
        
        return resolveResult;
    }

	@Override
	public Map<String, Object> properties() {
		return properties;
	}
}