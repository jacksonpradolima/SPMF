# SPMF
[An Open-Source Data Mining Library](http://www.philippe-fournier-viger.com/spmf/) - GitHub Fork

**NOTE** The [owner][jacksonpradolima] of this repository has no affiliation with official SPMF project. This repo is periodically updated as a kindness to others who
have shown interest in it.Therefore, this exists to provide an easy way to access and contribute with the project, nothing more.

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


# How to install

The project use Maven.

# How to run the test files:

Once you have installed the project you are ready for running the software. 

1. You should go to "ca.pfv.spmf.tests" folder. In this folder there are all the examples of how to use the software. All of these examples are  described in the documentation on the [website](http://www.philippe-fournier-viger.com/spmf/);
2. Let say that you want to run Example #9 on the website (the CHARM algorithm). This is the file **MainTestCharm_saveToMemory.java**;
3. To run it, you just "run the file";
4. That's it! You can modify the test files to take other files as input or change the parameters of the algorithms.

# Code Quality

It's available at sonarcloud (SonarQube) [here](https://sonarcloud.io/dashboard?id=ca.pfv%3ASPMF).

# License

The code is licensed under the open-source GNU GPL version 3 license.

# Citation

Fournier-Viger, P., Gomariz, A., Gueniche, T., Soltani, A., Wu., C., Tseng, V. S. (2014). SPMF: a Java Open-Source Pattern Mining Library. Journal of Machine Learning Research (JMLR), 15: 3389-3393.
