#!/bin/bash
#===============================================================================
#
#          FILE:  add-libs.sh
# 
#         USAGE:  ./add-libs.sh 
# 
#   DESCRIPTION:  
# 
#       OPTIONS:  ---
#  REQUIREMENTS:  ---
#          BUGS:  ---
#         NOTES:  ---
#        AUTHOR:  Wu Liang (Wu Liang), garcia.relax@gmail.com
#       COMPANY:  alibaba B2B
#       VERSION:  1.0
#       CREATED:  02/28/2012 12:14:51 AM CST
#      REVISION:  ---
#===============================================================================

mvn install:install-file -DgroupId=org.openide.filesystems -DartifactId=org.openide.filesystems -Dversion=7.0.1 -Dpackaging=jar -Dfile=lib/org-openide-filesystems.jar
mvn install:install-file -DgroupId=org.openide.modules -DartifactId=org.openide.modules -Dversion=7.0.1 -Dpackaging=jar -Dfile=lib/org-openide-modules.jar
mvn install:install-file -DgroupId=org.openide.util.lookup -DartifactId=org.openide.util.lookup -Dversion=7.0.1 -Dpackaging=jar -Dfile=lib/org-openide-util-lookup.jar
mvn install:install-file -DgroupId=org.openide.util -DartifactId=org.openide.util -Dversion=7.0.1 -Dpackaging=jar -Dfile=lib/org-openide-util.jar
mvn install:install-file -DgroupId=org.openidex.util -DartifactId=org.openidex.util -Dversion=7.0.1 -Dpackaging=jar -Dfile=lib/org-openidex-util.jar
mvn install:install-file -DgroupId=org.eclipse.cdt.core -DartifactId=org.eclipse.cdt.core -Dversion=5.2.1 -Dpackaging=jar -Dfile=lib/org.eclipse.cdt.core_5.2.1.201102110609.jar
mvn install:install-file -DgroupId=org.eclipse.cdt -DartifactId=org.eclipse.cdt -Dversion=7.0.2 -Dpackaging=jar -Dfile=lib/org.eclipse.cdt_7.0.2.201102110609.jar
mvn install:install-file -DgroupId=org.eclipse.core.jobs -DartifactId=org.eclipse.core.jobs -Dversion=3.5.1 -Dpackaging=jar -Dfile=lib/org.eclipse.core.jobs_3.5.1.R36x_v20100824.jar
mvn install:install-file -DgroupId=org.eclipse.core.resources -DartifactId=org.eclipse.core.resources -Dversion=3.6.1 -Dpackaging=jar -Dfile=lib/org.eclipse.core.resources_3.6.1.R36x_v20110131-1630.jar
mvn install:install-file -DgroupId=org.eclipse.core.runtime -DartifactId=org.eclipse.core.runtime -Dversion=3.6.0 -Dpackaging=jar -Dfile=lib/org.eclipse.core.runtime_3.6.0.v20100505.jar
mvn install:install-file -DgroupId=org.eclipse.equinox.common -DartifactId=org.eclipse.equinox.common -Dversion=3.6.0 -Dpackaging=jar -Dfile=lib/org.eclipse.equinox.common_3.6.0.v20110523.jar
mvn install:install-file -DgroupId=org.eclipse.osgi -DartifactId=org.eclipse.osgi -Dversion=3.6.2 -Dpackaging=jar -Dfile=lib/org.eclipse.osgi_3.6.2.R36x_v20110210.jar
mvn install:install-file -DgroupId=org.eclipse.linuxtools.binutils -DartifactId=org.eclipse.linuxtools.binutils -Dversion=4.0.0 -Dpackaging=jar -Dfile=lib/org.eclipse.linuxtools.binutils_4.0.0.201111050234.jar
mvn install:install-file -DgroupId=org.eclipse.linuxtools.dataviewers -DartifactId=org.eclipse.linuxtools.dataviewers -Dversion=4.0.0 -Dpackaging=jar -Dfile=lib/org.eclipse.linuxtools.dataviewers_4.0.0.201111050234.jar
mvn install:install-file -DgroupId=org.eclipse.linuxtools.dataviewers.annotatedsourceeditor -DartifactId=org.eclipse.linuxtools.dataviewers.annotatedsourceeditor -Dversion=4.1.0 -Dpackaging=jar -Dfile=lib/org.eclipse.linuxtools.dataviewers.annotatedsourceeditor_4.1.0.201111050234.jar
mvn install:install-file -DgroupId=org.eclipse.linuxtools.dataviewers.charts -DartifactId=org.eclipse.linuxtools.dataviewers.charts -Dversion=4.1.0 -Dpackaging=jar -Dfile=lib/org.eclipse.linuxtools.dataviewers.charts_4.1.0.201111050234.jar
mvn install:install-file -DgroupId=org.eclipse.linuxtools.gcov -DartifactId=org.eclipse.linuxtools.gcov -Dversion=4.1.1 -Dpackaging=jar -Dfile=lib/org.eclipse.linuxtools.gcov_4.1.1.201108301805.jar
mvn install:install-file -DgroupId=com.googlecode.cambridge -DartifactId=cambridge-core -Dversion=1.0 -Dpackaging=jar -Dfile=lib/cambridge-core-1.0.jar
