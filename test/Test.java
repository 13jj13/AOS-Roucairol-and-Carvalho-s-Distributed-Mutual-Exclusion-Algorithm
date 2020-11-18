import java.io.*;
import java.util.*;

class Test {
    
    public static void main(String[] args) throws Exception {
        try {
            
	    // Open file to read test results
	    File file = new File("test_results.txt");
	    Scanner results = new Scanner(file); 

 	    // While there are still lines to read in the file
	    while(results.hasNextLine())
	    {
		 // Get next line
		String result = results.nextLine();
		 // If a process reported an overlap, it will contain word FAIL in it
		if(result.contains("FAIL")) {
			// Result of test is that there was CS overlap
			System.out.println("Test result: Critical sections overlap.");
			results.close(); 
			return;
		}

		
	    }
	    // All lines have been read and no process reported overlap so no CS overlap occurred. 
	    System.out.println("Test result: Critical sections do NOT overlap.");
	    results.close(); 
	    
          } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
          }
    }
}
