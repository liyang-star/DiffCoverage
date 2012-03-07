# 工具介绍
* 一款针对C/C++的代码覆盖率分析统计工具;
* 基于[GCOV](http://gcc.gnu.org/onlinedocs/gcc/Gcov.html)，用于收集GCOV产生的覆盖率信息，并且使用HTML方式展现。
* 它其实和[lcov](http://ltp.sourceforge.net/coverage/lcov.php)类似，但lcov使用perl开发的，看到那变态的“$$$$$$$$”人就彻底崩溃了，完全不利于我们这种二次开发的需求。另外一方面，我看到eclipse推出的[Linux Tools](http://www.eclipse.org/linuxtools/)中有[gcov](http://www.eclipse.org/linuxtools/projectPages/gcov/)这一项，因此我就想利用它的API来帮助我解析gcc的覆盖率信息（这个是关键点）。只要有了这一信息，后面的操作无非就是对函数、分支、目录的统计。

# Features
* 支持根据差异信息（如svn diff得到的diff信息）来计算增量覆盖率信息
* 支持在统计信息中忽略任意的文件和目录
* 另一种计算函数、分支覆盖率的方式
* 在具体某一个文件的覆盖率展示方面，界面更为清爽，而且还具有代码高亮的

# 结果的展示
![目录的展示](http://qa.alibaba.com/wuliang/gallery/blog/c1.png)
![文件的展示](http://qa.alibaba.com/wuliang/gallery/blog/c2.png)

# 编译前的准备工作
因为这个工具还是基于gcov的那一套统计覆盖率的原理，因此 在编译选项中还是需要加上这三个变量选项：

    -g -ftest-coverage -fprofile-arcs

注意：

    原本的gcov其实并不要求加上 -g 参数，它是让用户通过 --object-directory 选项来指定cpp文件对应到哪一个目标文件（*.o文 件）来找到覆盖率信息的。
但我这边是批量计算各个文件的覆盖率信息，因此需要加上-g这 个参数，这样就可以通过解析目标文件（*.o文件）来得到cpp文件。这点大家一定得注意一下。

# 编译安装
Step 1: 安装本地的依赖包: 
    
    $ sh add-libs.sh

Step 2: Maven编译:

    $ mvn clean package
    
编译打包成功后，会在target目录下产生 `DiffCoverage.jar` 文件

# 运行参数

    java -jar DiffCoverage.jar
     --debug                                 : 打印debug日志，用于调试
     --diffFile diffFile                     : 如果计算增量覆盖率，需要通过这个参数指定diff文件
     --help                                  : 打印这个帮助信息
     --ignoreFile ignoreFile                 : 可以通过这个参数指定那些文件或者目录不要算在覆盖率统计信息中
     --isNormalDiffFormat                    : 由于svn diff的格式有两个信息，默认是Unified Diff Format;另外一种Normal格式
     --output output                         : 指定HTML目录的输出
     --projectPath projectPath               : 项目路径，默认是当 前目录
     --isIncrement                           : 如果是需要计算增量覆盖率，需要指定这个参数，因为默认是计算全量 覆盖率工具的
     --version                               : 输出当前版本

# 运行实例
#### 实例一：计算全量覆盖率

    $ java -jar DiffCoverage.jar --output /home/admin/web/xx --projectPath util/
这样就会把util这个目录下的覆盖率信息保存到/home/admin/web/xx中了

#### 实例二：计算增量覆盖率
    
    $ java -jar DiffCoverage.jar --projectPath . --output ~/xx4  --isIncrement --diffFile diff.txt
通过`--isIncrement`来指定说是我这次计算的是增量覆盖率 <br />
通过`--diffFile`来指定diff文件

#### 实例三:如果我在计算diff时想忽略空白行这些的改变，怎么办？
这个也比较好办，可以通过svn diff的扩展来diff时忽略红白行的改变：
    
    $ svn diff --diff-cmd '/usr/bin/diff' -x -w -x -b -x -B -x -u -r 87426 > diff.txt1
    
#### 实例四：如果想计算时忽略某些文件/目录，比如忽略开发的单元测试
这个首先编辑一个文件，文件名随便，文件的内容格式如下（[YAML](http://www.yaml.org/)格式）：
    
    # 需要忽略的文件，只要写文件名就可以，支持正则表达式
    ingoreFiles:
     - Statistic
 
    # 需要忽略的文件，只要写目录名就可以，支持正则表达式
    ignoreDirs:
     - config
`#`是YAML格式的注释
然后通过类似这样来指定这个忽略文件：

    $ java -jar ~/DiffCoverage.jar --projectPath . --output ~/xx4 --type upgrade --diffFile diff.txt1  --ignoreFile ~/ignore.yaml
    

