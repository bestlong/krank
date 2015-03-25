#Instructions for doing a new release

Bill and I spoke about this here:

http://groups.google.com/group/crank-developer-support/browse_thread/thread/719eda00b39ce1bd

Step 1: Check out to a new folder

```
svn checkout https://krank.googlecode.com/svn/trunk/ krank-release
```

Step 2: Delete the stuff out of the repo

Unix
```
- rm -rf ~/.m2/repository/org/crank 
```

Windows
```
del /S /Q %USERPROFILE%\.m2\repository\org\crank\*.* 
rd /S /Q %USERPROFILE%\.m2\repository\org\crank 
```

Step 3: change dir
```
cd krank-release
```

Step 4: Build crank
```
mvn clean install
```

Step 5: Run mvn prepare
```
mvn release:prepare
```

Step 6: Run mvn perform
```
mvn release:perform 
```