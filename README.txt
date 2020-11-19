Name: Jennifer Ward, Evelyn Wong
Project: 2
  
To compile: 
	Run command "javac *.java" in bin folder of dcxx machine.
  Run command "javac *.java" in test folder of dcxx machine. 
To run program:
	Run command "./launcher.sh" in launch folder of local machine.
To test program:
  Run command "java Test" in test folder of dcxx machine.
To evaluate program:
  Run command "java Evaluation <directory of log files>" in test folder of dcxx machine. 
  i.e. java Evaluation ./10-20-10

File stucture:
	On dcxx machines:
		Project2 folder includes bin, launch, and test folders as layed out below.
	On local machine:
		Project2 folder includes bin and launch folders as layed out below. 
	Main is in Application.java. Test.java and Evaluation.java are run separately from program.
		
	Project2\
		bin\
			Application.java
      ConfigFileInfo.java
			Message.java
			Node.java
      NeighborNode.java
      RCMutualExclusionService.java
		launch\
			cleanup.sh
			config.txt
			launcher.sh
    test\
      Test.java
      Evaluation.java
