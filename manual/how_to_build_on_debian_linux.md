# How to compile BaldPhone APK from sources, on Debian GNU/Linux

The **BaldPhone** app uses the **Gradle** building tool, this 
means that all the packages required to compile it, can be 
automatically downloaded and installed by the user, without 
requiring to install system packages. This should solve also the 
problem of Debian, shipping rather outdated versions of the 
various tools. Here we tested everything on a **Debian 10 
Stretch**.  Install the **git** and the **OpenJDK** Java runtime 
packages:

```
apt-get install git openjdk-11-jre openjdk-11-jre-headless
```

You shouldn't need to install other Debian packages. E.g. the 
following are NOT required: android-sdk-platform-23, gradle.

Clone the BaldPhone source code tree on your disk:

```
git clone https://github.com/UriahShaulMandel/BaldPhone.git
```

This will create the BaldPhone directory, constaining about 230 
Mb of data.

Create a directory where the Gradle building system will 
download and install all the required stuff to compile the 
Android package. The $HOME environment variable should point to 
your home directory (e.g. /home/johndoe). The environment 
variable **ANDROID_HOME** must be set whenever you will run the 
building tool again:

```
mkdir -p $HOME/lib/android-sdk
export ANDROID_HOME=$HOME/lib/android-sdk
```

Now you have to accept the **Android SDK licenses**. Accepting 
the licenses merely means that you have to put some files into 
the $HOME/lib/android-sdk/licenses/ directory. Fortunately 
enough, that files can be downloaded from the net:

```
mkdir -p $HOME/lib/android-sdk/licenses
git clone https://github.com/Shadowstyler/android-sdk-licenses.git
cp -a android-sdk-licenses/*-license $HOME/lib/android-sdk/licenses
```

Start now a first build run:

```
cd BaldPhone
chmod 755 gradlew
./gradlew
```

This will start a procedure which downloads all the required 
packages, e.g. the new gradle-5.4.1-all.zip, various Java 
libraries, etc. Everything will be installed under the 
$HOME/.gradle/ directory. This will occupy several thousands 
megabytes (about 500 Mb in my case). You shoul obtain the 
**BUILD SUCCESSFUL** message.

You can now try to actually build the APK package, with:

```
./gradlew build
```

This will download other tons of software under the 
$HOME/lib/android-sdk/ directory: Android SDK Build-Tools, 
Platform-Tools, etc. In my case about 330 Mb of software was 
installed. The procedure will install current versions of that 
tools, this is why we did not installed the (rather outdated) 
Debian packages, eg. android-sdk-platform-23. You shoul obtain 
the following result:

```
BUILD SUCCESSFUL in 20m 0s
```

The new **APK package** should exists in this path:

```
./app/build/outputs/apk/baldUpdates/release/app-baldUpdates-release-unsigned.apk
```

Beware of the **"unisgned"** label! This means that you will be 
UNABLE TO INSTALL IT on you smartphone! You should pretend to 
have a valid developer signature and use it to **sign the 
package**. So do the following (eventually replace the name 
johndoe with your name):

```
cd $HOME
keytool -genkey -v -keystore johndoe.keystore -alias johndoe -keyalg RSA -keysize 2048 -validity 10000
```

You have to type a password and some info:

```
Enter keystore password: MySecret
What is your first and last name? John Doe
What is the name of your organizational unit? Android Developer
What is the name of your organization? JohnDoe.Org
What is the name of your City or Locality? Florence
What is the name of your State or Province? Italy
What is the two-letter country code for this unit? IT
```

Now edit the file **$HOME/.gradle/gradle.properties** and write 
something like this:

```
MYAPP_RELEASE_STORE_FILE=/home/johndoe/johndoe.keystore
MYAPP_RELEASE_KEY_ALIAS=johndoe
MYAPP_RELEASE_STORE_PASSWORD=MySecret
MYAPP_RELEASE_KEY_PASSWORD=MySecret
```

Don't forget to protect the file, because it contains your password:

```
chmod 640 $HOME/.gradle/gradle.properties
```

Update your build recipe editing the **app/build.gradle** file:

```
cd BaldPhone/
vi app/build.gradle
```

You should add a **signingConfigs** section and a **buildTypes 
release** line:

```
android {
    ...
    defaultConfig {
        ...
    }
    signingConfigs {
        release {
            if (project.hasProperty('MYAPP_RELEASE_STORE_FILE')) {
                storeFile file(MYAPP_RELEASE_STORE_FILE)
                storePassword MYAPP_RELEASE_STORE_PASSWORD
                keyAlias MYAPP_RELEASE_KEY_ALIAS
                keyPassword MYAPP_RELEASE_KEY_PASSWORD
            }
        }
    }

    buildTypes {
        release {
            ...
            signingConfig signingConfigs.release
        }
    }
```

Finally you can repeat the build:

```
./gradlew build
```

The result should be this APK file:

```
./app/build/outputs/apk/baldUpdates/release/app-baldUpdates-release.apk
```

Copy this file on your smartphone and install it!

If you modify something into the source tree, you have to repeat 
only the **./gradlew build** step to obtain a new apk file. 
Never forget to export the **ANDROID_HOME** variable.
