# AOS-Roucairol-and-Carvalhos-Distributed-Mutual-Exclusion-Algorithm

# 1 Project Description

Implement a mutual exclusion service among n processes using Roucairol and Carvalho’s distributed
mutual exclusion algorithm. Your service should provide two function calls to the application: 
csenter() and cs-leave(). The first function call cs-enter() allows an application to request permission
to start executing its critical section. The function call is blocking and returns only when the
invoking application can execute its critical section. The second function call cs-leave() allows an
application to inform the service that it has finished executing its critical section.

**Implementation Details:** Design your program so that each process or node consists of two
separate modules. The top module implements the application (requests and executes critical
sections). The bottom module implements the mutual exclusion service. The two modules interact
using cs-enter() and cs-leave() functions.

**Application:** The application is responsible for generating critical section requests and then
executing critical sections on receiving permission from the mutual exclusion service. Model your
application using the following two parameters: inter-request delay, denoted by d, and cs-execution
time, denoted by c. The first parameter denotes the time elapsed between when a node’s current
request is satisfied and when it generates the next request. The second parameter denotes the time
a node spends in its critical section. Assume that both inter-request delay and cs-execution time are
random variables with exponential probability distribution.

**Testing:** Design a mechanism to test the correctness of your implementation. Your testing 
mechanism should ascertain that at most one process is in its critical section at any time. It should
be as automated as possible and should require minimal human intervention. For example, visual
inspection of log files to verify the correctness of the execution will not be acceptable. You will be
graded on how accurate and automated your testing mechanism is.

Our plans:
The program will log the timestamps and process ID of each process that executes its critical section and the test will use these logs to check if there is any other process that also executed its critical section at the same time. This is done by checking if any process’s init-term timestamp pairs overlap with any other process’s init-term timestamp pairs.

**Experimental Evaluation:** Evaluate the performance of your implementation with respect to
message complexity, response time and system throughput using experiments for various values
of system parameters, namely n, d and c. Display the results of your experiments by plotting
appropriate graphs. Note that each point in the graph should be obtained by averaging over
multiple runs.

# 3 Configuration Format

Your program should run using a configuration file in the following format:

The configuration file will be a plain-text formatted file no more than 100KB in size. Only lines
which begin with an unsigned integer are considered to be valid. Lines which are not valid should
be ignored. The configuration file will contain n + 1 valid lines.

The first valid line of the configuration file contains four tokens. The first token is the number
of nodes in the system. The second token is the mean value for inter-request delay (in milliseconds).
The third token is the mean value for cs-execution time (in milliseconds). The fourth token is the
number of requests each node should generate.

After the first valid line, the next n lines consist of three tokens. The first token is the node
ID. The second token is the host-name of the machine on which the node runs. The third token is
the port on which the node listens for incoming connections.

Your parser should be written so as to be robust concerning leading and trailing white space or
extra lines at the beginning or end of file, as well as interleaved with valid lines. The # character
will denote a comment. On any valid line, any characters after a # character should be ignored.

Listing 1: Example configuration file

9 20 10 1000

0 dc02 1234

1 dc03 1233

2 dc06 5233

3 dc05 1232

4 dc10 1233

5 dc09 3455

6 dc15 6789

7 dc18 3467

8 dc23 2546

# Compiling code 
javac ./bin/*.java

# Run Algorithm
cd launch
./launch

