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


class Issuer {

	private Pool pool;
	String poolName;
	private Wallet wallet;
	String myWalletConfig;
	String myWalletCredentials;
	String trusteeSeed = "000000000000000000000000Trustee1";
	String my_Did;
	String my_Verkey;
	String schemaId;
	String schemaJson;
	String credDefId;
	String credDefJson;
	String credOffer;
	Scanner sc = new Scanner(System.in);

	public Issuer() throws Exception{
		System.out.println("start Ledger");

		Pool.setProtocolVersion(PROTOCOL_VERSION).get();

		this.poolName = PoolUtils.createPoolLedgerConfig();
		this.pool = Pool.openPoolLedger(poolName, "{}").get();
	}

	public void create_wallet() throws Exception{
		// wallet storage: https://hyperledger-indy.readthedocs.io/projects/sdk/en/latest/docs/design/003-wallet-storage/README.html
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

	public void create_schema() throws Exception{
		System.out.println("Schema Name: ");
		String schemaName = sc.next();
		String schemaVersion = "1.0";
		String schemaAttributes = new JSONArray().put("name").put("age").put("sex").put("height").toString();
		AnoncredsResults.IssuerCreateSchemaResult createSchemaResult =
				issuerCreateSchema(this.my_Did, schemaName, schemaVersion, schemaAttributes).get();
		this.schemaId = createSchemaResult.getSchemaId();
		this.schemaJson = createSchemaResult.getSchemaJson();
	}

	public void create_cred_def() throws Exception{
		System.out.println("Credential Definition Name: ");
		String credDefTag = sc.next();
		String credDefConfigJson = new JSONObject().put("support_revocation", false).toString();
		AnoncredsResults.IssuerCreateAndStoreCredentialDefResult createCredDefResult =
				issuerCreateAndStoreCredentialDef(this.wallet, this.my_Did, this.schemaJson, credDefTag, null, credDefConfigJson).get();
		this.credDefId = createCredDefResult.getCredDefId();
		this.credDefJson = createCredDefResult.getCredDefJson();
	}

	public void rev_reg_def() throws Exception{
		// String revRegDefConfig = new JSONObject()
		// 		.put("issuance_type", "ISSUANCE_ON_DEMAND")
		// 		.put("max_cred_num", 5)
		// 		.toString();
		// String tailsWriterConfig = new JSONObject()
		// 		.put("base_dir", getIndyHomePath("tails").replace('\\', '/'))
		// 		.put("uri_pattern", "")
		// 		.toString();
		// BlobStorageWriter tailsWriter = BlobStorageWriter.openWriter("default", tailsWriterConfig).get();

		// String revRegDefTag = "Tag2";
		// AnoncredsResults.IssuerCreateAndStoreRevocRegResult createRevRegResult =
		// 		issuerCreateAndStoreRevocReg(issuerWallet, issuerDid, null, revRegDefTag, credDefId, revRegDefConfig, tailsWriter).get();
		// String revRegId = createRevRegResult.getRevRegId();
		// String revRegDefJson = createRevRegResult.getRevRegDefJson();
	}

	public void create_cred_offer() throws Exception{
		this.credOffer = issuerCreateCredentialOffer(this.wallet, this.credDefId).get();
		System.out.println(this.credOffer);
		// connection -> create_cred_offer
	}

	public void create_cred() throws Exception{
		// connection -> cred_req -> create_cred
		// DID <-> ID Database
		String credValuesJson = new JSONObject()
				.put("sex", new JSONObject().put("raw", "male").put("encoded", "594465709955896723921094925839488742869205008160769251991705001"))
				.put("name", new JSONObject().put("raw", "Alex").put("encoded", "1139481716457488690172217916278103335"))
				.put("height", new JSONObject().put("raw", "175").put("encoded", "175"))
				.put("age", new JSONObject().put("raw", "28").put("encoded", "28"))
		.toString();

		// AnoncredsResults.IssuerCreateCredentialResult createCredentialResult =
		// 		issuerCreateCredential(this.wallet, this.credOffer, credReqJson, credValuesJson, null, - 1).get();
		// String credential = createCredentialResult.getCredentialJson();
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
