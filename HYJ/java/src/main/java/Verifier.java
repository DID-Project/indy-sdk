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


class Verifier {

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

	public Verifier() throws Exception{
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

	public void proof_req() throws Exception {
		String nonce = generateNonce().get();
		String proofRequestJson = new JSONObject()
				.put("nonce", nonce)
				.put("name", "proof_req_1")
				.put("version", "0.1")
				.put("requested_attributes", new JSONObject()
						.put("attr1_referent", new JSONObject().put("name", "name"))
						.put("attr2_referent", new JSONObject().put("name", "sex"))
						.put("attr3_referent", new JSONObject().put("name", "phone"))
				)
				.put("requested_predicates", new JSONObject()
						.put("predicate1_referent", new JSONObject()
								.put("name", "age")
								.put("p_type", ">=")
								.put("p_value", 18)
						)
				)
				.toString();
	}

	public void verify_proof() throws Exception {
		// JSONObject revealedAttr1 = proof.getJSONObject("requested_proof").getJSONObject("revealed_attrs").getJSONObject("attr1_referent");
		// assertEquals("Alex", revealedAttr1.getString("raw"));

		// assertNotNull(proof.getJSONObject("requested_proof").getJSONObject("unrevealed_attrs").getJSONObject("attr2_referent").getInt("sub_proof_index"));

		// assertEquals(selfAttestedValue, proof.getJSONObject("requested_proof").getJSONObject("self_attested_attrs").getString("attr3_referent"));

		// String revocRegDefs = new JSONObject().toString();
		// String revocRegs = new JSONObject().toString();

		// Boolean valid = verifierVerifyProof(proofRequestJson, proofJson, schemas, credentialDefs, revocRegDefs, revocRegs).get();
		// assertTrue(valid);
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
