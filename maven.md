**Preconditions:**
  1. You must have run a mvn install before attempting the release.
  1. You may also want to run `mvn scm:validate` and `mvn scm:changelog` to make sure your subversion connection is happy or else the release attempt may fail.
> If you get _ERROR svn: Can't connect to host 'krank.googlecode.com': A connection attempt failed because the connected party did not properly respond after a period of time, or established connection failed because connected host has failed to respond._
> then you need to run `svn log -v https://krank.googlecode.com/svn/` manually, and accept the server certificate (p for permanently).  You should not need to repeat this step.  Rerun `mvn scm:changelog` and it should work out this time.

See:
http://maven.apache.org/plugins/maven-release-plugin/examples/prepare-release.html

This document was written performing a release of Krank Validation.

Check the version of the root POM file.  Make a mental note...

The first step is to 'prepare' the release, in maven lingo.  This performs a number of functions:

`mvn release:prepare -Dresume=false -Dusername=[[svnuser]] -Dpassword=[[svnpwd]] -DtagBase=https://krank.googlecode.com/svn/code/tags/`

Carefully answer the prompts, you will be asked for
  * A release version number
  * A release tag
  * A new development version number

If anything goes wrong, the safest thing is usually to start over.  This requires
`mvn release:prepare -Dresume=false` (or `mvn release:clean release:prepare`).  You may also need to revert the POM's that were modified by the release:prepare goal.

This will:
  * Create versioned release jars
  * Create a tag in scm.
  * Update scm with re-versioned POMs.
  * Modify the root POM's scm connection URL to point to the svn release tag.

When you complete this step successfully, run `mvn release:clean`

Finally, we are ready to do the ACTUAL release :) This step will pull the sources down from the tag, build the artifacts, and install them to the repository.  Here goes...

`mvn release:perform -Dusername=[[svnuser]] -Dpassword=[[svnpwd]] -DtagBase=https://krank.googlecode.com/svn/code/tags/ -DconnectionUrl=scm:svn:https://krank.googlecode.com/svn/ -Dtag="Initial Release 0.1"`

**TODO** _Fill in the distribution management section of the POM so this command actually does something_

Until the above TODO is completed (I.E. we have a repository to scp or ftp to) you can manually install the jars into a repo using the following commands.

mvn install:install-file -DgroupId=org.crank -DartifactId=crank-core -Dversion=0.1 -Dpackaging=jar -Dfile=crank-core-0.1.jar

mvn install:install-file -DgroupId=org.crank -DartifactId=crank-jsf-validation -Dversion=0.1 -Dpackaging=jar -Dfile=crank-jsf-validation-0.1.jar

mvn install:install-file -DgroupId=org.crank -DartifactId=crank-springmvc-validation -Dversion=0.1 -Dpackaging=jar -Dfile=crank-springmvc-validation-0.1.jar

mvn install:install-file -DgroupId=org.crank -DartifactId=crank-test-support -Dversion=0.1 -Dpackaging=jar -Dfile=crank-test-support-0.1.jar

mvn install:install-file -DgroupId=org.crank -DartifactId=crank-validation -Dversion=0.1 -Dpackaging=jar -Dfile=crank-validation-0.1.jar


Once the release is complete and you have the artifacts you desire, you may want to go back and alter the root POM to point back to trunk (or whatever branch you are working out of).
