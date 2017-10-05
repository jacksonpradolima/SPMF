# SPMF
[An Open-Source Data Mining Library](http://www.philippe-fournier-viger.com/spmf/) - GitHub Fork

# About
(Direct from the website project)

SPMF is an open-source data mining mining library written in Java, specialized in pattern mining.

It is distributed under the GPL v3 license.

It offers implementations of 133 data mining algorithms for:
- association rule mining,
- itemset mining,
- sequential pattern mining,
- sequential rule mining,
- sequence prediction,
- periodic pattern mining, 
- high-utility pattern mining,
- clustering and classification,
- time-series mining.

The source code of each algorithm can be easily integrated in other Java software.

Moreover, SPMF can be used as a standalone program with a simple user interface or from the command line.

SPMF is fast and lightweight (no dependencies to other libraries).

The current version is v2.18 and was released the 6th August 2017. 

Informations:
- [Algorithms](http://www.philippe-fournier-viger.com/spmf/index.php?link=algorithms.php)
- [Documentation](http://www.philippe-fournier-viger.com/spmf/index.php?link=documentation.php)
- [Developers' guide](http://www.philippe-fournier-viger.com/spmf/developers.php)
- [Datasets](http://www.philippe-fournier-viger.com/spmf/index.php?link=datasets.php)
- [FAQ](http://www.philippe-fournier-viger.com/spmf/index.php?link=FAQ.php)
- [Other resources](http://www.philippe-fournier-viger.com/spmf/index.php?link=resources.php): Tutorials, Research Papers, and Related Projects.


# HOW TO INSTALL

## INSTRUCTIONS FOR COMPILING & RUNNING THE SOFTWARE

To run the examples,  you will need to compile the source code. Because there is a lot of files to compile, it is preferable to use a development environment such as Eclipse or Netbeans.

Below, I provide instructions for ECLIPSE and NETBEANS.

## COMPILING THE SOURCE CODE WITH ECLIPSE

How to import the source code in Eclipse, and compile the project:

1. Install Eclipse. Make sure that you have installed the 
latest version of the JAVA SDK on your machine.
2. Open Eclipse. Do "File / New / Java Project /"
3. Type the name of the project and then click on "Finish"
4 Then, right-click on the "src" folder of the Java project that 
you have created. Choose "Properties". Copy the name of the 
directory that is shown in the properties. It should be something 
 like that: "C:\eclipse\Workspace\test2\src"
 5 Go to this directory and then uncompress the file 
 "spmf.zip" that you have downloaded, so that the folder "ca" should appear directly in the folder "src".
 6. Go back into Eclipse.
7. Right-click on! the Java project and select "Refresh".
 8. This is it. The project is now installed.

How to run the test files:

 Once you have installed the project you are ready for running the 
 software. 
1. In Eclipse, you should go to "ca.pfv.spmf.tests" 
 folder, in your Java project. In this folder there are all the 
examples of how to use the software. All of these examples are  described on the website : (  http://www.philippe-fournier-viger.com/spmf/ )
2. Let say that you want to run Example #9 on the website (the 
 CHARM algorithm). This is the file ""MainTestCharm_saveToMemory.java""
3. To run it, you have to right click on MainTestCharm_saveToMemory.java" 
and then choose "Run as. / Java application".
 4. That's it! You can modify the test files to take other files 
 as input or change the parameters of the algorithms.


## COMPILING THE SOURCE CODE WITH NETBEANS

How to import the source code in Netbeans, and compile the project:

1. Install Netbeans. Make sure that you have installed the 
latest version of the JAVA SDK on your machine.
2. Open NetBeans. Do "File" > New project > Java Application > Next > 
3. Type the name of the project and then click on "Finish"
4. Then, right click on "Source packages" and go to "Properties". This will open a window showing the project path. The project path should be something like that: "C:\Users\ph\Documents\NetBeansProjects\JavaApplication1" 
5. Go to this directory and then uncompress the file 
 "spmf.zip" that you have downloaded, so that the folder "ca" should appear directly in the folder "src".
6. Go back into NetBeans.
7. This is it. The project is now installed.

How to run the test files:

Once you have installed the project you are ready for running the software. 
1. In NetBeans, you should go to "ca.pfv.spmf.tests" 
 folder, in your Java project. In this folder there are all the 
examples of how to use the software. All of these examples are  described in the documentation on the website : (  http://www.philippe-fournier-viger.com/spmf/ )
2. Let say that you want to run Example #9 on the website (the 
 CHARM algorithm). This is the file ""MainTestCharm_saveToMemory.java""
3. To run it, you have to right click on MainTestCharm_saveToMemory.java" 
and then choose "Run file".
 4. That's it! You can modify the test files to take other files 
 as input or change the parameters of the algorithms.

# License

The code is licensed under the open-source GNU GPL version 3 license.

# Citation

Fournier-Viger, P., Gomariz, A., Gueniche, T., Soltani, A., Wu., C., Tseng, V. S. (2014). SPMF: a Java Open-Source Pattern Mining Library. Journal of Machine Learning Research (JMLR), 15: 3389-3393.
