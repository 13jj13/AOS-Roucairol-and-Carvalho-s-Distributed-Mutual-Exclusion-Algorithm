import java.io.*;
import java.util.*;

class Test {
    
    public static void main(String[] args) throws Exception {
        try {
            
	    File file = new File("test_results.txt");
	    Scanner results = new Scanner(file); 

	    while(results.hasNextLine())
	    {
		String result = results.nextLine();
		if(result.contains("FAIL")) {
			System.out.println("Test result: Critical sections overlap.");
			results.close(); 
			return;
		}

		
	    }
	    System.out.println("Test result: Critical sections do NOT overlap.");
	    results.close(); 
	    
          } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
          }
    }
}
