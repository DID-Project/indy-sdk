import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws Exception {
		
		Issuer issuer = new Issuer();
		Scanner sc = new Scanner(System.in);
		issuer.create_wallet();
		issuer.create_did();
		issuer.create_schema();
		issuer.create_cred_def();
		issuer.create_cred_offer();
		issuer.close_wallet();
		issuer.close_pool();
		// Ledger.close_pool();
		// System.out.println(pool);

		while(true){
		try {
			//printMenu();
			int num = sc.nextInt();
    
			switch (num) {
			case 1:
				System.out.print("name : ");
			case 2:
				System.out.println("exit");
				System.exit(0);
				break;
			default:
				throw new Exception();
			}
		}catch(Exception e) {
			System.out.println("\n");
		}
		}
	}
	//public static void printMenu(){
	//	System.out.println("Issuer");
	//	System.out.println("1. DID");
	//	System.out.println("2. Schema");
	//	System.out.println("3. Credential Definition");
	//	System.out.println("4. Credential");
	//	System.out.println("5. message");
	//	System.out.println("test");
	//	System.out.print("input : ");
	// }
}
