import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidJSONParameters;
import org.hyperledger.indy.sdk.did.DidResults.CreateAndStoreMyDidResult;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONObject;
import utils.PoolUtils;
import java.util.Scanner;

import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.anoncreds.CredentialsSearchForProofReq;
import org.json.JSONArray;

import static org.hyperledger.indy.sdk.anoncreds.Anoncreds.*;
import static org.hyperledger.indy.sdk.ledger.Ledger.buildNymRequest;
import static org.hyperledger.indy.sdk.ledger.Ledger.signAndSubmitRequest;
import static org.junit.Assert.assertEquals;
import static utils.PoolUtils.PROTOCOL_VERSION;


class Holder {

	private Pool pool;
	String poolName;
	private Wallet wallet;
	String myWalletConfig;
	String myWalletCredentials;
	String trusteeSeed = "000000000000000000000000Trustee1";
	String my_Did;
	String my_Verkey;
	String masterSecretId;
	Scanner sc = new Scanner(System.in);

	public Holder() throws Exception{
		System.out.println("start Ledger");

		Pool.setProtocolVersion(PROTOCOL_VERSION).get();

		this.poolName = PoolUtils.createPoolLedgerConfig();
		this.pool = Pool.openPoolLedger(poolName, "{}").get();
	}

	public void create_wallet() throws Exception{
		System.out.println("Create Wallet");
		System.out.println("Wallet ID: ");
		String my_wallet = sc.next();
		System.out.println("Wallet KEY: ");
		String my_wallet_key = sc.next();
		this.myWalletConfig = new JSONObject().put("id", my_wallet).toString();
		this.myWalletCredentials = new JSONObject().put("key", my_wallet_key).toString();
		Wallet.createWallet(this.myWalletConfig, this.myWalletCredentials).get();
		this.wallet = Wallet.openWallet(this.myWalletConfig, this.myWalletCredentials).get();
		System.out.println(this.wallet);
	}

	public void create_did() throws Exception{
		System.out.println("Create DID");
		CreateAndStoreMyDidResult createMyDidResult = Did.createAndStoreMyDid(this.wallet, "{}").get();
		this.my_Did = createMyDidResult.getDid();
		this.my_Verkey = createMyDidResult.getVerkey();

		DidJSONParameters.CreateAndStoreMyDidJSONParameter theirDidJson =
				new DidJSONParameters.CreateAndStoreMyDidJSONParameter(null, this.trusteeSeed, null, null);

		CreateAndStoreMyDidResult createTheirDidResult = Did.createAndStoreMyDid(this.wallet, theirDidJson.toJson()).get();
		String trusteeDid = createTheirDidResult.getDid();

		String nymRequest = buildNymRequest(trusteeDid, this.my_Did, this.my_Verkey, null, null).get();

		String nymResponseJson = signAndSubmitRequest(pool, this.wallet, trusteeDid, nymRequest).get();

		JSONObject nymResponse = new JSONObject(nymResponseJson);

		assertEquals(this.my_Did, nymResponse.getJSONObject("result").getJSONObject("txn").getJSONObject("data").getString("dest"));
		assertEquals(this.my_Verkey, nymResponse.getJSONObject("result").getJSONObject("txn").getJSONObject("data").getString("verkey"));
	}

	public void cred_offer_req() throws Exception {
		this.masterSecretId = proverCreateMasterSecret(this.wallet, null).get();
		// connection -> offer
	}

	public void cred_req() throws Exception {
		// connection -> offer
		// indy-pool -> credDefJson request
		// AnoncredsResults.ProverCreateCredentialRequestResult createCredReqResult =
		//		proverCreateCredentialReq(this.wallet, this.my_Did, credOffer, credDefJson, this.masterSecretId).get();
		// String credReqJson = createCredReqResult.getCredentialRequestJson();
		// String credReqMetadataJson = createCredReqResult.getCredentialRequestMetadataJson();
	}

	public void store_cred() throws Exception {
		// connection -> credential
		// proverStoreCredential(this.wallet, null, credReqMetadataJson, credential, credDefJson, null).get();
	}

	public void search_cred() throws Exception {
		// connection -> proofRequestJson
		// CredentialsSearchForProofReq credentialsSearch = CredentialsSearchForProofReq.open(this.wallet, proofRequestJson, null).get();

		// JSONArray credentialsForAttribute1 = new JSONArray(credentialsSearch.fetchNextCredentials("attr1_referent", 100).get());
		// String credentialIdForAttribute1 = credentialsForAttribute1.getJSONObject(0).getJSONObject("cred_info").getString("referent");

		// JSONArray credentialsForAttribute2 = new JSONArray(credentialsSearch.fetchNextCredentials("attr2_referent", 100).get());
		// String credentialIdForAttribute2 = credentialsForAttribute2.getJSONObject(0).getJSONObject("cred_info").getString("referent");

		// JSONArray credentialsForAttribute3 = new JSONArray(credentialsSearch.fetchNextCredentials("attr3_referent", 100).get());
		// assertEquals(0, credentialsForAttribute3.length());

		// JSONArray credentialsForPredicate = new JSONArray(credentialsSearch.fetchNextCredentials("predicate1_referent", 100).get());
		// String credentialIdForPredicate = credentialsForPredicate.getJSONObject(0).getJSONObject("cred_info").getString("referent");

		// credentialsSearch.close();
	}

	public void create_proof() throws Exception {
		// String selfAttestedValue = "8-800-300";
		// String requestedCredentialsJson = new JSONObject()
		// 		.put("self_attested_attributes", new JSONObject().put("attr3_referent", selfAttestedValue))
		// 		.put("requested_attributes", new JSONObject()
		//				.put("attr1_referent", new JSONObject()
		//						.put("cred_id", credentialIdForAttribute1)
		//						.put("revealed", true)
		//				)
		//				.put("attr2_referent", new JSONObject()
		//						.put("cred_id", credentialIdForAttribute2)
		//						.put("revealed", false)
		//				)
		//		)
		//		.put("requested_predicates", new JSONObject()
		//				.put("predicate1_referent", new JSONObject()
		//						.put("cred_id",credentialIdForPredicate)
		//				)
		//		)
		//		.toString();

		// String schemas = new JSONObject().put(schemaId, new JSONObject(schemaJson)).toString();
		// String credentialDefs = new JSONObject().put(credDefId,  new JSONObject(credDefJson)).toString();
		// String revocStates = new JSONObject().toString();

		// String proofJson = "";
		// try {
		// 	proofJson = proverCreateProof(proverWallet, proofRequestJson, requestedCredentialsJson,
		// 			masterSecretId, schemas, credentialDefs, revocStates).get();
		// } catch (Exception e){
		// 	System.out.println("");
		// }

		// JSONObject proof = new JSONObject(proofJson);

	}

	public void close_wallet() throws Exception {
		this.wallet.closeWallet().get();
		Wallet.deleteWallet(this.myWalletConfig, this.myWalletCredentials).get();
		System.out.println("Close Wallet");
	}

	public void close_pool() throws Exception {
		this.pool.closePoolLedger().get();
		Pool.deletePoolLedgerConfig(poolName).get();
		System.out.println("Close Ledger");
	}

}
